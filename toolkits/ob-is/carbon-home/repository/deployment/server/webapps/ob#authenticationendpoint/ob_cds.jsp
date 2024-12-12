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

<%

    session.setAttribute("accounts_data", getAttribute(request, session, "accounts_data", null));
    session.setAttribute("profiles_data", getAttribute(request, session, "profiles_data", null));
    session.setAttribute("sp_full_name", getAttribute(request, session, "sp_full_name", null));
    session.setAttribute("redirectURL", getAttribute(request, session, "redirectURL", null));
    session.setAttribute("consent_expiration", getAttribute(request, session, "consent_expiration", null));
    session.setAttribute("account_masking_enabled", getAttribute(request, session, "account_masking_enabled", null));
    session.setAttribute("isConsentAmendment", getAttribute(request, session, "isConsentAmendment", null));
    session.setAttribute("skipAccounts", getAttribute(request, session, "customerScopesOnly", null));
    session.setAttribute("isSharingDurationUpdated", getAttribute(request, session, "isSharingDurationUpdated", null));
    session.setAttribute("app", getAttribute(request, session, "app", null));
    session.setAttribute("configParamsMap", getAttribute(request, session, "data_requested", null));
    session.setAttribute("newConfigParamsMap", getAttribute(request, session, "new_data_requested", null));
    session.setAttribute("business_data_cluster", getAttribute(request, session, "business_data_cluster", null));
    session.setAttribute("new_business_data_cluster", getAttribute(request, session, "new_business_data_cluster", null));
    session.setAttribute("state", getAttribute(request, session, "state", null));
    session.setAttribute("sharing_duration_value", getAttribute(request, session, "sharing_duration_value", null));
    session.setAttribute("customerScopesOnly", getAttribute(request, session, "customerScopesOnly", null));
    session.setAttribute("nameClaims", getAttribute(request, session, "nameClaims", ""));
    session.setAttribute("contactClaims", getAttribute(request, session, "contactClaims", ""));

    String preSelectedProfileId = (String) getAttribute(request, session, "preSelectedProfileId", null);
    if (preSelectedProfileId == null || "".equals(preSelectedProfileId)) {
        response.sendRedirect("ob_cds_profile_selection.do");
    } else {
        session.setAttribute("preSelectedProfileId", preSelectedProfileId);
        response.sendRedirect("ob_cds_account_selection.do");
    }

%>

<%!
    /**
     * Retrieves an attribute from the request scope first and falls back to the session scope
     * if not found in the request. If the attribute is not found in either scope, a default
     * value is returned.
     *
     * @param request       the HttpServletRequest object to check for the attribute.
     * @param session       the HttpSession object to check for the attribute if not found in the request.
     * @param attributeName the name of the attribute to retrieve.
     * @param defaultValue  the default value to return if the attribute is not found in both the request and session.
     * @return the value of the attribute as an Object, or the default value if the attribute is not found.
     */
    private Object getAttribute(HttpServletRequest request, HttpSession session, String attributeName,
                                Object defaultValue) {
        // Check in the request first
        Object requestAttribute = request.getAttribute(attributeName);
        if (requestAttribute != null) {
            return requestAttribute;
        }

        String requestParameter = request.getParameter(attributeName);
        if (requestParameter != null) {
            return requestParameter;
        }

        // Fallback to session if not found in the request
        Object sessionAttribute = session.getAttribute(attributeName);
        if (sessionAttribute != null) {
            return sessionAttribute;
        }

        // Return the default value if not found
        return defaultValue;
    }
%>