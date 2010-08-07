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
parsing class for the uesrname will just return the value of this header.

3.  This configuration assumes the list of roles will be comma separated and will be contained in the header 
called "role-header-name".


How to debug
============

*Under Tomcat 6
1. Modify the conf/logging.properties file by adding the following:
com.millamilla.tomcat.header.sso.level = ALL
com.millamilla.tomcat.header.sso.handlers = java.util.logging.ConsoleHandler

