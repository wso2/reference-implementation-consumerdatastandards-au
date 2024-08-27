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
	String error = "Maximum retry limit reached. Please wait for a while until next attempt. In case of an incorrect Customer ID, please visit Internet Banking to retrieve your Customer ID or contact the CDR.";
	String callbackURL = request.getAttribute("callbackURL").toString();
	%>
	<script>window.location.href = "<%=callbackURL%>?status=Access Denied&statusMsg=<%=error%>"</script>	
	<%	
%>
