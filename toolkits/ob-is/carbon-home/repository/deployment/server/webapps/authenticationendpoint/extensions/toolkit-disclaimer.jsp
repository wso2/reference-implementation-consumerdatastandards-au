<!--
 ~ Copyright (c) 2024-2025, WSO2 LLC. (https://www.wso2.com).
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
 -->

<%@ page import="java.net.URLDecoder" %>

<%
    String spDetails = null;
    if (request.getAttribute("spDetails") != null) {
        spDetails = request.getAttribute("spDetails").toString();
    } else {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if ("spDetails".equals(cookies[i].getName())) {
                    spDetails = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                    break;
                }
            }
        }
    }
%>

<div class="form-actions disclaimer" style="font-size:0.9em">
	Your Customer ID will not be shared with "<%=spDetails%>". One time passwords are used
	to share banking data. You will never be asked to provide your real password to share banking data.
</div>
