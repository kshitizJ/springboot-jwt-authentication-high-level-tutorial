package com.jwt.supportportal.service;

import static com.jwt.supportportal.constant.EmailConstant.*;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.sun.mail.smtp.SMTPTransport;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendSuccessfullyRegisterMessage(String firstname, String email) throws MessagingException {

        // Getting the message
        Message message = createEmail(firstname, email);

        // Initializing the SMTPTransport instance to get the transport protocol which
        // is "smtp".
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);

        // Connecting to the gmail server
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);

        // Here sendMessage takes 2 params, the actual message and 2nd one is all the
        // recipients who are going to get that message. We get all the recipients by
        // getAllRecipients() method from the Message instance.
        smtpTransport.sendMessage(message, message.getAllRecipients());

        // Closing the connection once the message is sent.
        smtpTransport.close();

    }

    /**
     * 
     * Here we initialize the Message abstract class as an instance of MimeMessage
     * which extends Message class.
     * 
     * <p>
     * We are getting our session from getEmailSession() method and passing to the
     * constructor to initialize the message.
     * 
     * @param firstname
     * @param password
     * @param email
     * @return
     * @throws MessagingException
     */
    private Message createEmail(String firstname, String email) throws MessagingException {

        Message message = new MimeMessage(getEmailSession());

        // Setting which email address will be used to send the mail.
        message.setFrom(new InternetAddress(FROM_EMAIL));

        // Setting the recipients and parsing their email address. Here second parameter
        // of parse() method is strict which we are setting false. We can set as many
        // recipients as we want.
        message.setRecipients(TO, InternetAddress.parse(email, false));

        // Setting the emails in CC. In our case it is null.
        message.setRecipients(CC, InternetAddress.parse(CC_EMAIL, false));

        // Setting subject of our email
        message.setSubject(EMAIL_SUBJECT);

        // Setting the body of our email.
        message.setText("Hello " + firstname
                + "\n\n\n You have successfully registered your account. Please go ahead and login.");

        // Setting the sending date of our mail.
        message.setSentDate(new Date());

        // Saving all the changes
        message.saveChanges();

        return message;

    }

    /**
     * 
     * {@link Properties} is a subclass of Hashtable. Properties object does not
     * require external synchronization and Multiple threads can share a single
     * Properties object. It can also be used to retrieve the properties of the
     * system.
     * 
     * <p>
     * Here we are retrieving the properties of the {@link System}.
     * 
     * We are creating an email Session and setting the instance of the session by
     * passing the {@link Properties} object and authenticator which set to null.
     * 
     * @return
     */
    private Session getEmailSession() {

        Properties properties = System.getProperties();

        // Setting the email server.
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);

        // Setting the Authentication to true.
        properties.put(SMTP_AUTH, true);

        // Setting the default port of the smtp
        properties.put(SMTP_PORT, DEFAULT_PORT);

        // Enabling the Tranfer Layer Security (TLS.)
        properties.put(SMTP_STARTTLS_ENABLE, true);

        // Enabling Transfer Layer Security to always be required.
        properties.put(SMTP_STARTTLS_REQUIRED, true);

        return Session.getInstance(properties, null);

    }

}
