/**
 * Copyright (c) 2024 - 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.cds.preexecution

import com.nimbusds.oauth2.sdk.http.HTTPResponse
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.RESTApiUserCreationRequestBuilder

import org.wso2.cds.test.framework.utility.AUTestUtil

import java.util.logging.Logger

/**
 * Test class to contain steps to create Test Users.
 */
class UserCreationTest extends AUTest {

    String userId
    String roleId
    String userName
    String password
    String roleName
    RESTApiUserCreationRequestBuilder restApiUserCreationRequestBuilder
    Logger log = Logger.getLogger(UserCreationTest.class.toString())
    List<Map<String, String>> userInfoList

    @Test (groups = "SmokeTest")
    void "Create PSU"() {

        userInfoList = new ArrayList<>()

        for (int i = 0; i < 5; i++) {

            auConfiguration.setPsuNumber(i)
            userName = auConfiguration.getUserPSUName()
            password = auConfiguration.getUserPSUPWD()

            restApiUserCreationRequestBuilder = new RESTApiUserCreationRequestBuilder()

            if (!userName.equalsIgnoreCase(auConfiguration.getUserKeyManagerAdminName())) {
                def createUserResponse = restApiUserCreationRequestBuilder.createUser(userName, password)
                Assert.assertEquals(createUserResponse.statusCode(), HTTPResponse.SC_CREATED)
                userId = AUTestUtil.parseResponseBody(createUserResponse, "id")
                Assert.assertNotNull(userId)
            } else {
                userId = restApiUserCreationRequestBuilder.getAdminUserId()
            }

                Map<String, String> userInfo = new HashMap<>()
                userInfo.put("UserId", userId)
                userInfo.put("Username", userName)
                userInfoList.add(userInfo)
        }
    }

    @Test (dependsOnMethods = ["Create PSU"], groups = "SmokeTest")
    void "Assign Subscriber Role to the user"() {

        if(userName.equalsIgnoreCase(auConfiguration.getUserPublisherName())){
            roleName = AUConstants.PUBLISHER_ROLE

            //Get Internal/publisher role Id
            roleId = restApiUserCreationRequestBuilder.getRoleId(roleName)

            //Assign publisher role to the created user
            def response = restApiUserCreationRequestBuilder.assignUserRoles(roleName, roleId, userInfoList)

            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)

            //Verify Assigned User Role
            def responseUserRole = restApiUserCreationRequestBuilder.getUserDetails(userId)

            Assert.assertEquals(responseUserRole.statusCode(), HTTPResponse.SC_OK)
            Assert.assertTrue(AUTestUtil.parseResponseBody(responseUserRole, "roles[0].value")
                    .contains(roleId))

        } else if (!userName.equalsIgnoreCase(auConfiguration.getUserKeyManagerAdminName())) {
            roleName = AUConstants.SUBSCRIBER_ROLE

            //Get Internal/subscriber role Id
            roleId = restApiUserCreationRequestBuilder.getRoleId(roleName)

            //Assign subscriber role to the created user
            def response = restApiUserCreationRequestBuilder.assignUserRoles(roleName, roleId, userInfoList)

            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)

            //Verify Assigned User Role
            def responseUserRole = restApiUserCreationRequestBuilder.getUserDetails(userId)

            Assert.assertEquals(responseUserRole.statusCode(), HTTPResponse.SC_OK)
            Assert.assertTrue(AUTestUtil.parseResponseBody(responseUserRole, "roles[0].value")
                    .contains(roleId))
        }
    }
}
