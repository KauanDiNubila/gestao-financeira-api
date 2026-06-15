CREATE TABLE transacoes (
    id          BIGSERIAL PRIMARY KEY,
    descricao   VARCHAR(255)   NOT NULL,
    valor       NUMERIC(15, 2) NOT NULL,
    tipo        VARCHAR(10)    NOT NULL,
    categoria   VARCHAR(100)   NOT NULL,
    data        DATE           NOT NULL,
    usuario_id  BIGINT         NOT NULL,

    CONSTRAINT fk_transacao_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);