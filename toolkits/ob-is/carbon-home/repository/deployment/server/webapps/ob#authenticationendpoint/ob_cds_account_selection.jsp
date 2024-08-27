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

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="includes/consent_top.jsp"/>
<%@ page import ="javax.servlet.RequestDispatcher"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String preSelectedProfileId = (String) request.getAttribute("preSelectedProfileId");
    String selectedProfileId = (preSelectedProfileId == null || "".equals(preSelectedProfileId)) ?
            request.getParameter("selectedProfileId") : preSelectedProfileId;
    boolean isConsentAmendment = request.getAttribute("isConsentAmendment") != null ?
            (boolean) request.getAttribute("isConsentAmendment") : false;
    Object nameClaims = request.getAttribute("nameClaims");
    String nameClaimsString = nameClaims != null ? (String) nameClaims : "";
    session.setAttribute("nameClaims", nameClaimsString);
    Object contactClaims = request.getAttribute("contactClaims");
    String contactClaimsString = contactClaims != null ? (String) contactClaims : "";
    session.setAttribute("contactClaims", contactClaimsString);

    if (session.getAttribute("profiles_data") == null || isConsentAmendment) {
        session.setAttribute("profiles_data", request.getAttribute("profiles_data"));
    }
    if (session.getAttribute("configParamsMap") == null || isConsentAmendment) {
        session.setAttribute("configParamsMap", request.getAttribute("data_requested"));
    }
    if (session.getAttribute("newConfigParamsMap") == null || isConsentAmendment) {
        session.setAttribute("newConfigParamsMap", request.getAttribute("new_data_requested"));
    }
    if (session.getAttribute("business_data_cluster") == null || isConsentAmendment) {
        session.setAttribute("business_data_cluster", request.getAttribute("business_data_cluster"));
    }
    if (session.getAttribute("new_business_data_cluster") == null || isConsentAmendment) {
        session.setAttribute("new_business_data_cluster", request.getAttribute("new_business_data_cluster"));
    }
    if (session.getAttribute("skipAccounts") == null || isConsentAmendment) {
        session.setAttribute("skipAccounts", request.getAttribute("customerScopesOnly"));
    }
    
    boolean skipAccounts = (boolean) session.getAttribute("skipAccounts");
    if (skipAccounts) {
    	RequestDispatcher requestDispatcher = request.getRequestDispatcher("/oauth2_authz_consent.do");
        request.setAttribute("sessionDataKeyConsent", Encode.forHtmlAttribute(
                String.valueOf(session.getAttribute("sessionDataKeyConsent"))));
        request.setAttribute("isConsentAmendment", isConsentAmendment);
        Object isSharingDurationUpdated = request.getAttribute("isSharingDurationUpdated") != null ?
                request.getAttribute("isSharingDurationUpdated") : session.getAttribute("isSharingDurationUpdated");
        request.setAttribute("isSharingDurationUpdated", isSharingDurationUpdated);
        request.setAttribute("accountsArry[]", "unavailable");
        request.setAttribute("accNames", "");
        Object app = request.getAttribute("app") != null ? request.getAttribute("app") : session.getAttribute("app");
        request.setAttribute("app", app);
        Object spFullName = request.getAttribute("sp_full_name") != null ?
                request.getAttribute("sp_full_name") : session.getAttribute("sp_full_name");
        request.setAttribute("spFullName", spFullName);
        request.setAttribute("selectedProfileId", selectedProfileId);
        request.setAttribute("selectedProfileName", session.getAttribute("selectedProfileName"));
        Object consentExpiryDateTime = request.getAttribute("consent_expiration") != null ?
                request.getAttribute("consent_expiration") : session.getAttribute("consent_expiration");
        request.setAttribute("consent-expiry-date", consentExpiryDateTime);
        request.setAttribute("sharing_duration_value", session.getAttribute("sharing_duration_value"));
        requestDispatcher.forward(request, response);
    }
%>

<div class="row data-container">
    <div class="clearfix"></div>
    <form action="${pageContext.request.contextPath}/account_selection_confirm.do" method="post" id="oauth2_authz_consent"
          name="account_selection_confirm"
          class="form-horizontal">
        <div class="login-form">
            <div class="form-group ui form">
                <div class="col-md-12 ui box">
                    <h3 class="ui header"><strong>${sp_full_name}
                    </strong> requests account details on your account.
                    </h3>
                </div>
            </div>

            <div>
                <c:if test="${not empty accounts_data}">
                    <%--Get account ids for the selected profile--%>
                    <c:set var="selectedProfileId" scope="session" value="<%=selectedProfileId%>"/>
                    <c:forEach items="${profiles_data}" var="profile">
                        <c:if test="${profile['profileId'] eq selectedProfileId}">
                            <c:set var="profileAccountIds" value="${profile['accountIds']}" />
                            <c:set var="selectedProfileName" value="${profile['profileName']}" />
                        </c:if>
                    </c:forEach>
                    <div class="form-group ui form select">
                        <h4 class="ui body col-md-12">
                            Select the accounts you wish to authorise:
                        </h4>
                        <div class="col-md-12" >
                            <%--Display Selectable accounts--%>
                            <tr class="col-md-12" ><td colspan=2><br><button type="button" style='margin: 0; padding: 0;border: none;color: #00b4ff;background-color: transparent;text-decoration: underline;'
                                onClick='toggle(this)'>Select all </button></td></tr>
                            <tr ><td colspan=2></td><br></tr>
                            <c:forEach items="${accounts_data}" var="record">
                                <c:if test="${fn:contains(profileAccountIds, record['accountId'])}">
                                    <c:choose>
                                        <c:when test="${record['is_selectable']}">
                                            <label for="${record['displayName']}">
                                                <input type="checkbox" id="${record['displayName']}" name="chkAccounts" data-displayName="${record['accountIdToDisplay']}"
                                                value="${record['accountId']}" onclick="updateCDSAcc()"
                                                    ${record['isPreSelectedAccount'] ? 'checked' : ''}
                                                />
                                                    ${record['displayName']}
                                            </label>

                                            <span id="joint-accounts-info">
                                                <c:if test="${record['is_joint_account'] eq true && record['is_secondary_account'] ne true}">
                                                    <p class="hide data-container" id="selectablePopoverContent" style='color: rgb(0, 0, 0); text-align: left'> ${record['linked_members_count']} other account holder(s) can share this joint account data at any time,
                                                        without each other&lsquo;s permission. <br/><br/> You can change sharing preferences for this account by going to &lsquo;Settings &gt;Data sharing &gt; Account permissions&rsquo;</p>
                                                    <a class="selectablePopoverContentElement">&#9432;</a>
                                                </c:if>
                                            </span>

                                            <div class="accountIdClass" id="${record['accountIdToDisplay']}">
                                                <small>${record['accountIdToDisplay']}</small>
                                            </div><br/>
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="form-group ui form select">
                        <hr class="separator">
                        <h4 class="ui body col-md-12">
                            Accounts Unavailable To Share:
                        </h4>
                        <div class="col-md-12 padding-bottom-double">
                            <p id="UnavailableAccountPopover" class="hide" style='text-align: left'> There are a range of reasons why certain accounts may not available to share. Please call the bank for more details.
                                <br><br> &#8211; For joint accounts, all account holders must elect to make the account available for sharing. <br> &#8211; For secondary user accounts, the account holder(s) must give you secondary user data sharing rights before you can share data from this account.
                                <br><br>These can be done via the Data Sharing dashboard in Internet Banking or the app. </p>
                            <p>Why can't I share these? <a id="unavailablePopoverContentElement">&#9432;</a></p>
                        </div>
                        <br><br>
                        <div class="col-md-12" >
                                <%--Display Unavailable accounts--%>
                            <c:forEach items="${accounts_data}" var="record">
                                <c:if test="${fn:contains(profileAccountIds, record['accountId']) && !record['is_selectable']}">
                                    <label for="${record['displayName']}">
                                            ${record['displayName']}
                                    </label>
                                    <div class="accountIdClass" id="${record['accountIdToDisplay']}">
                                        <small>${record['accountIdToDisplay']}</small>
                                    </div><br/>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="form-group ui form row">
                <div class="ui body col-md-12">
                    <input type="button" class="ui default column button btn btn-default" id="cancel" name="cancel"
                           onclick="showModal()" checked data-toggle="modal" data-target="#cancelModel" value="Cancel"/>
                    <input type="button" class="ui primary button column btn" id="approve" name="confirm account"
                           onclick="approvedAcc(); return false;" value="Approve"/>
                    <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false"/>
                    <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
                    <input type="hidden" name="consent" id="consent" value="deny"/>
                    <input type="hidden" name="app" id="app" value="${app}"/>
                    <input type="hidden" name="spFullName" id="app" value="${sp_full_name}"/>
                    <input type="hidden" name="accountsArry[]" id="account" value=""/>
                    <input type="hidden" name="accNames" id="accountName" value=""/>
                    <input type="hidden" name="accDisplayNames" id="accountDisplayName" value=""/>
                    <input type="hidden" name="type" id="type" value="accounts"/>
                    <input type="hidden" name="consent-expiry-date" id="consentExp" value="${consent_expiration}"/>
                    <input type="hidden" name="isConsentAmendment" id="isConsentAmendment" value="${isConsentAmendment}"/>
                    <input type="hidden" name="isSharingDurationUpdated" id="isSharingDurationUpdated" value="${isSharingDurationUpdated}"/>
                    <input type="hidden" name="selectedProfileId" id="selectedProfileId" value="<%=selectedProfileId%>"/>
                    <input type="hidden" name="selectedProfileName" id="selectedProfileName" value="${selectedProfileName}"/>
                    <input type="hidden" name="sharing_duration_value" id="sharing_duration_value" value="${sharing_duration_value}"/>
                </div>
            </div>

            <div class="form-group ui form row">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                    <div class="well policy-info-message" role="alert margin-top-5x">
                        <div>
                            ${privacyDescription}
                            <a href="privacy_policy.do" target="policy-pane">
                                ${privacyGeneral}
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

<!-- Cancel Modal -->
<div class="modal" id="cancelModal">
    <div class="modal-dialog">
        <div class="modal-content">

            <!-- Modal body -->
            <div class="modal-body">
                <p style="color:black"> Unless you confirm your authorisation, we won't be able to share your data with
                    "${sp_full_name}". <br>
                    <br> Are you sure you would like to cancel this process? </p>

                <div class="ui two column grid">
                    <table style="width:100%">
                        <tbody>
                        <tr>
                            <td>
                                <div class="md-col-6 column align-left buttons">
                                    <input type="button" onclick="redirect()" class="ui default column button btn btn-default"
                                           id="registerLink" role="button" value="Yes cancel">
                                </div>
                            </td>
                            <td>
                                <div class="column align-right buttons">
                                    <input type="button" onclick="closeModal()" class="ui primary column button btn" role="button"
                                           value="No continue" style="float:right;">
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</div>

<script>

    let modal = document.getElementById("cancelModal");

    function showModal() {
        modal.style.display = "block";
    }

    function closeModal() {
        modal.style.display = "none";
    }

    function redirect() {
        let error = "User skip the consent flow";
        let state = "${state}"
        if (state) {
            top.location = "${redirectURL}#error=access_denied&error_description=" + error +
                "&state=" + state;
        } else {
            top.location = "${redirectURL}#error=access_denied&error_description=" + error;
        }
    }

    window.onclick = function (event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }

    $(document).ready(function(){
        updateCDSAcc();
        const popoverTemplate = ['<div class="popover" role="tooltip">',
            '<div class="arrow"></div>',
            '<h6 class="popover-title"></h6>',
            '<div class="popover-content">',
            '</div>',
            '</div>'].join('');

        $(".selectablePopoverContentElement").popover({
            placement: 'right',
            title: '&check; Pre-approval enabled',
            content: $("#selectablePopoverContent").html(),
            trigger: 'hover focus',
            html: true,
            template: popoverTemplate
        });

        $("#unavailablePopoverContentElement").popover({
            placement: 'right',
            title: 'Unavailable Accounts',
            content: $("#UnavailableAccountPopover").html(),
            trigger: 'hover focus',
            html: true,
            template: popoverTemplate
        });

    });

    function toggle(source) {
        var items = document.getElementsByName('chkAccounts');
        for (var i = 0; i < items.length; i++) {
            if (items[i].type == 'checkbox') {
                items[i].checked = true;
            }
        }
        updateCDSAcc();
    }

    function updateCDSAcc() {
        updateAcc();
        var accDisplayNames = "";
        $("input:checkbox[name=chkAccounts]:checked").each(function(){
            accDisplayNames = accDisplayNames.concat(":", $(this).attr("data-displayName"));
        });
        accDisplayNames = accDisplayNames.replace(/^\:/, '');
        document.getElementById('accountDisplayName').value = accDisplayNames;
    }

</script>

<jsp:include page="includes/consent_bottom.jsp"/>
