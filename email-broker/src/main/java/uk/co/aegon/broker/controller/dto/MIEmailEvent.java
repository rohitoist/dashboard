package uk.co.aegon.broker.controller.dto;

import java.io.Serializable;

import org.springframework.core.io.InputStreamSource;

import lombok.Data;

@Data
public class MIEmailEvent implements Serializable{
	private static final long serialVersionUID = 2404806364409840694L;
	private String fromAddress;
	private String subject;
	private String[] toAddresses;
	private String[] carbonCopy;
	private String[] blindCopy;
	private InputStreamSource attachment;
	private String attachmentName;
	private String content;
}
