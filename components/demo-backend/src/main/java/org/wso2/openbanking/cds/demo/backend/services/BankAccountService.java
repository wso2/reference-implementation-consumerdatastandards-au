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
package org.wso2.openbanking.cds.demo.backend.services;

import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.cds.demo.backend.BankException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * BankAccountService class.
 */
@Path("/bankaccountservice/")
public class BankAccountService {
    // accounts api with joint account info
    private static final String accountList = "{\n" +
            "    \"data\": [\n" +
            "        {\n" +
            "            \"accountId\": \"30080012343456\",\n" +
            "            \"displayName\": \"account_1\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Individual\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isSecondaryAccount\" : false,\n" +
            "            \"isJointAccount\": false,\n" +
            "            \"jointAccountConsentElectionStatus\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"30080098763459\",\n" +
            "            \"displayName\": \"account_2\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Individual\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isSecondaryAccount\" : false,\n" +
            "            \"isJointAccount\": false,\n" +
            "            \"jointAccountConsentElectionStatus\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"6500001232\",\n" +
            "            \"displayName\": \"joint_account_1\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Individual\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"isSecondaryAccount\" : false,\n" +
            "            \"jointAccountConsentElectionStatus\": \"ELECTED\",\n" +
            "            \"jointAccountinfo\": {\n" +
            "              \"linkedMember\": [\n" +
            "                {\n" +
            "                  \"memberId\": \"amy@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "                {\n" +
            "                  \"memberId\": \"ann@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                }" +
            "              ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"7500101544\",\n" +
            "            \"displayName\": \"joint_account_2\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"joint-account-2\",\n" +
            "            \"customerAccountType\": \"Individual\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"isSecondaryAccount\" : false,\n" +
            "            \"jointAccountConsentElectionStatus\": \"UNAVAILABLE\",\n" +
            "            \"jointAccountinfo\": {\n" +
            "                \"linkedMember\": [\n" +
            "                    {\n" +
            "                        \"memberId\": \"amy@gold.com@carbon.super\",\n" +
            "                        \"meta\": {}\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"7500101545\",\n" +
            "            \"displayName\": \"joint_account_3\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"joint-account-3\",\n" +
            "            \"customerAccountType\": \"Individual\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"isSecondaryAccount\" : false,\n" +
            "            \"jointAccountConsentElectionStatus\": \"ELECTED\",\n" +
            "            \"jointAccountinfo\": {\n" +
            "                \"linkedMember\": [\n" +
            "                    {\n" +
            "                        \"memberId\": \"mark@gold.com@carbon.super\",\n" +
            "                        \"meta\": {}\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "          \"accountId\": \"143-000-B1234\",\n" +
            "          \"displayName\": \"business_account_1\",\n" +
            "          \"authorizationMethod\": \"single\",\n" +
            "          \"nickName\": \"not-working\",\n" +
            "          \"customerAccountType\": \"Business\",\n" +
            "          \"profileName\": \"Organization A\",\n" +
            "          \"profileId\": \"00001\",\n" +
            "          \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "          \"isEligible\": true,\n" +
            "          \"isJointAccount\": false,\n" +
            "          \"isSecondaryAccount\" : false,\n" +
            "          \"jointAccountConsentElectionStatus\": false,\n" +
            "          \"businessAccountInfo\": {\n" +
            "              \"AccountOwners\": [\n" +
            "                  {\n" +
            "                  \"memberId\":\"user1@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                  },\n" +
            "                  {\n" +
            "                  \"memberId\":\"user2@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                 }\n" +
            "             ],\n" +
            "             \"NominatedRepresentatives\":[\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser1@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser2@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"admin@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 }\n" +
            "              ]\n" +
            "           }\n" +
            "        },\n" +
            "        {\n" +
            "          \"accountId\": \"153-000-C1234\",\n" +
            "          \"displayName\": \"business_account_2\",\n" +
            "          \"authorizationMethod\": \"single\",\n" +
            "          \"nickName\": \"not-working\",\n" +
            "          \"customerAccountType\": \"Business\",\n" +
            "          \"profileName\": \"Organization B\",\n" +
            "          \"profileId\": \"00002\",\n" +
            "          \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "          \"isEligible\": true,\n" +
            "          \"isJointAccount\": false,\n" +
            "          \"isSecondaryAccount\" : false,\n" +
            "          \"jointAccountConsentElectionStatus\": false,\n" +
            "          \"businessAccountInfo\": {\n" +
            "              \"AccountOwners\": [\n" +
            "                  {\n" +
            "                  \"memberId\":\"user1@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                  },\n" +
            "                  {\n" +
            "                  \"memberId\":\"user3@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                 }\n" +
            "             ],\n" +
            "             \"NominatedRepresentatives\":[\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser1@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser2@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"admin@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 }\n" +
            "              ]\n" +
            "           }\n" +
            "        },\n" +
            "        {\n" +
            "          \"accountId\": \"586-522-B0025\",\n" +
            "          \"displayName\": \"business_account_3\",\n" +
            "          \"authorizationMethod\": \"single\",\n" +
            "          \"nickName\": \"not-working\",\n" +
            "          \"customerAccountType\": \"Business\",\n" +
            "          \"profileName\": \"Organization B\",\n" +
            "          \"profileId\": \"00002\",\n" +
            "          \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "          \"isEligible\": true,\n" +
            "          \"isJointAccount\": false,\n" +
            "          \"isSecondaryAccount\" : false,\n" +
            "          \"jointAccountConsentElectionStatus\": false,\n" +
            "          \"businessAccountInfo\": {\n" +
            "              \"AccountOwners\": [\n" +
            "                  {\n" +
            "                  \"memberId\":\"nominatedUser3@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                  },\n" +
            "                  {\n" +
            "                  \"memberId\":\"user2@wso2.com@carbon.super\",\n" +
            "                  \"meta\":{}\n" +
            "                  }\n" +
            "             ],\n" +
            "             \"NominatedRepresentatives\":[\n" +
            "                 {\n" +
            "                 \"memberId\":\"user1@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser1@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"admin@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 },\n" +
            "                 {\n" +
            "                 \"memberId\":\"nominatedUser4@wso2.com@carbon.super\",\n" +
            "                 \"meta\":{}\n" +
            "                 }\n" +
            "              ]\n" +
            "           }\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"30080098763500\",\n" +
            "            \"displayName\": \"secondary_account_1\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": false,\n" +
            "            \"jointAccountConsentElectionStatus\": false,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"secondaryAccountPrivilegeStatus\" : true,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                {\n" +
            "                  \"memberId\": \"amy@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "              ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"30080098763501\",\n" +
            "            \"displayName\": \"secondary_account_2\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": false,\n" +
            "            \"jointAccountConsentElectionStatus\": false,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"secondaryAccountPrivilegeStatus\" : true,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                {\n" +
            "                  \"memberId\": \"ann@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "              ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"30080098763510\",\n" +
            "            \"displayName\": \"secondary_account_3\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"not-working\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isJointAccount\": false,\n" +
            "            \"jointAccountConsentElectionStatus\": false,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"secondaryAccountPrivilegeStatus\" : true,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                {\n" +
            "                  \"memberId\": \"mark@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "              ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"7500101550\",\n" +
            "            \"displayName\": \"secondary_joint_account_1\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"secondary_joint_account_1\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"jointAccountConsentElectionStatus\": \"ELECTED\",\n" +
            "            \"secondaryAccountPrivilegeStatus\" : true,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                     {\n" +
            "                  \"memberId\": \"amy@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "                {\n" +
            "                  \"memberId\": \"ann@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                }" +
            "                ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"7500101555\",\n" +
            "            \"displayName\": \"secondary_joint_account_2\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"secondary_joint_account_2\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"jointAccountConsentElectionStatus\": \"ELECTED\",\n" +
            "            \"secondaryAccountPrivilegeStatus\" : false,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                     {\n" +
            "                  \"memberId\": \"john@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "                {\n" +
            "                  \"memberId\": \"peter@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                }" +
            "                ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"accountId\": \"7500101558\",\n" +
            "            \"displayName\": \"secondary_joint_account_3\",\n" +
            "            \"authorizationMethod\": \"single\",\n" +
            "            \"nickName\": \"secondary_joint_account_1\",\n" +
            "            \"customerAccountType\": \"Secondary\",\n" +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "            \"isEligible\": true,\n" +
            "            \"isSecondaryAccount\" : true,\n" +
            "            \"isJointAccount\": true,\n" +
            "            \"jointAccountConsentElectionStatus\": \"UNAVAILABLE\",\n" +
            "            \"secondaryAccountPrivilegeStatus\" : true,\n" +
            "            \"secondaryAccountInfo\": {\n" +
            "              \"accountOwner\": [\n" +
            "                     {\n" +
            "                  \"memberId\": \"john@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "                     {\n" +
            "                  \"memberId\": \"amy@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                },\n" +
            "                {\n" +
            "                  \"memberId\": \"ann@gold.com@carbon.super\",\n" +
            "                  \"meta\": {}\n" +
            "                }" +
            "                ]\n" +
            "            },\n" +
            "            \"meta\": {}\n" +
            "        }\n" +
            "      ]\n" +
            "}";

    @GET
    @Path("/payable-accounts")
    @Produces("application/json; charset=utf-8")
    public Response getPayableAccounts() throws BankException {
        return Response.status(200).entity(accountList).build();
    }

    @GET
    @Path("/sharable-accounts")
    @Produces("application/json; charset=utf-8")
    public Response getSharableAccounts() throws BankException {
        return Response.status(200).entity(accountList).build();
    }

    @POST
    @Path("/payment-charges")
    @Produces("application/json; charset=utf-8")
    @Consumes("application/json")
    public Response calculatePaymentCharges(String request) throws BankException {
        if (StringUtils.isNotBlank(request)) {
            String response = "{\n" +
                    "    \"payment_charges\": \"1.0\",\n" +
                    "    \"payment_currency\": \"GBP\",\n" +
                    "    \"payment_exchange_rate\": \"0.1\"\n" +
                    "}";

            return Response.status(200).entity(response).build();
        }
        return Response.status(403).build();
    }
}
