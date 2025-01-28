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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import org.testng.Assert
import org.wso2.cds.test.framework.configuration.APIConfigurationService
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil

/**
 * Class contains REST Api implementation for publisher.
 */
class RESTApiPublishRequestBuilder {

    private static APIConfigurationService apiConfiguration
    private static AUConfigurationService auConfiguration

    String baseURL = AUConstants.REST_API_PUBLISHER_ENDPOINT
    List<String> mediationPolicyID = new ArrayList<>()
    List<String> revisionID = new ArrayList<>()

    RESTApiPublishRequestBuilder() {
        auConfiguration = new AUConfigurationService()
        apiConfiguration = new APIConfigurationService()
    }

    /**
     * Create APIs by referring the api-config-provisioning.yaml.
     * @param accessToken
     * @return apiIDs
     */
    List<String> createAPIs(String accessToken) {
        URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + "import-openapi")
        ArrayList<String> apiFilePaths = apiConfiguration.getApiFilePath()
        List<String> apiIDs = new ArrayList<String>()
        def apis = apiFilePaths.size()
        for (int i = 0; i < apis; i++) {

            if (apiConfiguration.getApiEndpointType()[i] == "http") {

                def response = AURestAsRequestBuilder.buildRequest()
                        .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                        .contentType(AUConstants.CONTENT_TYPE_MULTIPART)
                        .multiPart("file", new File(apiFilePaths[i]))
                        .multiPart("additionalProperties", getAdditionalProperties(apiConfiguration.getApiName()[i],
                                apiConfiguration.getApiProperty()[i]["ob-api-version"].toString(),
                                apiConfiguration.getApiContext()[i], apiConfiguration.getApiEndpointType()[i],
                                apiConfiguration.getEnableSchemaValidation()[i], apiConfiguration.getSandboxEndpoint()[i],
                                apiConfiguration.getProductionEndpoint()[i]))
                        .post apiEndpoint.toString()
                Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
                Assert.assertEquals(AUTestUtil.parseResponseBody(response, "name"), apiConfiguration.getApiName()[i])
                apiIDs.add(AUTestUtil.parseResponseBody(response, "id"))

            } else {
                def response = AURestAsRequestBuilder.buildRequest()
                        .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                        .contentType(AUConstants.CONTENT_TYPE_MULTIPART)
                        .multiPart("file", new File(apiFilePaths[i]))
                        .multiPart("additionalProperties", getAdditionalProperties(apiConfiguration.getApiName()[i],
                                apiConfiguration.getApiProperty()[i]["ob-api-version"].toString(),
                                apiConfiguration.getApiContext()[i], apiConfiguration.getApiEndpointType()[i],
                                apiConfiguration.getEnableSchemaValidation()[i]))
                        .post apiEndpoint.toString()
                Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
                Assert.assertEquals(AUTestUtil.parseResponseBody(response, "name"), apiConfiguration.getApiName()[i])
                apiIDs.add(AUTestUtil.parseResponseBody(response, "id"))
            }
        }
        return apiIDs
    }

    /**
     * Create mediation policy by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    void createRevision(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + apiIDs.get(i) + "/revisions")
            def response = AURestAsRequestBuilder.buildRequest()
                    .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                    .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(getCreateRevisionPayload("revision1"))
                    .post(apiEndpoint)
            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
            revisionID.add(AUTestUtil.parseResponseBody(response, "id"))

        }
    }

    /**
     * Deploy revision by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    void deployRevision(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + apiIDs.get(i) + "/deploy-revision")
            String apimHostname = apiEndpoint.getHost();
            apimHostname = apimHostname.startsWith("www.") ? apimHostname.substring(4) : apimHostname;
            def response = AURestAsRequestBuilder.buildRequest()
                    .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                    .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .queryParam("revisionId", revisionID.get(i))
                    .body(getDeployRevisionPayload(apimHostname, revisionID.get(i)))
                    .post(apiEndpoint)
            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
        }
    }

    /**
     * Publish API.
     * @param accessToken
     * @param apiIDs
     */
    void publishAPI(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + "change-lifecycle")
            def response = AURestAsRequestBuilder.buildRequest()
                    .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                    .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .queryParam("apiId", apiIDs.get(i))
                    .queryParam("action", "Publish")
                    .post(apiEndpoint)

            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        }
    }

    /**
     * Get mediation policy.
     * @param id
     * @param name
     * @return mediation policy
     */
    static List<Map> getMediationPolicy(id, name) {
        Map property = new HashMap()
        property.put("id", id)
        property.put("name", name)
        property.put("type", "in")
        List<Map> policy = new ArrayList()
        policy.add(property)
        return policy
    }

    /**
     * Get additional properties.
     * @param api_name
     * @param api_version
     * @param api_context
     * @param api_endpoint_type
     * @param isSchemaEnabled
     * @param sandbox_endpoints
     * @param production_endpoints
     * @return additional properties
     */
    static String getAdditionalProperties(String api_name, String api_version, String api_context, String api_endpoint_type,
                                          String isSchemaEnabled, String sandbox_endpoints = "default",
                                          String production_endpoints = "default") {


        if (!api_endpoint_type.equalsIgnoreCase("default")) {
            if(api_name.contains("ConsumerDataStandards")){
                sandbox_endpoints = "${auConfiguration.getServerGatewayURL()}" + "${sandbox_endpoints}"
                production_endpoints = "${auConfiguration.getServerGatewayURL()}" + "${production_endpoints}"
            } else {
                sandbox_endpoints = "${auConfiguration.getServerAuthorisationServerURL()}" + "${sandbox_endpoints}"
                production_endpoints = "${auConfiguration.getServerAuthorisationServerURL()}" + "${production_endpoints}"
            }
        }

        return """
            {
              "name": "$api_name",
              "version": "$api_version",
              "context": "$api_context",
              "enableSchemaValidation": $isSchemaEnabled,
              "policies": [
                "Unlimited"
              ],
              "apiThrottlingPolicy": "Unlimited",
              "endpointConfig": {
                "endpoint_type": "$api_endpoint_type",
                "sandbox_endpoints": {
                  "url": "$sandbox_endpoints"
                },
                "production_endpoints": {
                  "url": "$production_endpoints"
                }
              }
            }
            """.stripIndent()
    }

    /**
     * Get create revision payload.
     * @param description
     * @return create revision payload
     */
    static String getCreateRevisionPayload(String description) {
        return """
            {
              "description": "$description"
            }
             """.stripIndent()
    }

    /**
     * Get deploy revision payload.
     * @param apimHostname
     * @return deploy revision payload
     */
    static String getDeployRevisionPayload(String apimHostname, String revisionId){
        return """
            [{
                "revisionUuid": "$revisionId",
                "name": "Default",
                "vhost": "$apimHostname",
                "displayOnDevportal": true
            }]
             """.stripIndent()
    }

    /**
     * Add policy by referring the api-config-provisioning.yaml
     * @param accessToken
     * @return
     */
    void addPolicy(String accessToken, List<String> apiIDs) {

        ArrayList<String> sequenceFilePaths = apiConfiguration.getSequenceFilePath()

        ArrayList<String> apiNames = apiConfiguration.getApiName()
        def sequences = sequenceFilePaths.size()
        for (int i = 0; i < sequences; i++) {
            def t = sequenceFilePaths[i]
            URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + apiIDs.get(i)
                    + "/operation-policies")

            if (!sequenceFilePaths[i].equals("")) {
                def response = AURestAsRequestBuilder.buildRequest()
                        .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                                AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                        .contentType(AUConstants.CONTENT_TYPE_MULTIPART)
                        .multiPart("synapsePolicyDefinitionFile", new File(sequenceFilePaths[i]))
                        .multiPart("policySpecFile",
                                getPolicySpecFileDefinition(apiNames.get(i), "1.0").toString())
                        .post(apiEndpoint.toString())

                Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
                // Updating the API resources with operation policies.
                updateAPIWithPolicy(apiIDs.get(i), apiNames.get(i),
                        AUTestUtil.parseResponseBody(response, "id"), accessToken)
            }
        }
    }

    /**
     * Update API with the created policy
     * @param apiId
     * @param apiName
     * @param policyId
     * @param accessToken
     */
    private void updateAPIWithPolicy(String apiId, String apiName, String policyId, String accessToken) {

        URI apiEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + apiId)
        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                        AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                .get(apiEndpoint.toString())

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)

        JsonObject responseJSONObject = JsonParser.parseString(response.getBody().asString()) as JsonObject
        JsonArray operations = responseJSONObject.get("operations").getAsJsonArray()
        JsonArray modifiedOperationsList = new JsonArray()
        for (int operationCount = 0; operationCount < operations.size(); operationCount++) {
            JsonObject currentOperation = operations.get(operationCount) as JsonObject
            JsonObject operationPolicies = currentOperation.get("operationPolicies") as JsonObject;
            JsonArray policy = getRequestOperationPolicy(apiName, policyId, "1.0")
            operationPolicies.add("request", policy)
            operations.get(operationCount).add("operationPolicies", operationPolicies)
            modifiedOperationsList.add(operations.get(operationCount))
        }
        operations = modifiedOperationsList
        responseJSONObject.add("operations", operations)

        def updatedResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(responseJSONObject.toString())
                .put(apiEndpoint)
        Assert.assertEquals(updatedResponse.statusCode(), HTTPResponse.SC_OK)
    }

    /**
     * This method will return the policy spec file definition for given inputs.
     * @param apiName api name
     * @param version api version
     * @return
     */
    private static JsonObject getPolicySpecFileDefinition(String apiName, String version) {

        JsonObject policySpecObject = new JsonObject();
        policySpecObject.addProperty("category", "Mediation")
        policySpecObject.addProperty("name", apiName)
        policySpecObject.addProperty("displayName", apiName)
        policySpecObject.addProperty("version", version)
        policySpecObject.addProperty("description", apiName)
        JsonArray applicableFlows = new JsonArray();
        applicableFlows.add("request")
        applicableFlows.add("response")
        applicableFlows.add("fault")
        policySpecObject.add("applicableFlows", applicableFlows)
        JsonArray supportedApiTypes = new JsonArray();
        supportedApiTypes.add("HTTP")
        policySpecObject.add("supportedApiTypes", supportedApiTypes)
        JsonArray supportedGateways = new JsonArray();
        supportedGateways.add("Synapse")
        policySpecObject.add("supportedGateways", supportedGateways)
        JsonArray policyAttributes = new JsonArray();
        policySpecObject.add("policyAttributes", policyAttributes)
        return policySpecObject;
    }

    /**
     * This method will return the operation policy json for the given inputs.
     * @param policyName policy name
     * @param policyId policy id
     * @param policyVersion policy version
     * @return
     */
    private static JsonArray getRequestOperationPolicy(String policyName, String policyId, String policyVersion) {

        JsonArray request = new JsonArray();
        JsonObject policyObject = new JsonObject();
        policyObject.addProperty("policyName", policyName)
        policyObject.addProperty("policyId", policyId)
        policyObject.addProperty("policyVersion", policyVersion)
        request.add(policyObject)
        return request;
    }
}
