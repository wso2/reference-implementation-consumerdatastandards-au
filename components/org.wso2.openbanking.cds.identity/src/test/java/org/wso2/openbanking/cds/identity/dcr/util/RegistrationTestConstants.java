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
package org.wso2.openbanking.cds.identity.dcr.util;

/**
 * Registration Test Constants.
 */
public class RegistrationTestConstants {

    public static final String SSA = "eyJhbGciOiJQUzI1NiIsImtpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2USIsInR5cCI6" +
            "IkpXVCJ9.eyJpc3MiOiJjZHItcmVnaXN0ZXIiLCJpYXQiOjE1NzE4MDgxNjcsImV4cCI6MjE0NzQ4MzY0NiwianRpIjoiM2JjMjA" +
            "1YTFlYmM5NDNmYmI2MjRiMTRmY2IyNDExOTYiLCJvcmdfaWQiOiIzQjBCMEE3Qi0zRTdCLTRBMkMtOTQ5Ny1FMzU3QTcxRDA3QzgiL" +
            "CJvcmdfbmFtZSI6Ik1vY2sgQ29tcGFueSBJbmMuIiwiY2xpZW50X25hbWUiOiJNb2NrIFNvZnR3YXJlIiwiY2xpZW50X2Rlc2NyaXB" +
            "0aW9uIjoiQSBtb2NrIHNvZnR3YXJlIHByb2R1Y3QgZm9yIHRlc3RpbmcgU1NBIiwiY2xpZW50X3VyaSI6Imh0dHBzOi8vd3d3Lm1vY" +
            "2tjb21wYW55LmNvbS5hdSIsInJlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3Q" +
            "xIiwiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QyIl0sImxvZ29fdXJpIjoiaHR0cHM6Ly93d3cubW9ja" +
            "2NvbXBhbnkuY29tLmF1L2xvZ29zL2xvZ28xLnBuZyIsInRvc191cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5jb20uYXUvdG9" +
            "zLmh0bWwiLCJwb2xpY3lfdXJpIjoiaHR0cHM6Ly93d3cubW9ja2NvbXBhbnkuY29tLmF1L3BvbGljeS5odG1sIiwiandrc191cmkiOi" +
            "JodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYLzliNXVzRHBiTnRteERjVHpz" +
            "N0d6S3AuandrcyIsInJldm9jYXRpb25fdXJpIjoiaHR0cHM6Ly9naXN0LmdpdGh1YnVzZXJjb250ZW50LmNvbS9pbWVzaDk0LzMxN" +
            "zJlMmU0NTc1N2NkYTA4ZWMyNzI3ZjkwYjcyY2VkL3Jhdy9mZjBkM2VhYmU0Y2RkY2U0N2VlYzAyMjhmNTkyMTc1MjIzZGQ5MmIyL3dz" +
            "bzItYXUtZGNyLWRlbW8uandrcyIsInJlY2lwaWVudF9iYXNlX3VyaSI6Imh0dHBzOi8vd3d3Lm1vY2tjb21wYW55LmNvbS5hdSIsIn" +
            "NvZnR3YXJlX2lkIjoiNzQwQzM2OEYtRUNGOS00RDI5LUEyRUEtMDUxNEE2NkIwQ0ROIiwic29mdHdhcmVfcm9sZXMiOiJkYXRhLXJl" +
            "Y2lwaWVudC1zb2Z0d2FyZS1wcm9kdWN0Iiwic2NvcGUiOiJvcGVuaWQgYmFuazphY2NvdW50cy5iYXNpYzpyZWFkIGJhbms6YWNjb" +
            "3VudHMuZGV0YWlsOnJlYWQgYmFuazp0cmFuc2FjdGlvbnM6cmVhZCBiYW5rOnBheWVlczpyZWFkIGJhbms6cmVndWxhcl9wYXltZW5" +
            "0czpyZWFkIGNvbW1vbjpjdXN0b21lci5iYXNpYzpyZWFkIGNvbW1vbjpjdXN0b21lci5kZXRhaWw6cmVhZCBjZHI6cmVnaXN0cmF0a" +
            "W9uIn0.Mr5NJA7w8mgbp52xpTzUPLfqdgXBNFozoA9i2XpINsS_7zwun0fcaRRBfb1vxr4HauPG2AA43IzkAnvE5f-BjNgWzlBb_u" +
            "wDV2ShXd5-KboUXgH6BsR2MwS102qZ9YEI9caUR6jZ-sJL6kojpSf8MPThhkSIS07o0YOaZRgsmOAzA1a158iUg-zEl_I6QDLO4s9G" +
            "_2__ii6Qhg1hmmcJfTf3nl2Ci8eVAKA4SPDDqdJ5gn0DW77LpwhkC1iXcAd9fkgS4DEtyX8Bpcrah3o4z6kmxR3JSMp6y4C8quwlELb" +
            "0V6CcByfLEL9_eVKNZycigOfsiFB-0-LnFF_Wc_TjdQ";

    public static String ssaBodyJson = "{\n" +
            "  \"iss\": \"cdr-register\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"org_id\": \"3B0B0A7B-3E7B-4A2C-9497-E357A71D07C8\",\n" +
            "  \"org_name\": \"Mock Company Inc.\",\n" +
            "  \"client_name\": \"Mock Software\",\n" +
            "  \"client_description\": \"A mock software product for testing SSA\",\n" +
            "  \"client_uri\": \"https://www.mockcompany.com.au\",\n" +
            "  \"logo_uri\": \"https://www.mockcompany.com.au/logos/logo1.png\",\n" +
            "  \"tos_uri\": \"https://www.mockcompany.com.au/tos.html\",\n" +
            "  \"policy_uri\": \"https://www.mockcompany.com.au/policy.html\",\n" +
            "  \"software_id\": \"740C368F-ECF9-4D29-A2EA-0514A66B0CDN\",\n" +
            "  \"recipient_base_uri\": \"https://www.mockcompany.com.au\",\n" +
            "  \"jwks_uri\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9b5usDpbNtmxDcTzs" +
            "7GzKp.jwks\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://www.google.com/redirects/redirect1\",\n" +
            "    \"https://www.google.com/redirects/redirect2\"\n" +
            "  ],\n" +
            "  \"software_roles\": \"data-recipient-software-product\",\n" +
            "  \"sector_identifier_uri\": \"https://run.mocky.io/v3/01638249-eede-49f3-a255-da6cb8a18ee7\",\n" +
            "  \"scope\": \"openid bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read " +
            "bank:payees:read bank:regular_payments:read common:customer.basic:read common:customer.detail:read " +
            "cdr:registration\",\n" +
            "  \"revocation_uri\": \"https://gist.githubusercontent.com/imesh94/3172e2e45757cda08ec2727f90b72c" +
            "ed/raw/ff0d3eabe4cddce47eec0228f592175223dd92b2/wso2-au-dcr-demo.jwks\",\n" +
            "}";

    public static String ssaBodyJsonWithDummyWorkingURLs = "{\n" +
            "  \"iss\": \"cdr-register\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"org_id\": \"3B0B0A7B-3E7B-4A2C-9497-E357A71D07C8\",\n" +
            "  \"org_name\": \"Mock Company Inc.\",\n" +
            "  \"client_name\": \"Mock Software\",\n" +
            "  \"client_description\": \"A mock software product for testing SSA\",\n" +
            "  \"client_uri\": \"https://www.google.com\",\n" +
            "  \"logo_uri\": \"https://www.google.com\",\n" +
            "  \"tos_uri\": \"https://www.google.com\",\n" +
            "  \"policy_uri\": \"https://www.google.com\",\n" +
            "  \"software_id\": \"740C368F-ECF9-4D29-A2EA-0514A66B0CDN\",\n" +
            "  \"recipient_base_uri\": \"https://www.mockcompany.com.au\",\n" +
            "  \"jwks_uri\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9b5usDpbNtmxDcTzs" +
            "7GzKp.jwks\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://www.google.com/redirects/redirect1\",\n" +
            "    \"https://www.google.com/redirects/redirect2\"\n" +
            "  ],\n" +
            "  \"software_roles\": \"data-recipient-software-product\",\n" +
            "  \"scope\": \"openid bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read " +
            "bank:payees:read bank:regular_payments:read common:customer.basic:read common:customer.detail:read " +
            "cdr:registration\",\n" +
            "  \"revocation_uri\": \"https://gist.githubusercontent.com/imesh94/3172e2e45757cda08ec2727f90b72c" +
            "ed/raw/ff0d3eabe4cddce47eec0228f592175223dd92b2/wso2-au-dcr-demo.jwks\",\n" +
            "}";

    public static String ssaBodyJsonWithDifferentRedirectUriHostnames = "{\n" +
            "  \"iss\": \"cdr-register\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"org_id\": \"3B0B0A7B-3E7B-4A2C-9497-E357A71D07C8\",\n" +
            "  \"org_name\": \"Mock Company Inc.\",\n" +
            "  \"client_name\": \"Mock Software\",\n" +
            "  \"client_description\": \"A mock software product for testing SSA\",\n" +
            "  \"client_uri\": \"https://www.google.com\",\n" +
            "  \"logo_uri\": \"https://www.google.com\",\n" +
            "  \"tos_uri\": \"https://www.google.com\",\n" +
            "  \"policy_uri\": \"https://www.google.com\",\n" +
            "  \"software_id\": \"740C368F-ECF9-4D29-A2EA-0514A66B0CDN\",\n" +
            "  \"recipient_base_uri\": \"https://www.mockcompany.com.au\",\n" +
            "  \"jwks_uri\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9b5usDpbNtmxDcTzs" +
            "7GzKp.jwks\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://www.wso2.com/redirects/redirect1\",\n" +
            "    \"https://www.google.com/redirects/redirect2\"\n" +
            "  ],\n" +
            "  \"software_roles\": \"data-recipient-software-product\",\n" +
            "  \"scope\": \"openid bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read " +
            "bank:payees:read bank:regular_payments:read common:customer.basic:read common:customer.detail:read " +
            "cdr:registration\",\n" +
            "  \"revocation_uri\": \"https://gist.githubusercontent.com/imesh94/3172e2e45757cda08ec2727f90b72c" +
            "ed/raw/ff0d3eabe4cddce47eec0228f592175223dd92b2/wso2-au-dcr-demo.jwks\",\n" +
            "}";

    public static String registrationRequestJson = "{\n" +
            "  \"iss\": \"740C368F-ECF9-4D29-A2EA-0514A66B0CDN\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"jti\": \"92713892-5514-11e9-8647-d663bd873d93\",\n" +
            "  \"aud\": \"https://localbank.com\",\n" +
            "  \"scope\": \"accounts payments\",\n" +
            "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "  \"grant_types\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"refresh_token\"\n" +
            "  ],\n" +
            "  \"response_types\": [\n" +
            "    \"code id_token\"\n" +
            "  ],\n" +
            "  \"id_token_signed_response_alg\": \"PS256\",\n" +
            "  \"request_object_signing_alg\": \"PS256\",\n" +
            "  \"software_id\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"application_type\": \"web\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://www.google.com/redirects/redirect1\",\n" +
            "    \"https://www.google.com/redirects/redirect2\"\n" +
            "  ],\n" +
            "  \"software_statement\" : " + RegistrationTestConstants.SSA +
            "}";

    public static String extendedRegistrationRequestJson = "{\n" +
            "  \"iss\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"jti\": \"92713892-5514-11e9-8647-d663bd873d93\",\n" +
            "  \"aud\": \"https://localbank.com\",\n" +
            "  \"scope\": \"accounts payments\",\n" +
            "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "  \"grant_types\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"refresh_token\"\n" +
            "  ],\n" +
            "  \"response_types\": [\n" +
            "    \"code id_token\"\n" +
            "  ],\n" +
            "  \"id_token_signed_response_alg\": \"PS256\",\n" +
            "  \"request_object_signing_alg\": \"PS256\",\n" +
            "  \"software_id\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"application_type\": \"web\",\n" +
            "  \"software_statement\" : " + RegistrationTestConstants.SSA +
            "}";
}
