package uk.co.aegon.broker.controller.dto;

import lombok.Data;

@Data
public class EmailSettings {
	private String userId;
	private String text;
	private String[] to;
	private String from;
	private String subject;
	private boolean useAegonLogo;
}
