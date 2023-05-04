package uk.co.aegon.broker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import uk.co.aegon.broker.controller.dto.EmailSettings;
import uk.co.aegon.broker.controller.dto.MIEmailEvent;
import uk.co.aegon.broker.service.EmailBrokerService;

@RestController
@Slf4j
@Scope("prototype")
public class EmailBrokerController {
	
	@Autowired
	private EmailBrokerService service;

	@PostMapping("/email")
	public ResponseEntity<String> email(@RequestBody EmailSettings settings) {
		service.email(settings.isUseAegonLogo(), settings.getFrom(), settings.getSubject(), settings.getText(), settings.getTo());
		return ResponseEntity.ok("sent");
	}
	
	@PostMapping(path = "/miemail", consumes= { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<String> miemail(@RequestPart("event") MIEmailEvent event, @RequestPart(value="attachment",required=false) MultipartFile attachment){
		event.setAttachment(attachment);
		service.email(event);
		return ResponseEntity.ok("sent");
	}
	
	@GetMapping("/test")
	public ResponseEntity<String> test(){
		EmailSettings settings = new EmailSettings();
		settings.setFrom("philbarrmail@gmail.com");
		settings.setSubject("Test Email");
		settings.setText("<html><body><p>This is a test</p><image src=\"cid:aegonlogo.png\" /></body></html>");
		settings.setTo(new String[]{"phil.barr@aegon-service.co.uk"});
		settings.setUserId("USERID");
		service.email(true, settings.getFrom(), settings.getSubject(), settings.getText(), settings.getTo());
		return ResponseEntity.ok("sent");
	}
}
