package com.millamilla.tomcat.header.sso;

import java.util.ArrayList;
import java.util.List;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class CommaRoleHeaderParser implements RoleHeaderParser {
	private static Log log = LogFactory.getLog(CommaRoleHeaderParser.class);

	public List parseRoleHeader(String roleHeader) {
		ArrayList<String> result = new ArrayList<String>();
		if (roleHeader != null) {
			String[] roleHeaders = roleHeader.split(",");
			for (int i = 0; i < roleHeaders.length; i++) {
				result.add(roleHeaders[i]);
			}
		}

		return result;
	}

}
