package com.TracoCultural.TracoCultural.controller;

import com.TracoCultural.TracoCultural.config.security.JwtUtil;
import com.TracoCultural.TracoCultural.model.Repository.UsuarioRepository;
import com.TracoCultural.TracoCultural.model.entity.Usuario;
import com.TracoCultural.TracoCultural.model.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final int MAX_TENTATIVAS = 5;
    private static final int JANELA_MINUTOS = 10;
    private static final int CODIGO_VALIDADE_MINUTOS = 15;
    private static final int REENVIO_INTERVALO_SEGUNDOS = 60;

    private final Map<String, List<LocalDateTime>> tentativasPorEmail = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getSenha() == null || usuario.getNome() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Nome, email e senha são obrigatórios")
            );
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return ResponseEntity.status(409).body(
                    Map.of("status", 409, "retorno", "Conflict", "message", "Email já cadastrado")
            );
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setConfirmado(false);
        usuario.setCodigoVerificacao(gerarCodigo());
        usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(CODIGO_VALIDADE_MINUTOS));

        Usuario novo = usuarioRepository.save(usuario);

        try {
            emailService.enviarCodigoConfirmacao(novo.getEmail(), novo.getNome(), novo.getCodigoVerificacao());
        } catch (RuntimeException e) {
            // A conta ja foi criada (nao-confirmada); o usuario pode pedir reenvio depois.
            return ResponseEntity.status(201).body(Map.of(
                    "id", novo.getId(),
                    "email", novo.getEmail(),
                    "message", "Conta criada, mas houve falha ao enviar o email de confirmação. Use \"Reenviar código\" na tela seguinte."
            ));
        }

        return ResponseEntity.status(201).body(Map.of(
                "id",    novo.getId(),
                "email", novo.getEmail(),
                "message", "Conta criada. Enviamos um código de confirmação para o seu email."
        ));
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email e senha são obrigatórios")
            );
        }

        if (estaBloqueado(email)) {
            return ResponseEntity.status(429).body(
                    Map.of("status", 429, "retorno", "Too Many Requests",
                            "message", "Muitas tentativas. Tente novamente em alguns minutos.")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenha())) {
            registrarTentativa(email);
            return ResponseEntity.status(401).body(
                    Map.of("status", 401, "retorno", "Unauthorized", "message", "Email ou senha inválidos")
            );
        }

        if (!usuario.isConfirmado() && !usuario.getIsAdm()) {
            return ResponseEntity.status(403).body(
                    Map.of("status", 403, "retorno", "Forbidden",
                            "message", "Email ainda não confirmado. Verifique seu email para ativar a conta.",
                            "emailNaoConfirmado", true)
            );
        }

        tentativasPorEmail.remove(email);
        String token = jwtUtil.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(Map.of(
                "token",  token,
                "id",     usuario.getId(),
                "nome",   usuario.getNome(),
                "email",  usuario.getEmail(),
                "isAdm",  usuario.getIsAdm()
        ));
    }

    // POST /api/v1/auth/verificar-codigo
    @PostMapping("/verificar-codigo")
    public ResponseEntity<Object> verificarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email e código são obrigatórios")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado")
            );
        }

        if (usuario.isConfirmado()) {
            String token = jwtUtil.gerarToken(usuario.getEmail());
            return ResponseEntity.ok(Map.of(
                    "token", token, "id", usuario.getId(), "nome", usuario.getNome(),
                    "email", usuario.getEmail(), "isAdm", usuario.getIsAdm()
            ));
        }

        if (usuario.getCodigoVerificacao() == null
                || usuario.getCodigoExpiracao() == null
                || usuario.getCodigoExpiracao().isBefore(LocalDateTime.now())
                || !usuario.getCodigoVerificacao().equals(codigo)) {
            return ResponseEntity.status(400).body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Código inválido ou expirado.")
            );
        }

        usuario.setConfirmado(true);
        usuario.setCodigoVerificacao(null);
        usuario.setCodigoExpiracao(null);
        usuarioRepository.save(usuario);

        String token = jwtUtil.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(Map.of(
                "token", token, "id", usuario.getId(), "nome", usuario.getNome(),
                "email", usuario.getEmail(), "isAdm", usuario.getIsAdm()
        ));
    }

    // POST /api/v1/auth/reenviar-codigo
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<Object> reenviarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email é obrigatório")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado")
            );
        }

        if (usuario.isConfirmado()) {
            return ResponseEntity.status(400).body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Este email já foi confirmado.")
            );
        }

        if (estaBloqueadoPorReenvio(email)) {
            return ResponseEntity.status(429).body(
                    Map.of("status", 429, "retorno", "Too Many Requests",
                            "message", "Aguarde um pouco antes de pedir outro código.")
            );
        }

        usuario.setCodigoVerificacao(gerarCodigo());
        usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(CODIGO_VALIDADE_MINUTOS));
        usuarioRepository.save(usuario);
        registrarReenvio(email);

        try {
            emailService.enviarCodigoConfirmacao(usuario.getEmail(), usuario.getNome(), usuario.getCodigoVerificacao());
        } catch (RuntimeException e) {
            return ResponseEntity.status(502).body(
                    Map.of("status", 502, "retorno", "Bad Gateway", "message", "Falha ao enviar o email. Tente novamente em instantes.")
            );
        }

        return ResponseEntity.ok(Map.of("message", "Código reenviado com sucesso."));
    }

    // POST /api/v1/auth/esqueci-senha
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Object> esqueciSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email é obrigatório")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        // Sempre responde OK (mesmo se o email não existir) para não revelar quais emails estão cadastrados.
        if (usuario == null) {
            return ResponseEntity.ok(Map.of("message", "Se o email existir, um código de redefinição foi enviado."));
        }

        if (estaBloqueadoPorReenvio(email)) {
            return ResponseEntity.status(429).body(
                    Map.of("status", 429, "retorno", "Too Many Requests",
                            "message", "Aguarde um pouco antes de pedir outro código.")
            );
        }

        usuario.setCodigoVerificacao(gerarCodigo());
        usuario.setCodigoExpiracao(LocalDateTime.now().plusMinutes(CODIGO_VALIDADE_MINUTOS));
        usuarioRepository.save(usuario);
        registrarReenvio(email);

        try {
            emailService.enviarCodigoRedefinicaoSenha(usuario.getEmail(), usuario.getNome(), usuario.getCodigoVerificacao());
        } catch (RuntimeException e) {
            return ResponseEntity.status(502).body(
                    Map.of("status", 502, "retorno", "Bad Gateway", "message", "Falha ao enviar o email. Tente novamente em instantes.")
            );
        }

        return ResponseEntity.ok(Map.of("message", "Se o email existir, um código de redefinição foi enviado."));
    }

    // POST /api/v1/auth/validar-codigo
    // Apenas confere se o código bate, sem consumi-lo (usado pelo front pra
    // revelar os campos de nova senha em tempo real, antes do submit final).
    @PostMapping("/validar-codigo")
    public ResponseEntity<Object> validarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest().body(Map.of("valido", false));
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        boolean valido = usuario != null
                && usuario.getCodigoVerificacao() != null
                && usuario.getCodigoExpiracao() != null
                && usuario.getCodigoExpiracao().isAfter(LocalDateTime.now())
                && usuario.getCodigoVerificacao().equals(codigo);

        return ResponseEntity.ok(Map.of("valido", valido));
    }

    // POST /api/v1/auth/redefinir-senha
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Object> redefinirSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");
        String novaSenha = body.get("novaSenha");

        if (email == null || codigo == null || novaSenha == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Email, código e nova senha são obrigatórios")
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body(
                    Map.of("status", 404, "retorno", "Not Found", "message", "Usuário não encontrado")
            );
        }

        if (usuario.getCodigoVerificacao() == null
                || usuario.getCodigoExpiracao() == null
                || usuario.getCodigoExpiracao().isBefore(LocalDateTime.now())
                || !usuario.getCodigoVerificacao().equals(codigo)) {
            return ResponseEntity.status(400).body(
                    Map.of("status", 400, "retorno", "Bad Request", "message", "Código inválido ou expirado.")
            );
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setCodigoVerificacao(null);
        usuario.setCodigoExpiracao(null);
        // Se a conta ainda não estava confirmada, redefinir a senha com sucesso já confirma o email.
        usuario.setConfirmado(true);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }

    private String gerarCodigo() {
        int codigo = random.nextInt(1_000_000);
        return String.format("%06d", codigo);
    }

    private final Map<String, LocalDateTime> ultimoReenvioPorEmail = new ConcurrentHashMap<>();

    private boolean estaBloqueadoPorReenvio(String email) {
        LocalDateTime ultimo = ultimoReenvioPorEmail.get(email);
        return ultimo != null && ultimo.plusSeconds(REENVIO_INTERVALO_SEGUNDOS).isAfter(LocalDateTime.now());
    }

    private void registrarReenvio(String email) {
        ultimoReenvioPorEmail.put(email, LocalDateTime.now());
    }

    private void registrarTentativa(String email) {
        tentativasPorEmail.computeIfAbsent(email, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    private boolean estaBloqueado(String email) {
        List<LocalDateTime> tentativas = tentativasPorEmail.get(email);
        if (tentativas == null) return false;
        LocalDateTime janela = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);
        tentativas.removeIf(t -> t.isBefore(janela));
        return tentativas.size() >= MAX_TENTATIVAS;
    }
}