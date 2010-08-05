package com.millamilla.tomcat.header.sso;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class DefaultUsernameHeaderParser implements UsernameHeaderParser {

	private static Log log = LogFactory
			.getLog(DefaultUsernameHeaderParser.class);

	public String parseUsernameHeader(String usernameHeader) {
		return usernameHeader;
	}

}
