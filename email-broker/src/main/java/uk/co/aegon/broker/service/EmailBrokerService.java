package uk.co.aegon.broker.service;

import java.util.Arrays;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import uk.co.aegon.broker.controller.dto.MIEmailEvent;

@Service
@Slf4j
public class EmailBrokerService {
	
	@Autowired
    private JavaMailSender emailSender;
	
	@Value("classpath:templates/aegonlogo.png")
	private Resource aegonLogo;
	
	@Value("classpath:templates/bgemail.png")
	private Resource bgemail;
		
	@Value("${atos.mailsend.throwexceptions:true}")
	private boolean throwexceptions;
	
	public void email(boolean isUseAegonLogo, String from, String subject, String content, String... to){
		log.info("sending emails to " + Arrays.toString(to));
		MimeMessage message = emailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, true);
			helper.setFrom(from);
			if (isUseAegonLogo)
			{
				helper.addInline("aegonlogo.png", aegonLogo);
			//	helper.addInline("bgemail.png", bgemail);  // couldn't get the background image to show in email in Outlook 2013 
			}
			emailSender.send(message);
		} catch (MailSendException mse){
	        if (detectInvalidAddress(mse)) {
				log.error("MailSendException: invalid address - ignore and continue", mse);
	        } else {
				log.error("MailSendException: ", mse);
	        	if (throwexceptions) {
	        		throw mse;
	        	}
	        }
		} catch (MailException me){
			log.error("MailException: composing or sending email failed!", me);
	       	if (throwexceptions) {
	       		throw me;
	       	}
		} catch (MessagingException e) {
			log.error("composing or sending email failed!", e);
		}
	}
	
	public void email(MIEmailEvent event) {
		MimeMessage mime = emailSender.createMimeMessage();
		boolean multipart = event.getAttachment() != null && StringUtils.isNotEmpty(event.getAttachmentName());
		
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mime,multipart, "UTF-8");

			if (event.getBlindCopy() != null) {
				helper.setBcc(event.getBlindCopy());				
			}
			if (event.getCarbonCopy() != null) {
				helper.setCc(event.getCarbonCopy());
			}
			if (event.getToAddresses() != null) {
				helper.setTo(event.getToAddresses());				
			} else {
				log.error("attempting to send an email with no 'to' addresses! REFUSING TO ATTEMPT EMAIL SEND");
				return;
			}
			if (event.getSubject() != null) {
				helper.setSubject(event.getSubject());				
			} else {
				log.warn("attempting to send an email with no subject");
			}
			if (event.getFromAddress() != null) {
				helper.setFrom(event.getFromAddress());				
			} else {
				log.warn("attempting to send an email with no from address");
			}
			if (event.getContent() != null) {
				helper.setText(event.getContent());				
			} else {
				log.warn("attempting to send an email with no content");
			}
			if (event.getAttachment() != null && event.getAttachmentName() != null) {
				helper.addAttachment(event.getAttachmentName(), event.getAttachment());				
			}
			emailSender.send(mime);
		} catch (MailSendException mse){
	        if (detectInvalidAddress(mse)) {
				log.error("MailSendException - invalid address - ignore and continue", mse);
	        } else {
				log.error("MailSendException: ", mse);
	        	if (throwexceptions) {
	        		throw mse;
	        	}
	        }
		} catch (MailException me){
			log.error("MailException - composing or sending email failed!", me);
	       	if (throwexceptions) {
	       		throw me;
	       	}
		} catch (MessagingException e) {
			log.error("MessagingException: composing or sending email failed!", e);
		}
	}
	
	private boolean detectInvalidAddress(MailSendException me) {
		boolean invalidAddressFlag = false;
	    Exception[] messageExceptions = me.getMessageExceptions();
	    if (messageExceptions.length > 0) {
	        Exception messageException = messageExceptions[0];
	        if (messageException instanceof SendFailedException) {
	            SendFailedException sfe = (SendFailedException) messageException;
	            Address[] invalidAddresses = sfe.getInvalidAddresses();
	            StringBuilder addressStr = new StringBuilder();
	            for (Address address : invalidAddresses) {
	                addressStr.append(address.toString()).append("; ");
	            }
	            
	            if (invalidAddresses!=null && invalidAddresses.length > 0) {
	            	invalidAddressFlag = true;
	            }

	            log.error("invalid address(es)：{}", addressStr);
	        }
	    }

	    log.error("exception while sending mail.", me);
	    
        return invalidAddressFlag;
	}
}
