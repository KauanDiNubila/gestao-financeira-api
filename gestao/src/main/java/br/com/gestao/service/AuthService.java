package br.com.gestao.service;

import br.com.gestao.config.JwtUtil;
import br.com.gestao.domain.usuario.Usuario;
import br.com.gestao.domain.usuario.UsuarioRepository;
import br.com.gestao.dto.CadastroRequest;
import br.com.gestao.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void cadastrar(CadastroRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .build();

        usuarioRepository.save(usuario);
    }

    public String login(LoginRequest request) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (BadCredentialsException e) {

            throw new BadCredentialsException("Email ou senha inválidos");
        }

        return jwtUtil.gerarToken(request.email());
    }
}
