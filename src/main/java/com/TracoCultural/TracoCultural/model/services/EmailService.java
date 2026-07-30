package com.TracoCultural.TracoCultural.model.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.remetente-nome:Traco Cultural}")
    private String remetenteNome;

    /**
     * Envia o codigo de confirmacao de cadastro (ou reenvio) para o email do usuario.
     * Lanca RuntimeException se o envio falhar, para que o controller possa
     * responder com um erro claro ao invés de mascarar a falha.
     */
    public void enviarCodigoConfirmacao(String destinatario, String nome, String codigo) {
        String assunto = "Confirme seu cadastro - " + remetenteNome;
        String corpo = "Ola, " + nome + "!\n\n"
                + "Seu codigo de confirmacao e: " + codigo + "\n\n"
                + "Esse codigo expira em 15 minutos.\n"
                + "Se voce nao criou uma conta no " + remetenteNome + ", pode ignorar este email.";

        enviar(destinatario, assunto, corpo);
    }

    /**
     * Envia o codigo de redefinicao de senha para o email do usuario.
     */
    public void enviarCodigoRedefinicaoSenha(String destinatario, String nome, String codigo) {
        String assunto = "Redefinicao de senha - " + remetenteNome;
        String corpo = "Ola, " + nome + "!\n\n"
                + "Recebemos uma solicitacao para redefinir sua senha.\n"
                + "Seu codigo de redefinicao e: " + codigo + "\n\n"
                + "Esse codigo expira em 15 minutos.\n"
                + "Se voce nao solicitou isso, pode ignorar este email.";

        enviar(destinatario, assunto, corpo);
    }

    private void enviar(String destinatario, String assunto, String corpo) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setTo(destinatario);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mailSender.send(mensagem);
        } catch (MailException e) {
            logger.error("Falha ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Nao foi possivel enviar o email de confirmacao. Tente novamente mais tarde.", e);
        }
    }
}
