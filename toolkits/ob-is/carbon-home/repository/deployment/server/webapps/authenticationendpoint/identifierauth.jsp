<%--
 ~ Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 ~
 ~ WSO2 LLC. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --%>

<%@ page import="org.apache.cxf.jaxrs.client.JAXRSClientFactory" %>
<%@ page import="org.apache.cxf.jaxrs.provider.json.JSONProvider" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.client.SelfUserRegistrationResource" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.ResendCodeRequestDTO" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO" %>
<%@ page import="com.wso2.openbanking.accelerator.identity.authenticator.OBIdentifierAuthenticator" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isSelfSignUpEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isRecoveryEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.getServerURL" %>
<%@ page import="org.wso2.carbon.identity.core.URLBuilderException" %>
<%@ page import="org.wso2.carbon.identity.core.ServiceURLBuilder" %>
<%@ page import="org.json.JSONObject" %>

<jsp:directive.include file="includes/init-loginform-action-url.jsp"/>

<%
     boolean disableInput = false;
     String callbackURL = "retry.do";

     File extensionFile = new File(getServletContext().getRealPath("extensions/toolkit-data-extension.jsp"));
     if (extensionFile.exists()) { 
        %>
	    <jsp:include page="extensions/toolkit-data-extension.jsp"/>
	    <%
        callbackURL = request.getAttribute("callbackURL").toString(); 
     }
%>

<script>
    function submitIdentifier () {
        var userName = document.getElementById("username");
        var usernameUserInput = document.getElementById("usernameUserInput");

        if (usernameUserInput) {
            userName.value = usernameUserInput.value.trim();
        }

        if (username.value) {
            document.getElementById("identifierForm").submit();
        }
    }
</script>

<form class="ui large form" action="<%=loginFormActionURL%>" method="post" id="identifierForm">
    <%
        if (loginFormActionURL.equals(samlssoURL) || loginFormActionURL.equals(oauth2AuthorizeURL)) {
    %>
    <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">
    <%
        }
    %>
    <% if (Boolean.parseBoolean(loginFailed)) { %>
    <div class="ui visible negative message" id="error-msg">
        <%

        String error = "Something has gone wrong. Please try again.";

        if (errorMessage.equals("Too.many.attempts")) {

	     File errorFile = new File(getServletContext().getRealPath("extensions/toolkit-error.jsp"));
	        if (errorFile.exists()) {
	            disableInput = true;
	    %>
               <jsp:include page="extensions/toolkit-error.jsp"/>
		
	    <% } else {
               error = "Maximum retry limit reached. Please wait for a while until next attempt. " +
                       "In case of an incorrect Customer ID, please visit Internet Banking to " +
                       "retrieve your Customer ID or contact the bank.";
               disableInput = true;
               callbackURL = "retry.do";
               response.sendRedirect(callbackURL + "?status=Access Denied&statusMsg=" + error);
             }
        } else if (errorMessage.equals("Login.failed")) {
            error = "These Details are incorrect. Please try again.";

        } else if (errorMessage.equals("session.expired")) {
            disableInput = true;
        }
        out.println(error);
        %>
    </div>
    <% } else if ((Boolean.TRUE.toString()).equals(request.getParameter("authz_failure"))) { %>
    <div class="ui visible negative message" id="error-msg">
        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "unauthorized.to.login")%>
    </div>
    <% } else { %>
        <div class="ui visible negative message" style="display: none;" id="error-msg"></div>
    <% } %>

    <div class="field">
        <div class="ui fluid left icon input">
            <input
                type="text"
                id="usernameUserInput"
                value=""
                name="usernameUserInput"
                tabindex="0"
                placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "username")%>"
                required />
            <i aria-hidden="true" class="user icon"></i>
        </div>
        <input id="username" name="username" type="hidden" value="">
        <input id="authType" name="authType" type="hidden" value="idf">
    </div>
    <%
        if (reCaptchaEnabled) {
    %>
    <div class="field">
        <div class="g-recaptcha"
             data-sitekey="<%=Encode.forHtmlContent(request.getParameter("reCaptchaKey"))%>">
        </div>
    </div>
    <%
        }
    %>
    
    <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>

    <%
        String recoveryEPAvailable = application.getInitParameter("EnableRecoveryEndpoint");
        String enableSelfSignUpEndpoint = application.getInitParameter("EnableSelfSignUpEndpoint");
        Boolean isRecoveryEPAvailable = false;
        Boolean isSelfSignUpEPAvailable = false;
        String identityMgtEndpointContext = "";
        String urlEncodedURL = "";
        String urlParameters = "";

        if (StringUtils.isNotBlank(recoveryEPAvailable)) {
            isRecoveryEPAvailable = Boolean.valueOf(recoveryEPAvailable);
        } else {
            isRecoveryEPAvailable = isRecoveryEPAvailable();
        }

        if (StringUtils.isNotBlank(enableSelfSignUpEndpoint)) {
            isSelfSignUpEPAvailable = Boolean.valueOf(enableSelfSignUpEndpoint);
        } else {
            isSelfSignUpEPAvailable = isSelfSignUpEPAvailable();
        }

        if (isRecoveryEPAvailable || isSelfSignUpEPAvailable) {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String uri = (String) request.getAttribute(JAVAX_SERVLET_FORWARD_REQUEST_URI);
            String prmstr = (String) request.getAttribute(JAVAX_SERVLET_FORWARD_QUERY_STRING);
            String urlWithoutEncoding = scheme + "://" +serverName + ":" + serverPort + uri + "?" + prmstr;

            urlEncodedURL = URLEncoder.encode(urlWithoutEncoding, UTF_8);
            urlParameters = prmstr;

            identityMgtEndpointContext = application.getInitParameter("IdentityManagementEndpointContextURL");
            if (StringUtils.isBlank(identityMgtEndpointContext)) {
                try {
                    identityMgtEndpointContext = ServiceURLBuilder.create().addPath(ACCOUNT_RECOVERY_ENDPOINT).build()
                            .getAbsolutePublicURL();
                } catch (URLBuilderException e) {
                    request.setAttribute(STATUS, AuthenticationEndpointUtil.i18n(resourceBundle, CONFIGURATION_ERROR));
                    request.setAttribute(STATUS_MSG, AuthenticationEndpointUtil
                            .i18n(resourceBundle, ERROR_WHILE_BUILDING_THE_ACCOUNT_RECOVERY_ENDPOINT_URL));
                    request.getRequestDispatcher("error.do").forward(request, response);
                    return;
                }
            }
        }
    %>

    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
	    <%
            File usernameValidationFile = new File(getServletContext().getRealPath("extensions/toolkit-username-validation.jsp"));
            if (usernameValidationFile.exists()) {
        %>
        <jsp:include page="extensions/toolkit-username-validation.jsp"/>
        <% } else { %>
        <div class="field disclaimer" style="font-size:0.9em">
            Forgot Customer ID? Go to the WSO2 Open Banking website to retrieve it.
        </div>
        <% } %>
    </div>
    <div class="ui two column grid">
	<div class="column align-left buttons">
	    <% if (isRecoveryEPAvailable) { %>
	    <input
		type="button"
		onclick="showModal()"
		class="ui large button link-button"
		id="cancel"
		role="button"
		data-toggle="modal"
		data-target="#cancelModel"
		value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "cancel")%>" />
	    <% } %>
	</div>
	<div class="column align-right buttons">
	    <input
		type="submit"
		onclick="submitIdentifier()"
		class="ui primary large button"
		role="button"
		id="submitBtn"
		disabled=true
		value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "next")%>" />
	</div>
    </div>
    
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
       <hr class="separator">
       <%
           File disclaimerFile = new File(getServletContext().getRealPath("extensions/toolkit-disclaimer.jsp"));
           if (disclaimerFile.exists()) {
       %>
               <jsp:include page="extensions/toolkit-disclaimer.jsp"/>
       <% } else { %>
               <div class="form-actions disclaimer" style="font-size:small">
                 Your Customer ID will not be shared with any third party. One time passwords are used
                 to share banking data. You will never be asked to provide your real password to share banking data.
              </div>
      <% } %>
    </div>
</form>

<!-- The Modal -->
<div class="modal" id="cancelModal">
  <div class="modal-dialog">
    <div class="modal-content">

      <!-- Modal body -->
      <div class="modal-body">
        <%
            File consentSkipFile = new File(getServletContext().getRealPath("extensions/toolkit-consent-skip.jsp"));
            if (consentSkipFile.exists()) {
        %>
                <jsp:include page="extensions/toolkit-consent-skip.jsp"/>
        <% } else { %>
                <p style="color:black"> Unless you confirm your authorisation, we won't be able to share your data with
                any third party. <br> <br> Are you sure you would like to cancel this process? </p>
	<% } %>
        <div class="ui two column grid">
            <div class="column align-left buttons">
                <input
                    type="button"
                    onclick="redirect()"
                    class="ui danger button"
                    id="registerLink"
                    role="button"
                    value="Yes cancel" />
            </div>
            <div class="column align-right buttons">
                <input
                    type="button"
                    onclick="closeModal()"
                    class="ui primary button"
                    role="button"
                    value="No continue" />
            </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>

    var modal = document.getElementById("cancelModal");

    function showModal() {
        modal.style.display = "block";
    }

    function closeModal() {
        modal.style.display = "none";
    }

    function redirect() {
        let error = "User skipped the consent flow";
        let state = "${state}"
        if (state) {
            top.location = "<%=callbackURL%>?status=Access Denied&statusMsg=" + error +
                "&state=" + state;
        } else {
            top.location = "<%=callbackURL%>?status=Access Denied&statusMsg=" + error;
        }
    }

    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }

    // enable "next" btn until the input count equal to 8
    document.getElementById('usernameUserInput').addEventListener('input', function () {
        var text = this.value,
        count = text.trim().length;
        if (count >= 8) {
            document.getElementById("submitBtn").disabled = false;
        }

    });

    $(document).ready(function(){
        if(<%=disableInput%>) {
            document.getElementById("usernameUserInput").disabled = true;
        }
    });
</script>
