TomcatHeaderSso

8.7.2010

Currently these instructions only list Tomcat 6.  Tomcat 5/5.5 should work as the code has been compiled with JDK 5.
Tomcat 5/5.5 just has not been tested yet.

The purpose of this authentication valve is to accept headers from a reverse proxy/front-end web server (rp/fews).  In this 
architecture, the rp/fews handles authentication and passes headers for the username and roles of the user.  
This takes the burden of managing authentication away from Tomcat.  Tomcat is still handling authorization as
it applies the security constraints based on the values passed in the headers.  This allows you to use:
	Declarative J2EE security via security-constraint tags in your application's web.xml file 
	request.isUserInRole("role name to check")
	request.getRemoteUser()
	request.getUserPrincipal()

As with all security solutions, there are risks.  If users have access to making requests directly to your Tomcat 
servers, understand how to pass header values, know the names of the headers that are being used, know the 
names of the roles, know what each role has access to (or at least the most trusted ones), and know the format 
to list the roles by, the user can bypass authentication and pass credentials that Tomcat will accept without 
challenge.

There are ways to mitigate this risk.  Here are some, but not the only, examples:
1. You can configure Tomcat to only accept requests from specific IP addresses.  For example you only accept
requests from the rp/fews.

2. Encrypt the header values as long as the rp/fews understands how to encrypt the values.  This 
authentication valve allows you to supply classes that parse the username and roles.  Your classes receive the 
text value of the header and your return a string value for the username or a list of string values for the roles.

3. Control access to the Tomcat server via network configuration.  Using a Demilitarized Zone (DMZ) that seperates 
the rp/fews from the Tomcat Servers and seperates the Tomcat servers from your internal network.
Internet -> Firewall -> rp/fews -> External DMZ -> Tomcat Server(s) -> Data DMZ -> Internal network


How to use
==========

*Under Tomcat 6
1. Modify the servers conf/context.xml by adding the following:
     <Valve className="com.millamilla.tomcat.header.sso.TomcatHeaderAuthenticator" 
		roleHeaderName="role-header-name" 
		usernameHeaderName="username-header-name" 
		roleHeaderParsingClassName="com.millamilla.tomcat.header.sso.CommaRoleHeaderParser" 
		usernameHeaderParsingClassName="com.millamilla.tomcat.header.sso.DefaultUsernameHeaderParser"/>
		
2.  This configuration assume the username will be found in the header called "username-header-name".  The 
parsing class for the username will just return the value of this header.

3.  This configuration assumes the list of roles will be comma separated and will be contained in the header 
called "role-header-name".


How to test
============

*Under Tomcat 6
You can test before putting the Tomcat server behind your Apache web server, other web server, or load balancer 
that normally passes the headers to Tomcat via the following:

1. This assumes a default install of Tomcat, including the /manager app with the TomcatHeaderAuthentication 
installed as instructed above.
2. Running "curl http://localhost:8080/manager/html" will give you an error "401 Unauthorized"
3. Running "curl -H role-header-name:manager -H username-header-name:tomcat http://localhost:8080/manager/html" 
will not return an error.  It will return "Server Information"
	a. If you receive an error here, let's verify your Tomcat manager application configuration
	b. Look in \webapps\manager\WEB-INF\web.xml
	c. The security-constraint element for /html/ ( and others ) lists a role-name.  In the example above,
	the role is assumed to be manager.  If the role you see in the security-constraint is different, please
	change the curl command to reflect the role name in your security constraint.
  <security-constraint>
    <web-resource-collection>
      <web-resource-name>HTMLManger and Manager command</web-resource-name>
      <url-pattern>/jmxproxy/*</url-pattern>
      <url-pattern>/html/*</url-pattern>
      <url-pattern>/list</url-pattern>
      <url-pattern>/expire</url-pattern>
      <url-pattern>/sessions</url-pattern>
      <url-pattern>/start</url-pattern>
      <url-pattern>/stop</url-pattern>
      <url-pattern>/install</url-pattern>
      <url-pattern>/remove</url-pattern>
      <url-pattern>/deploy</url-pattern>
      <url-pattern>/undeploy</url-pattern>
      <url-pattern>/reload</url-pattern>
      <url-pattern>/save</url-pattern>
      <url-pattern>/serverinfo</url-pattern>
      <url-pattern>/status/*</url-pattern>
      <url-pattern>/roles</url-pattern>
      <url-pattern>/resources</url-pattern>
      <url-pattern>/findleaks</url-pattern>
    </web-resource-collection>
    <auth-constraint>
       <!-- NOTE:  This role is not present in the default users file -->
       <role-name>manager</role-name>
    </auth-constraint>
  </security-constraint>


How to debug
============

*Under Tomcat 6
1. Modify the conf/logging.properties file by adding the following:
com.millamilla.tomcat.header.sso.level = ALL
com.millamilla.tomcat.header.sso.handlers = java.util.logging.ConsoleHandler

2. Start Tomcat using the usual/bin/startup.bat or /bin/startup.sh This will add output to the console window.