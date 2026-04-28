<%--
~ Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% response.setCharacterEncoding("UTF-8"); %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%!
    private Object resolveFromAccount(Map account, String key) {
        return account.get(key);
    }

    private Object resolveFromAdditionalProperties(Map account, String key) {
        Object additionalPropertiesObj = account.get("additionalProperties");
        if (!(additionalPropertiesObj instanceof Map)) {
            return null;
        }

        Map additionalProperties = (Map) additionalPropertiesObj;
        Object value = additionalProperties.get(key);
        if (value != null) {
            return value;
        }

        Object nestedValueObj = additionalProperties.get("value");
        if (nestedValueObj instanceof Map) {
            return ((Map) nestedValueObj).get(key);
        }

        return null;
    }

    private String resolveProfileId(Object accountObj) {
        if (accountObj instanceof Map) {
            Map account = (Map) accountObj;
            Object value = resolveFromAdditionalProperties(account, "profileId");
            if (value == null || String.valueOf(value).trim().isEmpty()) {
                value = account.get("profileId");
            }
            if (value == null || String.valueOf(value).trim().isEmpty()) {
                return "individual";
            }
            return String.valueOf(value);
        }
        return "individual";
    }

    private String resolveProfileName(Object accountObj, String profileId) {
        if (accountObj instanceof Map) {
            Map account = (Map) accountObj;
            Object value = resolveFromAdditionalProperties(account, "profileName");
            if (value == null || String.valueOf(value).trim().isEmpty()) {
                value = account.get("profileName");
            }
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value);
            }
        }

        if ("individual".equals(profileId)) {
            return "Individual";
        }

        return profileId;
    }
%>

<%
    String[] keysToPromoteToSession = {
            "initiatedAccountsForConsent", "selectAccounts", "reAuthenticationDisclaimer", "dataRequested",
            "buttonGoBack", "defaultSelect", "buttonDeny", "consumerAccounts", "type",
            "basicConsentData", "hasMultiplePermissions", "textDirection", "ifStopDataSharing", "permissions",
            "allowMultipleAccounts", "appRequestsDetails", "requestedPermissions", "noConsumerAccounts",
            "isReauthorization", "buttonNext", "doYouConfirm", "handleAccountSelectionSeparately",
            "onFollowingAccounts", "buttonConfirm", "additionalData", "privacyDescription",
            "privacyGeneral", "redirectURL", "state"
    };

    for (String key : keysToPromoteToSession) {
        Object value = request.getAttribute(key);
        if (value != null) {
            session.setAttribute(key, value);
        }
    }

    String selectedProfileId = request.getParameter("selectedProfileId");
    String selectedProfileName = request.getParameter("selectedProfileName");
    boolean isProfileSubmission = "POST".equalsIgnoreCase(request.getMethod())
            && selectedProfileId != null && !selectedProfileId.isEmpty();

    Object allConsumerAccountsObj = session.getAttribute("consumerAccounts");
    if (!(allConsumerAccountsObj instanceof List)) {
        allConsumerAccountsObj = request.getAttribute("consumerAccounts");
    }

    if (!isProfileSubmission && allConsumerAccountsObj instanceof List) {
        List allConsumerAccounts = (List) allConsumerAccountsObj;
        Set uniqueProfileIds = new LinkedHashSet();
        Map profileNamesById = new LinkedHashMap();

        for (Object accountObj : allConsumerAccounts) {
            String accountProfileId = resolveProfileId(accountObj);
            uniqueProfileIds.add(accountProfileId);

            if (!profileNamesById.containsKey(accountProfileId)) {
                profileNamesById.put(accountProfileId, resolveProfileName(accountObj, accountProfileId));
            }
        }

        if (uniqueProfileIds.size() == 1) {
            selectedProfileId = String.valueOf(uniqueProfileIds.iterator().next());
            Object autoSelectedProfileName = profileNamesById.get(selectedProfileId);
            selectedProfileName = autoSelectedProfileName == null ? null : String.valueOf(autoSelectedProfileName);
            isProfileSubmission = true;
        }
    }

    if (isProfileSubmission) {
        session.setAttribute("selectedProfileId", selectedProfileId);
        if (selectedProfileName != null) {
            session.setAttribute("selectedProfileName", selectedProfileName);
        }
        session.setAttribute("handleAccountSelectionSeparately", true);

        List filteredConsumerAccounts = new ArrayList();

        if (allConsumerAccountsObj instanceof List) {
            List allConsumerAccounts = (List) allConsumerAccountsObj;
            for (Object accountObj : allConsumerAccounts) {
                String accountProfileId = resolveProfileId(accountObj);
                if (selectedProfileId.equals(accountProfileId)) {
                    filteredConsumerAccounts.add(accountObj);
                }
            }
        }

        // Filter additionalData items by selected profile (null type treated as "individual")
        List filteredAdditionalData = new ArrayList();
        Object allAdditionalDataObj = session.getAttribute("additionalData");
        if (allAdditionalDataObj instanceof List) {
            for (Object sectionObj : (List) allAdditionalDataObj) {
                if (!(sectionObj instanceof Map)) {
                    continue;
                }
                Map section = (Map) sectionObj;
                Object itemsObj = section.get("items");
                if (!(itemsObj instanceof List)) {
                    filteredAdditionalData.add(section);
                    continue;
                }
                List filteredItems = new ArrayList();
                for (Object itemObj : (List) itemsObj) {
                    if (!(itemObj instanceof Map)) {
                        continue;
                    }
                    Map itemMap = (Map) itemObj;
                    Object typeObj = itemMap.get("type");
                    String itemType = (typeObj == null || String.valueOf(typeObj).trim().isEmpty())
                            ? "individual" : String.valueOf(typeObj);
                    if (selectedProfileId.equals(itemType)) {
                        filteredItems.add(itemObj);
                    }
                }
                if (!filteredItems.isEmpty()) {
                    Map filteredSection = new LinkedHashMap(section);
                    filteredSection.put("items", filteredItems);
                    filteredAdditionalData.add(filteredSection);
                }
            }
        }

        for (String key : keysToPromoteToSession) {
            Object sessionValue = session.getAttribute(key);
            if (sessionValue != null) {
                request.setAttribute(key, sessionValue);
            }
        }

        request.setAttribute("consumerAccounts", filteredConsumerAccounts);
        request.setAttribute("additionalData", filteredAdditionalData);
        request.setAttribute("handleAccountSelectionSeparately", true);
        request.setAttribute("selectedProfileId", selectedProfileId);
        request.setAttribute("selectedProfileName", selectedProfileName);

        javax.servlet.RequestDispatcher dispatcher = request.getRequestDispatcher("/fs_default_account_selection.jsp");
        dispatcher.forward(request, response);
        return;
    }
%>

<html>
    <head>
        <jsp:include page="includes/head.jsp"/>
        <script src="js/profile-selection.js"></script>
    </head>

    <body dir="${textDirection}">
        <div class="page-content-wrapper" style="position: relative; min-height: 100vh;">
            <div class="container-fluid" style="padding-bottom: 40px">
                <div class="container">
                    <div class="login-form-wrapper">

                        <jsp:include page="includes/logo.jsp"/>

                        <div class="row data-container">
                            <div class="clearfix"></div>
                            <form action="${pageContext.request.contextPath}/fs_cds_profile_selection.jsp" method="post"
                                  id="cds_profile_selection" name="cds_profile_selection" class="form-horizontal">
                                <div class="login-form">
                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">

                                            <h3 class="ui header">
                                                ${appRequestsDetails}
                                            </h3>

                                            <div class="form-group ui form select">
                                                <h5 class="ui body col-md-12">
                                                    Please select the profile you would like to share data from:
                                                </h5>
                                                <div class="col-md-12" id="profile-selection-list"></div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-group ui form row">
                                        <div class="ui body col-md-12">
                                            <input type="button" class="ui default column button btn btn-default" id="cancel"
                                                   name="cancel" onclick="showModal()" value="Cancel"/>
                                            <input type="submit" class="ui primary column button btn" id="btnNext"
                                                   name="confirm profile" value="${buttonNext}"/>
                                            <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false"/>
                                            <input type="hidden" name="consent" id="consent" value="deny"/>
                                            <input type="hidden" name="accountsArray[]" id="account" value=""/>
                                            <input type="hidden" name="accNames" id="accountName" value=""/>
                                            <input type="hidden" name="type" id="type" value="${type}"/>
                                            <input type="hidden" name="selectedProfileId" id="selectedProfileId" value=""/>
                                            <input type="hidden" name="selectedProfileName" id="selectedProfileName" value=""/>
                                            <input type="hidden" name="handleAccountSelectionSeparately" value="true"/>
                                            <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
                                        </div>
                                    </div>

                                    <jsp:include page="includes/privacy-footer.jsp"/>
                                </div>

                                <div id="profile-grouped-seed-data" class="hide">
                                    <c:forEach items="${consumerAccounts}" var="account">
                                        <c:set var="profileIdValue" value="${not empty account.additionalProperties.profileId ? account.additionalProperties.profileId : account.profileId}"/>
                                        <c:if test="${empty profileIdValue}">
                                            <c:set var="profileIdValue" value="individual"/>
                                        </c:if>
                                        <c:set var="profileNameValue" value="${not empty account.additionalProperties.profileName ? account.additionalProperties.profileName : account.profileName}"/>
                                        <c:if test="${empty profileNameValue}">
                                            <c:choose>
                                                <c:when test="${profileIdValue eq 'individual'}">
                                                    <c:set var="profileNameValue" value="Individual"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="profileNameValue" value="${profileIdValue}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        <span class="profile-account-seed"
                                              data-display-name="<c:out value='${account.displayName}'/>"
                                              data-profile-id="<c:out value='${profileIdValue}'/>"
                                              data-profile-name="<c:out value='${profileNameValue}'/>"
                                              data-description="<c:out value='${account.description}'/>"
                                              data-title="<c:out value='${account.title}'/>"
                                              data-selected="${account.selected}">
                                        </span>
                                    </c:forEach>

                                    <c:forEach items="${permissions}" var="permission">
                                        <span class="permission-seed" data-uid="<c:out value='${permission.uid}'/>"></span>
                                    </c:forEach>

                                    <span id="allow-multiple-accounts-seed" data-value="${allowMultipleAccounts}"></span>
                                    <span id="pre-selected-profile-id-seed" data-value="${preSelectedProfileId}"></span>
                                    <span id="no-accounts-text-seed" data-value="${noConsumerAccounts}"></span>
                                    <span id="select-profile-text-seed" data-value="Please select a profile"></span>
                                    <span id="redirect-url-seed" data-value="${redirectURL}"></span>
                                    <span id="state-seed" data-value="${state}"></span>
                                </div>
                            </form>

                            <div class="modal" id="cancelModal">
                                <div class="modal-dialog">
                                    <div class="modal-content">
                                        <div class="modal-body">
                                            <p style="color:black">
                                                Unless you confirm your authorisation, we won't be able to share your data.
                                                <br><br>Are you sure you would like to cancel this process?
                                            </p>

                                            <div class="ui two column grid">
                                                <table style="width:100%">
                                                    <tbody>
                                                    <tr>
                                                        <td>
                                                            <div class="md-col-6 column align-left buttons">
                                                                <input type="button" onclick="redirectFromProfileSelection()"
                                                                       class="ui default column button btn btn-default"
                                                                       id="registerLink" role="button" value="Yes cancel">
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <div class="column align-right buttons">
                                                                <input type="button" onclick="closeModal()"
                                                                       class="ui primary column button btn" role="button"
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
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="includes/footer.jsp"/>
            <script src="js/tooltip-functions.js"></script>
        </div>
    </body>
</html>
