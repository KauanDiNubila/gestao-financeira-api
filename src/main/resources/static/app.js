// ============================================================
//  Estado em memória
// ============================================================
let token = null;
let paginaAtual = 0;
let totalPaginas = 1;
const TAMANHO_PAGINA = 8;

// ============================================================
//  Alternar abas de login/cadastro
// ============================================================
function trocarAba(aba) {
    const tabLogin = document.getElementById("tab-login");
    const tabCadastro = document.getElementById("tab-cadastro");
    const formLogin = document.getElementById("form-login");
    const formCadastro = document.getElementById("form-cadastro");
    esconderMsg("auth-msg");

    if (aba === "login") {
        tabLogin.classList.add("active");
        tabCadastro.classList.remove("active");
        formLogin.classList.remove("hidden");
        formCadastro.classList.add("hidden");
    } else {
        tabCadastro.classList.add("active");
        tabLogin.classList.remove("active");
        formCadastro.classList.remove("hidden");
        formLogin.classList.add("hidden");
    }
}

// ============================================================
//  Autenticação
// ============================================================
async function cadastrar() {
    const nome = document.getElementById("cad-nome").value;
    const email = document.getElementById("cad-email").value;
    const senha = document.getElementById("cad-senha").value;

    try {
        const res = await fetch(`${API_BASE}/auth/cadastro`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, email, senha })
        });

        if (res.status === 409) {
            throw new Error("Esse email já está cadastrado.");
        }
        if (!res.ok) {
            throw new Error("Erro ao cadastrar. Verifique os dados.");
        }

        mostrarMsg("auth-msg", "Conta criada! Faça login para entrar.", "sucesso");
        trocarAba("login");
    } catch (err) {
        mostrarMsg("auth-msg", err.message, "erro");
    }
}

async function login() {
    const email = document.getElementById("login-email").value;
    const senha = document.getElementById("login-senha").value;

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        if (!res.ok) {
            throw new Error("Email ou senha inválidos.");
        }

        // O login retorna o token como texto puro (não JSON)
        token = await res.text();

        document.getElementById("auth-section").classList.add("hidden");
        document.getElementById("main-panel").classList.remove("hidden");
        document.getElementById("user-info").textContent = email;

        // Define o mês atual no seletor de resumo
        const agora = new Date();
        const mesAtual = `${agora.getFullYear()}-${String(agora.getMonth() + 1).padStart(2, "0")}`;
        document.getElementById("resumo-mes").value = mesAtual;

        // Carrega os dados iniciais
        await carregarResumo();
        await carregarTransacoes();
        await carregarPorCategoria();
    } catch (err) {
        mostrarMsg("auth-msg", err.message, "erro");
    }
}

function logout() {
    token = null;
    paginaAtual = 0;
    document.getElementById("main-panel").classList.add("hidden");
    document.getElementById("auth-section").classList.remove("hidden");
}

function authHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

// ============================================================
//  Resumo mensal
// ============================================================
async function carregarResumo() {
    const mes = document.getElementById("resumo-mes").value;
    if (!mes) return;

    try {
        const res = await fetch(`${API_BASE}/transacoes/resumo?mes=${mes}`, {
            headers: authHeaders()
        });
        if (!res.ok) throw new Error("Erro ao carregar resumo");
        const resumo = await res.json();

        document.getElementById("total-receitas").textContent = formatarMoeda(resumo.totalReceitas);
        document.getElementById("total-despesas").textContent = formatarMoeda(resumo.totalDespesas);
        document.getElementById("saldo").textContent = formatarMoeda(resumo.saldo);
    } catch (err) {
        console.error(err);
    }
}

// ============================================================
//  Transações (com paginação)
// ============================================================
async function carregarTransacoes() {
    try {
        const res = await fetch(
            `${API_BASE}/transacoes?page=${paginaAtual}&size=${TAMANHO_PAGINA}&sort=data,desc`,
            { headers: authHeaders() }
        );
        if (!res.ok) throw new Error("Erro ao carregar transações");
        const pagina = await res.json();

        renderTransacoes(pagina.content);
        totalPaginas = pagina.totalPages || 1;
        atualizarPaginacao();
    } catch (err) {
        console.error(err);
    }
}

function renderTransacoes(transacoes) {
    const list = document.getElementById("transacoes-list");
    list.innerHTML = "";

    if (transacoes.length === 0) {
        list.innerHTML = '<li><span class="item-detail">Nenhuma transação encontrada.</span></li>';
        return;
    }

    transacoes.forEach(t => {
        const ehReceita = t.tipo === "RECEITA";
        const li = document.createElement("li");
        li.innerHTML = `
            <div class="item-info">
                <span class="item-title">${t.descricao}</span>
                <span class="item-detail">
                    <span class="badge ${ehReceita ? "badge-receita" : "badge-despesa"}">${t.tipo}</span>
                    &middot; ${t.categoria} &middot; ${formatarData(t.data)}
                </span>
            </div>
            <div class="item-actions">
                <span class="item-valor ${ehReceita ? "valor-receita" : "valor-despesa"}">
                    ${ehReceita ? "+" : "-"} ${formatarMoeda(t.valor)}
                </span>
                <button class="btn-delete" onclick="deletarTransacao(${t.id})">Excluir</button>
            </div>
        `;
        list.appendChild(li);
    });
}

async function criarTransacao(event) {
    event.preventDefault();
    const transacao = {
        descricao: document.getElementById("t-descricao").value,
        valor: parseFloat(document.getElementById("t-valor").value),
        tipo: document.getElementById("t-tipo").value,
        categoria: document.getElementById("t-categoria").value,
        data: document.getElementById("t-data").value
    };

    try {
        const res = await fetch(`${API_BASE}/transacoes`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify(transacao)
        });
        if (!res.ok) throw new Error("Erro ao criar transação");

        event.target.reset();
        mostrarMsg("transacao-msg", "Transação adicionada!", "sucesso");
        paginaAtual = 0;
        await carregarTransacoes();
        await carregarResumo();
        await carregarPorCategoria();
    } catch (err) {
        mostrarMsg("transacao-msg", err.message, "erro");
    }
}

async function deletarTransacao(id) {
    try {
        const res = await fetch(`${API_BASE}/transacoes/${id}`, {
            method: "DELETE",
            headers: authHeaders()
        });
        if (!res.ok) throw new Error("Erro ao excluir");

        await carregarTransacoes();
        await carregarResumo();
        await carregarPorCategoria();
    } catch (err) {
        console.error(err);
    }
}

// ============================================================
//  Paginação
// ============================================================
function atualizarPaginacao() {
    document.getElementById("pagina-info").textContent = `Página ${paginaAtual + 1} de ${totalPaginas}`;
    document.getElementById("btn-anterior").disabled = paginaAtual === 0;
    document.getElementById("btn-proxima").disabled = paginaAtual >= totalPaginas - 1;
}

function paginaAnterior() {
    if (paginaAtual > 0) {
        paginaAtual--;
        carregarTransacoes();
    }
}

function proximaPagina() {
    if (paginaAtual < totalPaginas - 1) {
        paginaAtual++;
        carregarTransacoes();
    }
}

// ============================================================
//  Despesas por categoria
// ============================================================
async function carregarPorCategoria() {
    try {
        const res = await fetch(`${API_BASE}/transacoes/por-categoria`, {
            headers: authHeaders()
        });
        if (!res.ok) throw new Error("Erro ao carregar categorias");
        const categorias = await res.json();
        renderCategorias(categorias);
    } catch (err) {
        console.error(err);
    }
}

function renderCategorias(categorias) {
    const list = document.getElementById("categoria-list");
    list.innerHTML = "";

    if (categorias.length === 0) {
        list.innerHTML = '<li><span class="item-detail">Nenhuma despesa registrada.</span></li>';
        return;
    }

    categorias.forEach(c => {
        const li = document.createElement("li");
        li.innerHTML = `
            <div class="item-info">
                <span class="item-title">${c.categoria}</span>
            </div>
            <span class="item-valor valor-despesa">${formatarMoeda(c.total)}</span>
        `;
        list.appendChild(li);
    });
}

// ============================================================
//  Helpers
// ============================================================
function formatarMoeda(valor) {
    const numero = Number(valor) || 0;
    return numero.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatarData(isoString) {
    if (!isoString) return "—";
    const [ano, mes, dia] = isoString.split("-");
    return `${dia}/${mes}/${ano}`;
}

function mostrarMsg(id, texto, tipo) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.className = `msg msg-${tipo}`;
    el.classList.remove("hidden");
}

function esconderMsg(id) {
    document.getElementById(id).classList.add("hidden");
}
