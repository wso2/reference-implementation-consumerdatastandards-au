/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cds.test.framework.request_builder

import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.Header
import io.restassured.response.Response
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil

import javax.swing.text.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.nio.charset.Charset

/**
 * SCIM2 Rest API request builder for User Creation.
 */
class RESTApiUserCreationRequestBuilder {

	private AUConfigurationService auConfiguration = new AUConfigurationService()
	String baseURL = AUConstants.REST_API_SCIM2_ENDPOINT
	Response createUserResponse
	Response roleGroupResponse
	Response responseUserDetails

	def authToken = "${auConfiguration.getUserKeyManagerAdminName()}:${auConfiguration.getUserKeyManagerAdminPWD()}"
	String basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

	Response createUser(String userName, String password) {

		URI scim2Url = new URI("${auConfiguration.getServerAuthorisationServerURL()}" + baseURL + "/Users")
		def payload = getUserCreationPayload(userName, password)

		createUserResponse = AURestAsRequestBuilder.buildRequest()
						.contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
						.accept(AUConstants.CONTENT_TYPE_APPLICATION_SCIM_JSON)
						.header(AUConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
						.body(payload)
						.post(scim2Url.toString())

		return createUserResponse
	}

	String getRoleId(String roleName) {

		URI scim2Url = new URI("${auConfiguration.getServerAuthorisationServerURL()}" + baseURL + "/Roles")

		String roleNameSubString = roleName.split("/")[1]

		def roleGroupResponse = AURestAsRequestBuilder.buildRequest()
						.accept(AUConstants.CONTENT_TYPE_APPLICATION_SCIM_JSON)
						.header(AUConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
						.queryParam("filter", "displayName eq $roleNameSubString")
						.get(scim2Url.toString())

		def roleId = AUTestUtil.parseResponseBody(roleGroupResponse, "Resources[0].id")

		return roleId
	}

	Response assignUserRoles(String roleName, String roleId, List<Map<String, String>> userInfoList) {

		URI scim2Url = new URI("${auConfiguration.getServerAuthorisationServerURL()}" + baseURL + "/Groups/$roleId")
		def payload = defaultUserRolesPayload(roleName, userInfoList)

		roleGroupResponse = RestAssured.given()
				.config(RestAssuredConfig.newConfig()
						.encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))) // Prevent charset appending
				.relaxedHTTPSValidation()
				.header("Content-Type", "application/scim+json")
				.header("Accept", "application/scim+json")
				.header(AUConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
				.body(payload)
				.put(scim2Url.toString())

		return roleGroupResponse

	}

	Response getUserDetails(String userId) {

		URI scim2Url = new URI("${auConfiguration.getServerAuthorisationServerURL()}" + baseURL + "/Users/$userId")

		responseUserDetails = AURestAsRequestBuilder.buildRequest()
						.accept(AUConstants.CONTENT_TYPE_APPLICATION_SCIM_JSON)
						.header(AUConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
						.queryParam("attributes", "roles")
						.get(scim2Url.toString())

		return responseUserDetails
	}

	String getUserCreationPayload(String userName, String password) {

		String defaultUserCreationPayload = """
			{
			"schemas": [],  
			"name": {    
				"givenName": "psu",    
				"familyName": "psu"  
			},  
			"userName": "${userName}",  
			"password": "${password}",  
			"emails": [
				{      
					"type": "home",      
					"value": "${userName}",      
					"primary": true    
				},    
				{
					"type": "work",      
					"value": "${userName}"
				}  
			],  
			"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User": {    
				"employeeNumber": "1234",    
				"manager": {      
					"value": "${userName}"    
				}  
			}
			}
	    """.stripIndent()

		return defaultUserCreationPayload

	}

	String defaultUserRolesPayload(String roleName, List<Map<String, String>> userInfoList) {

		StringBuilder membersArray = new StringBuilder()
		for (Map<String, String> userInfo : userInfoList) {
			String userId = userInfo.get("UserId")
			String userName = userInfo.get("Username")

			// Add each user to the members array (in JSON format)
			if (membersArray.length() > 0) {
				membersArray.append(",\n")  // Add a comma to separate members
			}

			membersArray.append("  {\n")
					.append("    \"value\": \"").append(userId).append("\",\n")
					.append("    \"display\": \"").append(userName).append("\"\n")
					.append("  }")
		}

		String payload = """
			{
			"displayName":"$roleName",
			"members": [
            """ + membersArray.toString() + """
                ]
		}
		""".stripIndent()

		return payload
	}

	String getAdminUserId() {

		URI scim2Url = new URI("${auConfiguration.getServerAuthorisationServerURL()}" + baseURL + "/Users")

		def roleGroupResponse = AURestAsRequestBuilder.buildRequest()
				.accept(AUConstants.CONTENT_TYPE_APPLICATION_SCIM_JSON)
				.header(AUConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
				.queryParam("filter", "userName eq ${auConfiguration.getUserKeyManagerAdminName()}")
				.get(scim2Url.toString())

		def userId = AUTestUtil.parseResponseBody(roleGroupResponse, "Resources[0].id")

		return userId
	}
}
