package com.TracoCultural.TracoCultural.model.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final String COR_DOURADO = "#D4A373";
    private static final String COR_DOURADO_ESCURO = "#B8875A";
    private static final String COR_FUNDO = "#FAF6F1";
    private static final String COR_TEXTO = "#2E2A26";
    private static final String COR_TEXTO_SUAVE = "#6B6560";

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.remetente-nome:Traço Cultural}")
    private String remetenteNome;

    public void enviarCodigoConfirmacao(String destinatario, String nome, String codigo) {
        String assunto = "Confirme seu cadastro - " + remetenteNome;
        String html = montarTemplate(
                "Confirme seu cadastro",
                "Olá, " + escapeHtml(nome) + "!",
                "Use o código abaixo para confirmar sua conta e começar a explorar o " + escapeHtml(remetenteNome) + ".",
                codigo,
                "Se você não criou uma conta no " + escapeHtml(remetenteNome) + ", pode ignorar este email com segurança."
        );
        enviar(destinatario, assunto, html);
    }

  
    public void enviarCodigoRedefinicaoSenha(String destinatario, String nome, String codigo) {
        String assunto = "Redefinição de senha - " + remetenteNome;
        String html = montarTemplate(
                "Redefinição de senha",
                "Olá, " + escapeHtml(nome) + "!",
                "Recebemos uma solicitação para redefinir a senha da sua conta. Use o código abaixo para continuar.",
                codigo,
                "Se você não solicitou isso, pode ignorar este email — sua senha continuará a mesma."
        );
        enviar(destinatario, assunto, html);
    }

    private String montarTemplate(String titulo, String saudacao, String instrucao, String codigo, String rodape) {
        return "<!DOCTYPE html>"
            + "<html lang=\"pt-BR\"><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:0;background-color:" + COR_FUNDO + ";font-family:'Segoe UI',Helvetica,Arial,sans-serif;\">"
            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:" + COR_FUNDO + ";padding:32px 16px;\">"
            + "<tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"100%\" style=\"max-width:480px;background-color:#FFFFFF;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.06);\">"

            + "<tr><td style=\"background:linear-gradient(135deg," + COR_DOURADO + "," + COR_DOURADO_ESCURO + ");padding:28px 32px;\">"
            + "<p style=\"margin:0;color:#FFFFFF;font-size:22px;font-weight:700;letter-spacing:0.3px;\">Traço Cultural</p>"
            + "</td></tr>"

            + "<tr><td style=\"padding:36px 32px 28px 32px;\">"
            + "<h1 style=\"margin:0 0 16px 0;color:" + COR_TEXTO + ";font-size:20px;font-weight:600;\">" + titulo + "</h1>"
            + "<p style=\"margin:0 0 8px 0;color:" + COR_TEXTO + ";font-size:15px;line-height:1.5;\">" + saudacao + "</p>"
            + "<p style=\"margin:0 0 28px 0;color:" + COR_TEXTO_SUAVE + ";font-size:14px;line-height:1.6;\">" + instrucao + "</p>"

            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td align=\"center\" style=\"background-color:" + COR_FUNDO + ";border:1px dashed " + COR_DOURADO + ";border-radius:12px;padding:20px;\">"
            + "<span style=\"font-size:32px;font-weight:700;letter-spacing:8px;color:" + COR_DOURADO_ESCURO + ";font-family:'Courier New',monospace;\">" + codigo + "</span>"
            + "</td></tr></table>"

            + "<p style=\"margin:24px 0 0 0;color:" + COR_TEXTO_SUAVE + ";font-size:13px;line-height:1.5;\">⏱️ Este código expira em <strong>15 minutos</strong>.</p>"
            + "</td></tr>"

            + "<tr><td style=\"padding:20px 32px 28px 32px;border-top:1px solid #F0E9E1;\">"
            + "<p style=\"margin:0;color:" + COR_TEXTO_SUAVE + ";font-size:12px;line-height:1.6;\">" + rodape + "</p>"
            + "</td></tr>"

            + "</table>"
            + "<p style=\"margin:20px 0 0 0;color:#B0A89E;font-size:11px;\">© Traço Cultural</p>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    private String escapeHtml(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void enviar(String destinatario, String assunto, String htmlCorpo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(htmlCorpo, true);
            mailSender.send(mensagem);
        } catch (MailException | MessagingException e) {
            logger.error("Falha ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Nao foi possivel enviar o email de confirmacao. Tente novamente mais tarde.", e);
        }
    }
}
