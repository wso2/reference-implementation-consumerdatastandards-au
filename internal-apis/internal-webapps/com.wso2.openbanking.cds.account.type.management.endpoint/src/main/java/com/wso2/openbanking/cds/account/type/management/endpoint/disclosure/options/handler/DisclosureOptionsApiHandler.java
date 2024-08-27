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

package org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.handler;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataService;
import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.wso2.openbanking.cds.account.type.management.endpoint.constants.AccountTypeManagementConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import javax.ws.rs.core.Response;

/**
 * Handler class for handling CDS Account Disclosure Options requests.
 * Updates the disclosure options for CDS accounts based on the provided request body.
 */
public class DisclosureOptionsApiHandler {
    private static final Log log = LogFactory.getLog(DisclosureOptionsApiHandler.class);
    AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    public Response updateCDSAccountDisclosureOptions(String requestBody) {

        log.debug("Update Account Disclosure Options request received");
        JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

        try {
            JSONObject requestBodyJSON = (JSONObject) parser.parse(requestBody);
            JSONArray requestBodyJSONData = (JSONArray) requestBodyJSON.get(AccountTypeManagementConstants.DATA);

            for (Object requestBodyJSONDataItem : requestBodyJSONData) {

                String accountId = ((JSONObject) requestBodyJSONDataItem).
                        getAsString(AccountTypeManagementConstants.ACCOUNT_ID);
                String disclosureOption = ((JSONObject) requestBodyJSONDataItem).
                        getAsString(AccountTypeManagementConstants.DISCLOSURE_OPTION);

                // Add the disclosureOption value to a HashMap
                HashMap<String, String> disclosureOptionsMap = new HashMap<String, String>();
                disclosureOptionsMap.put(AccountTypeManagementConstants.DISCLOSURE_OPTION_STATUS, disclosureOption);

                // Call the addOrUpdateGlobalAccountMetadata method from the AccountMetadataService class
                accountMetadataService.addOrUpdateAccountMetadata(accountId, disclosureOptionsMap);
            }
            return Response.ok().build();
        } catch (OpenBankingException e) {
            log.error("Error occurred while updating CDS Account Disclosure Options", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ParseException e) {
            log.error("Bad Request. Request body validation failed", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}


