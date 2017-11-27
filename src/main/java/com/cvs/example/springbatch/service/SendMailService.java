package com.cvs.example.springbatch.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileFilter;

public class SendMailService {

    private static final Log log = LogFactory.getLog(SendMailService.class);
    private JavaMailSender mailSender;
    private String senderAddress;
    private String recipient;
    private String attachmentFilePath;

    // set the fields
    public void setFields(JavaMailSender mailSender, String senderAddress, String recipient, String attachmentFilePath) {

        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
        this.recipient = recipient;
        this.attachmentFilePath = attachmentFilePath;
    }

    public void sendMail() {
        log.debug("send Email started");
        // read directory
        // get file from directory
        final File file =new FileSystemResource(attachmentFilePath).getFile();

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                mimeMessage.setFrom(new InternetAddress(senderAddress));
                mimeMessage.setSubject("Neuer Report");
                // MimeMessagesHelper is needed for the attachment. The Boolean value in
                // constructor is for multipart/data = true
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.addAttachment(file.getName(), new FileSystemResource(file));
                helper.setText("Text in Email Body");
            }
        };
        try {
            this.mailSender.send(preparator);

            log.debug("send Email completed");
        } catch (MailException ex) {
            log.debug("send Email failed", ex);
        }
    }

    public static FileFilter FILE_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return !file.isDirectory();
        }
    };
}