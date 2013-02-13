package com.recomdata.security

import com.recomdata.transmart.domain.searchapp.AccessLog
import javax.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService

 class HeaderAuthenticationFilter  
    extends AbstractPreAuthenticatedProcessingFilter { 
  protected final Logger log = Logger.getLogger(HeaderAuthenticationFilter.class); 
  def principalRequestHeader  
  def springSecurityService
  def authenticationDetailsService
  def redirectStrategy
  def authenticationManger
  /** 
   * Configure a value in the applicationContext-security for local tests. 
   */
  private String testUserId = null; 
  /** 
   * Configure whether a missing SSO header is an exception. 
   */
  private boolean exceptionIfHeaderMissing = false; 
  def String baseUrl
  
  /** 
   * Read and return header named by <TT>principalRequestHeader</TT> from Request 
   *  
   * @throws PreAuthenticatedCredentialsNotFoundException 
   *             if the header is missing and 
   *             <TT>exceptionIfHeaderMissing</TT> is set to <TT>true</TT>. 
   */
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) { 
	String baseURL=request.getRequestURL().toString()
	if (baseURL.contains(".com:"))
	{
		String[] removePort=baseURL.split(":")
		String[] tail=removePort[3].split("/")
		StringBuffer newUrl=new StringBuffer()
		newURL.append(removePort[0])
		newURL.append(":")
		newURL.append(removePort[1])
		newURL.append("/")
		for (int i=0;i<tail.length();i++)
		{
			newURL.append(tail[i])
			newURL.append("/")
		}
		log.debug("new url" +newURL);
	}
    String principal = request.getHeader(principalRequestHeader); 
	log.info("Principal user is:" +principal)
    if (principal == null) { 
      if (exceptionIfHeaderMissing) {
        throw new PreAuthenticatedCredentialsNotFoundException(principalRequestHeader 
            + " header not found in request."); 
      }
	  else
	  {
		  		msg = SpringSecurityUtils.securityConfig.errors.login.fail
				new AccessLog(username: username, event:"Login Failed",
					eventmessage: msg,
					accesstime:new Date()).save()
					redirectStrategy.sendRedirect(request, response, baseUrl)
	  }
    } 
    // also set it into the session, sometimes that's easier for jsp/faces 
    // to get at.. 
    request.getSession().setAttribute("session_user", principal); 
/*	if (!springSecurityService.isLoggedIn())
	{log.info("Setting user logged in status.");
	springSecurityService.set
	}*/
    return principal; 
  } 
  
  /** 
   * Credentials aren't applicable here for OAM WebGate SSO. 
   */
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) { 
    return "password_not_applicable"; 
  } 
  
  def setPrincipalRequestHeader(String principalRequestHeader) { 
    Assert.hasText(principalRequestHeader, "principalRequestHeader must not be empty or null"); 
    this.principalRequestHeader = principalRequestHeader; 
  } 
  
  def setTestUserId(String testId) { 
    if (StringUtils.isNotBlank(testId)) { 
      this.testUserId = testId; 
    } 
  } 
  
  /** 
   * Exception if the principal header is missing. Default <TT>false</TT> 
   * @param exceptionIfHeaderMissing 
   */
  def setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) { 
    this.exceptionIfHeaderMissing = exceptionIfHeaderMissing; 
  } 
  
  def setAuthenticationDetailsSource(GrailsUserDetailsService source) { 
    log.info("testing authenticationDetailsSource set " + source); 
    super.setAuthenticationDetailsSource(source); 
  } 
  def setAuthenticatManager(AuthenticationManager mgr)
  {
	  log.info("Set Authentication");
	  super.setAuthenticationManager(mgr)
  }
  def setBaseUrl(String url)
  {
	 this.baseUrl=url;
  }
}