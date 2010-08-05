package com.millamilla.tomcat.header.sso;

import java.util.List;

public interface RoleHeaderParser {
	public List<String> parseRoleHeader(String roleHeader);
}
