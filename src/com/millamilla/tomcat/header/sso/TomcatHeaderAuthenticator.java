package com.millamilla.tomcat.header.sso;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of HTTP BASIC
 * Authentication, as outlined in RFC 2617: "HTTP Authentication: Basic and
 * Digest Access Authentication."
 * 
 * @author Craig R. McClanahan
 * @version $Revision: 467222 $ $Date: 2006-10-24 05:17:11 +0200 (Tue, 24 Oct
 *          2006) $
 */

public class TomcatHeaderAuthenticator extends AuthenticatorBase {
    private static final String TOMCAT_HEADER_AUTH = "TOMCAT_HEADER_AUTH";
	private static final String ROLE_HEADER = "role-header";
	private static final String USERNAME_HEADER = "username-header";
	private static final String ROLE_HEADER_PARSING_CLASS_NAME = "com.millamilla.tomcat.header.sso.CommaRoleHeaderParser";
	private static final String USERNAME_HEADER_PARSING_CLASS_NAME = "com.millamilla.tomcat.header.sso.DefaultUsernameHeaderParser";

	private String roleHeaderName = ROLE_HEADER;
	private String usernameHeaderName = USERNAME_HEADER;
	private String roleHeaderParsingClassName = ROLE_HEADER_PARSING_CLASS_NAME;
	private String usernameHeaderParsingClassName = USERNAME_HEADER_PARSING_CLASS_NAME;

	private UsernameHeaderParser usernameHeaderParser = null;
	private RoleHeaderParser roleHeaderParser = null;

	public String getRoleHeaderParsingClassName() {
		return roleHeaderParsingClassName;
	}

	public void setRoleHeaderParsingClassName(String roleHeaderParsingClassName) {
		this.roleHeaderParsingClassName = roleHeaderParsingClassName;
	}

	public String getUsernameHeaderParsingClassName() {
		return usernameHeaderParsingClassName;
	}

	public void setUsernameHeaderParsingClassName(
			String usernameHeaderParsingClassName) {
		this.usernameHeaderParsingClassName = usernameHeaderParsingClassName;
	}

	public String getRoleHeaderName() {
		return roleHeaderName;
	}

	public void setRoleHeaderName(String groupHeaderName) {
		this.roleHeaderName = groupHeaderName;
	}

	public String getUsernameHeaderName() {
		return usernameHeaderName;
	}

	public void setUsernameHeaderName(String usernameHeaderName) {
		this.usernameHeaderName = usernameHeaderName;
	}

	private static Log log = LogFactory.getLog(TomcatHeaderAuthenticator.class);

	// ----------------------------------------------------- Instance Variables

	/**
	 * Descriptive information about this implementation.
	 */
	protected static final String info = "com.millamilla.tomcat.header.sso.TomcatHeaderAuthenticator";

	// ------------------------------------------------------------- Properties

	/**
	 * Return descriptive information about this Valve implementation.
	 */
	public String getInfo() {
		log.debug("getInfo");
		return (info);

	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Authenticate the user making this request, based on the specified login
	 * configuration. Return <code>true</code> if any specified constraint has
	 * been satisfied, or <code>false</code> if we have created a response
	 * challenge already.
	 * 
	 * @param request
	 *            Request we are processing
	 * @param response
	 *            Response we are creating
	 * @param config
	 *            Login configuration describing how authentication should be
	 *            performed
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	public boolean authenticate(Request request, Response response,
			LoginConfig config) throws IOException {
		log.debug("begin");

		// Have we already authenticated someone?
		Principal principal = request.getUserPrincipal();
		String ssoId = (String) request.getNote(org.apache.catalina.authenticator.Constants.REQ_SSOID_NOTE);
		if (principal != null) {
			log.debug("already-authenticated");
			if (log.isDebugEnabled())
				log
						.debug("Already authenticated '" + principal.getName()
								+ "'");
			// Associate the session with any existing SSO session
			if (ssoId != null)
				associate(ssoId, request.getSessionInternal(true));
			return (true);
		}

		// Is there an SSO session against which we can try to reauthenticate?
		if (ssoId != null) {
			log.debug("sso-reauthenticate");
			if (log.isDebugEnabled())
				log.debug("SSO Id " + ssoId + " set; attempting "
						+ "reauthentication");
			/*
			 * Try to reauthenticate using data cached by SSO. If this fails,
			 * either the original SSO logon was of DIGEST or SSL (which we
			 * can't reauthenticate ourselves because there is no cached
			 * username and password), or the realm denied the user's
			 * reauthentication for some reason. In either case we have to
			 * prompt the user for a logon
			 */
			if (reauthenticateFromSSO(ssoId, request))
				return true;
		}

		log.debug("usernameHeaderName: " + usernameHeaderName);
		MessageBytes usernameHeader = request.getCoyoteRequest()
				.getMimeHeaders().getValue(usernameHeaderName);
		log.debug("usernameHeader: " + usernameHeader);

		log.debug("roleHeaderName: " + roleHeaderName);
		MessageBytes roleHeader = request.getCoyoteRequest().getMimeHeaders()
				.getValue(roleHeaderName);
		log.debug("roleHeader: " + roleHeader);

		if ((usernameHeader != null) && (roleHeader != null)) {
			log.debug("brand-new-authentication");

			try {
				log.debug("usernameHeaderParserClassName: " + usernameHeaderParsingClassName);
				log.debug("roleHeaderParsingClassName: " + roleHeaderParsingClassName);
				if (usernameHeaderParser == null) {
					Class usernameHeaderParsingClass = Class
							.forName(usernameHeaderParsingClassName);
					usernameHeaderParser = (UsernameHeaderParser) usernameHeaderParsingClass
							.newInstance();
				}

				if (roleHeaderParser == null) {
					Class roleHeaderParsingClass = Class
							.forName(roleHeaderParsingClassName);
					roleHeaderParser = (RoleHeaderParser) roleHeaderParsingClass
							.newInstance();
				}

				List<String> parsedRoles = roleHeaderParser
						.parseRoleHeader(roleHeader.getString());
				// ArrayList<String> list = new ArrayList<String>();
				// String[] groupHeaders = groupHeader.getString().split(",");
				// for (int i=0; i<groupHeaders.length; i++) {
				// list.add(groupHeaders[i]);
				// }

				String parsedUsername = usernameHeaderParser
						.parseUsernameHeader(usernameHeader.getString());

				log.debug("parsedUsername: " + parsedUsername);
				log.debug("parsedRoles: " + parsedRoles);

				principal = new GenericPrincipal(context.getRealm(),
						parsedUsername, "no-password", parsedRoles);

				register(request, response, principal,
						TOMCAT_HEADER_AUTH, parsedUsername, "no-password");
				
				return (true);
			} catch (ClassNotFoundException e) {
				log.debug(e);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return (false);
			} catch (IllegalAccessException e) {
				log.debug(e);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return (false);
			} catch (InstantiationException e) {
				log.debug(e);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return (false);
			}
		} else {
			if (usernameHeader == null) {
				log.debug("missing username header value.");
			}
			if (roleHeader == null) {
				log.debug("missing role header value.");
			}
		}

		// We did not get authenticated...
		// Send an "unauthorized" response and an appropriate challenge
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		// response.flushBuffer();
		return (false);

	}

}
