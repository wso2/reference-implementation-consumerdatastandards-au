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
package org.wso2.openbanking.cds.consent.extensions.util;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.util.ArrayList;

/**
 * Constant class for cds consent authorize tests.
 */
public class CDSConsentAuthorizeTestConstants {

    public static final String PERMISSION_SCOPES = "openid bank:accounts.basic:read bank:accounts.detail:read " +
            "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
            "common:customer.detail:read cdr:registration";

    public static final String PAYLOAD = "{\n" +
            "  \"accountIds\": [\"1234\", \"2345\"],\n" +
            "  \"metadata\": {\n" +
            "          \"authorisationId\": \"DummyAuthId\",\n" +
            "        }\n" +
            "}";

    public static final String PAYLOAD_WITHOUT_ACCOUNT_DATA = "{\n" +
            "  \"metadata\": {\n" +
            "          \"authorisationId\": \"DummyAuthId\",\n" +
            "        }\n" +
            "}";

    public static final String PAYLOAD_NON_STRING_ACCOUNT_DATA = "{\n" +
            "  \"accountIds\": [1234, 2345],\n" +
            "  \"metadata\": {\n" +
            "          \"authorisationId\": \"DummyAuthId\",\n" +
            "        }\n" +
            "}";

    public static final String VALID_REQUEST_OBJECT = "eyJraWQiOiJXX1RjblFWY0hBeTIwcTh6Q01jZEJ5cm9vdHciLCJhbGciOiJQ" +
            "UzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsImlzcyI6Im1DbVd0RUhBa0FmU2FCcEtLa" +
            "F9MTEtUUG5Hc2EiLCJzY29wZSI6Im9wZW5pZCBiYW5rOmFjY291bnRzLmJhc2ljOnJlYWQgYmFuazphY2NvdW50cy5kZXRhaWw6cmV" +
            "hZCBiYW5rOnRyYW5zYWN0aW9uczpyZWFkIGJhbms6cGF5ZWVzOnJlYWQgYmFuazpyZWd1bGFyX3BheW1lbnRzOnJlYWQgY29tbW9uO" +
            "mN1c3RvbWVyLmJhc2ljOnJlYWQgY29tbW9uOmN1c3RvbWVyLmRldGFpbDpyZWFkIiwiY2xhaW1zIjp7InNoYXJpbmdfZHVyYXRpb24" +
            "iOjYwMDAwLCJpZF90b2tlbiI6eyJhY3IiOnsidmFsdWVzIjpbInVybjpjZHMuYXU6Y2RyOjMiXSwiZXNzZW50aWFsIjp0cnVlfX0sIn" +
            "VzZXJpbmZvIjp7fX0sInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6Ly93d3cuZ29vZ" +
            "2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIiwic3RhdGUiOiJzdWl0ZSIsImV4cCI6MTYzOTY0MDUzMiwibm9uY2UiOiI4ZmM0Y2Ji" +
            "NC0yODdiLTQyYWEtYTFkMC02N2RjZTZmYzc0NzkiLCJjbGllbnRfaWQiOiJtQ21XdEVIQWtBZlNhQnBLS2hfTExLVFBuR3NhIn0.ssp" +
            "skdrenj8o_6Fc35jeTvqxnvonBncKAA7RWe8r3a_M9OxUAMhzNROxwOtaqkD2FXxx_Ic61ZlQpY-W02arH_hIo0FZGBY33uW-PluO0" +
            "4T2kKuxVWjjJ4s2pB4FAZA7OB-H3Fhr7G1vP8D-eM85C7HluA9DpZK-G6PnBkmwp4A66cBpC7DhRPeuQqWClIqyVYCgKii8uZPPycPx" +
            "TGnJmnNRRScsf_f9zF9eWH5UDnFbXc1CiEY-eCfw4eZYcbgo2uiKXdGY38MyVWY4KfxX76vC1IfLX1MyzHEc-KGKeFaNHB1c-DnBEt9" +
            "sNBXIbs2clSbG7yWXHcx_oJfTp25iLg";

    public static final String VALID_REQUEST_OBJECT_DIFF = "eyJraWQiOiJXX1RjblFWY0hBeTIwcTh6Q01jZEJ5cm9vdHciLCJhbGci" +
            "OiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsImlzcyI6Im1DbVd0RUhBa0FmU2FCcE" +
            "tLaF9MTEtUUG5Hc2EiLCJzY29wZSI6Im9wZW5pZCBiYW5rOmFjY291bnRzLmJhc2ljOnJlYWQgYmFuazphY2NvdW50cy5kZXRhaWw6c" +
            "mVhZCBiYW5rOnRyYW5zYWN0aW9uczpyZWFkIGJhbms6cGF5ZWVzOnJlYWQgYmFuazpyZWd1bGFyX3BheW1lbnRzOnJlYWQgY29tbW9u" +
            "OmN1c3RvbWVyLmJhc2ljOnJlYWQgY29tbW9uOmN1c3RvbWVyLmRldGFpbDpyZWFkIiwiY2xhaW1zIjp7InNoYXJpbmdfZHVyYXRpb24" +
            "iOjMxNjIyNTAwLCJpZF90b2tlbiI6eyJhY3IiOnsidmFsdWVzIjpbInVybjpjZHMuYXU6Y2RyOjMiXSwiZXNzZW50aWFsIjp0cnVlfX" +
            "0sInVzZXJpbmZvIjp7fX0sInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6Ly93d3cuZ" +
            "29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIiwic3RhdGUiOiJzdWl0ZSIsImV4cCI6MTYzOTY0MDUzMiwibm9uY2UiOiI4ZmM0" +
            "Y2JiNC0yODdiLTQyYWEtYTFkMC02N2RjZTZmYzc0NzkiLCJjbGllbnRfaWQiOiJtQ21XdEVIQWtBZlNhQnBLS2hfTExLVFBuR3NhIn0" +
            ".ZMNzwBYHcJIYzjZP24QQaeLWF9SGCNuqbn8leGNUqSfDKPcE6B7V0PeYNEiYosgHzjPN8Exokt7GFBl2QgvWTL4gSTQHboLJYkgzLf" +
            "KPl-LkKOoari7BWjyWBYrCeVTWEsLbvhfC5-9dA9IdVe5QJtxmB6tTP80Jl_YoWywzZFpwbuMCeHgXV-Ex27R_t_MJgbxzJ1IWyaR6_" +
            "CmNo3eCrZhrV-YJHOrrQ2NCdUdGpEZyZ1re5MWLlaat8JaaEJed3_b09INZ4NpkjLCQrWDcYxioJaF8sjFSL6naRsAOdds_sfxzgVe" +
            "fe7VgeXWhe236vs8qcxor-v37_j7PgaRBHw";

    public static final String REQUEST_OBJECT_WITHOUT_SHARING_VAL = "eyJraWQiOiJXX1RjblFWY0hBeTIwcTh6Q01jZEJ5cm9vdHc" +
            "iLCJhbGciOiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsImlzcyI6Im1DbVd0RUhB" +
            "a0FmU2FCcEtLaF9MTEtUUG5Hc2EiLCJzY29wZSI6Im9wZW5pZCBiYW5rOmFjY291bnRzLmJhc2ljOnJlYWQgYmFuazphY2NvdW50cy5" +
            "kZXRhaWw6cmVhZCBiYW5rOnRyYW5zYWN0aW9uczpyZWFkIGJhbms6cGF5ZWVzOnJlYWQgYmFuazpyZWd1bGFyX3BheW1lbnRzOnJlYW" +
            "QgY29tbW9uOmN1c3RvbWVyLmJhc2ljOnJlYWQgY29tbW9uOmN1c3RvbWVyLmRldGFpbDpyZWFkIiwiY2xhaW1zIjp7ImlkX3Rva2VuI" +
            "jp7ImFjciI6eyJ2YWx1ZXMiOlsidXJuOmNkcy5hdTpjZHI6MyJdLCJlc3NlbnRpYWwiOnRydWV9fSwidXNlcmluZm8iOnt9fSwicmVz" +
            "cG9uc2VfdHlwZSI6ImNvZGUgaWRfdG9rZW4iLCJyZWRpcmVjdF91cmkiOiJodHRwczovL3d3dy5nb29nbGUuY29tL3JlZGlyZWN0cy9" +
            "yZWRpcmVjdDEiLCJzdGF0ZSI6InN1aXRlIiwiZXhwIjoxNjM5NjQwNTMyLCJub25jZSI6IjhmYzRjYmI0LTI4N2ItNDJhYS1hMWQwLT" +
            "Y3ZGNlNmZjNzQ3OSIsImNsaWVudF9pZCI6Im1DbVd0RUhBa0FmU2FCcEtLaF9MTEtUUG5Hc2EifQ.Mu9qXKmP6bw3uBmGi5maHcpKyY" +
            "lzyzkbJU3NVCKtlvv-PWvGTB8OPq9kQlAmjQYf2bpfbPww_wNvepl6gb3XwLsMT9p35Lgmz6UzxfG9PFyham0Minv2o7Gp-6PzCklhW" +
            "1GDQjZlSYKfI1up5348yF-Tkj8wW3m5_uNLBd0_YqGyTOu8RCiri_w5DHP9sUjXmbmmbzNwssOBRvEtSLCOfRZq_eQDGVc9T6En954x" +
            "SP45ED8rQ2tyyFOkYx7gW3xvYpT7aCY_AGe7Rh7vhYSJg-Si8dUFz8eE8Xj-SbPByoE7AgXRK1Hb3U6jvMHUHPtSnnLURod4E5OK5gn" +
            "qcTHpbQ";

    public static final String ENCRYPTED_JWT = "eyJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.QZFDDUFzdGy" +
            "w2inAq45QEUGyKt7-GpRJJ9SqRSyvRKLEJLcg2jbKLBBKDCNdru9ZqAZpw21ERkIikQxnt-baxSuFbRS5F6XEEMfDI2zkZSrF7" +
            "dAjOjy2w_Ai54QH2ghFOMqaOFuP1BPWy2K0P8ehg-yaAnPMsrUnqWJpnvBNagGLYJRh7wa96KUCvH_-hFZiKwrRVLmQqI5vFsS" +
            "wHGCAPxlQzJ_7aD55j7gFbYh6RZ_Psn_Es9zlbo3ae0mEKl38Hn2mtpt2XME17h64sbq3ajHZ68fOa13_mM0NZFr66C67DAfnI" +
            "5IyYDWVoGhAdBNo41IkYMPSG7wuGkCSCEDPXg.xveN6xYKl7hkB2im.p2THY1Y7dDVKzsMEeKtPbbcf1lrsB4yomPIifwk0jc2" +
            "y3Y5LM8bi_jHIM4RdYy4kOb9hpXZDX5hvPX3Xbuh6Zx0s4jWNMjhXReWtmg4KyUuUpLm48vM81SEjU64utynBf2YgD1pGJmdRG" +
            "tSJphotPtrcSloPCW51kR5DTujx31yR2P26-6_Pe3ddQnv1ie0M-9sOPojYnv5c_-p8xJfNTtjfycmNh7EMraaH86NpNuAW9h2" +
            "6ZazKfe1xxESEHqg-qfVNt-jZzxCgyrwNn2utPtNiCHyuf-wwvNjU8kFHky4DNPJ5WJlU923e-yYbPtMNNm8eAe8nUG7AUnWkv" +
            "-fq1EqyP6BObP7W_OZB6x1uAFZ5d_tyYMqoUxyLljtRCfJ33_l8KOEmAmSwXulKVvi4gHZy7vPBpCNNwA6U6C_JeGFVCg4IzNT" +
            "6RdiKKVF8sEsqSYkkSk2uTsu9_y75xgDc0Ebx5F0xIzYteFeUTbM-Vs0g25q0RMIUnBK35pQoZokn0-aNOfAn_CL9R4XKlagZ4" +
            "BG_sQ6HyOjf5Ui9Y9AOQ99GjANSUygmk0ivFHawx0u8XLFVaKwOe1Mbh5McNHuMV3dECX2VdyM_QS2hOavM4KAJ9Fnp3YHme_n" +
            "qRKbuvnvDGAeAoSv-WF8LY_GDidUEkZOyPxQrFOyhseC2jcaiU7mES6diPaueQ3yeB6TnNX3JNGfNT5Reir6z2Mi5Gv3_jWEzC" +
            "VZouyHSd0IFWoZJqIRiXyAm63IwmMsuP4VFL1dFizL0LColZic7Vdwkr4W-QozyrUmbSyWy2ntZcw21eK-lTKDZBkxVJWXe9Zb" +
            "4sx3J5pilPFlLTtj8egLwkVu354wGu8e1XmyEeQH2tEyro4lfvT7WzSJoYEOYWulaSP3UWRU40LTT_V6AWA2IWlsixr9GuJDfY" +
            "und5FEyrunqm7ojfmVxgM52-0exwPuN-T49Gw2kai7Y31DRpT1ZqJROL5UJk4mi6Vb7bWMhb0PC-e9enWlDBYGtX7ulNIhBR9V" +
            "K87ZB5l659kynAMZ8CrwPrasv-dsDOsaK_PEWdeUbx0GGm80JbOzpAeYEnrHzedfoNIdQs9ecK5QrUui9rYiUiC3f-w24EGl32" +
            "0-g8GayH0PGMpq9zHFB0SfaPjuR-oxa9kn1x-lfeqlHGm0R21BIaJIiN2Bp7B8NWMPv7OB_jFEyWWhqNZUEDQ0evkMRI-evvDA" +
            "2U3gW9GtQ_ap-7Zb-78G29mdgL9tfxyJSf8xbYvLbGfWiI0cacCA-1WubwBrX6RTLiibMJZcn8JPC27ZXRC3FIcBo85s7JJ3qj" +
            "L-8gjhLNaUnDHDX5ieZYb8sFGUUIRFMuIkZDHB1u_-FZETiQcLXxMoiQS6-sXH8DVVVmMaahtz763Pfnh3ilSmZPZ8XUkvn4Qb" +
            "m25RbWU3FISjlOYk_1Cm7K0Z6rL1kNGEcb1O8jFjHuhWa7989jCnuuT73yxyduyfU1yBoprBCzWy-7zL6meSuTqcbj7Bb-g9LB" +
            "oVVQ8e4XmZMXQPnmyFPzQoveDrIriLg3IHVmqsDwrIF63YWmWxzHvoczMt8CV5yYiX--BXSXteTa8fua1rqh82xFLFcmFv4S8P" +
            "yV1hFUhVTD3i8WqHTCT6PfHb-KwWS2sIPqARi8ILAXx5HHDyCrwaBJq2e78GSQhH785aGutcwSDlBrhmSplsNmkQghJf2bhRB4" +
            "Oi6-BRxJAgOXr2J8NjyD-jXo52TTWKU5kPlRsZyoDBpjZ9RQP2ssxdc_TQsKQdzHeSNdIoD6rpwY7qWkhnbv69Re0oIPTpPvAJ" +
            "_7_UW7NvUDFu56xN3.U1nq0A6rc8mdTdb1iL2s3w";

    public static final String VALID_AMENDMENT_REQUEST_OBJECT = "eyJraWQiOiJXX1RjblFWY0hBeTIwcTh6Q01jZEJ5cm9vdH" +
            "ciLCJhbGciOiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsImlzcyI6Im1DbVd" +
            "0RUhBa0FmU2FCcEtLaF9MTEtUUG5Hc2EiLCJzY29wZSI6Im9wZW5pZCBiYW5rOmFjY291bnRzLmJhc2ljOnJlYWQgYmFuazphY" +
            "2NvdW50cy5kZXRhaWw6cmVhZCBiYW5rOnRyYW5zYWN0aW9uczpyZWFkIGJhbms6cGF5ZWVzOnJlYWQgYmFuazpyZWd1bGFyX3B" +
            "heW1lbnRzOnJlYWQgY29tbW9uOmN1c3RvbWVyLmJhc2ljOnJlYWQgY29tbW9uOmN1c3RvbWVyLmRldGFpbDpyZWFkIiwiY2xha" +
            "W1zIjp7InNoYXJpbmdfZHVyYXRpb24iOjYwMDAwLCJjZHJfYXJyYW5nZW1lbnRfaWQiOiI2M2JjMjJhYy02ZmQyLTRlODUtYTk" +
            "3OS1jMmZjN2M0ZGI5ZGEiLCJpZF90b2tlbiI6eyJhY3IiOnsidmFsdWVzIjpbInVybjpjZHMuYXU6Y2RyOjMiXSwiZXNzZW50a" +
            "WFsIjp0cnVlfX0sInVzZXJpbmZvIjp7fX0sInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjo" +
            "iaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIiwic3RhdGUiOiJzdWl0ZSIsImV4cCI6MTYzOTY0M" +
            "DUzMiwibm9uY2UiOiI4ZmM0Y2JiNC0yODdiLTQyYWEtYTFkMC02N2RjZTZmYzc0NzkiLCJjbGllbnRfaWQiOiJtQ21XdEVIQWt" +
            "BZlNhQnBLS2hfTExLVFBuR3NhIn0.ZWmudH6Ob5VJOLYblt_oOl5ZNxnzKG2nDOuFf5QDrnuI53RIy57rsKVe9rVdIRFh1H23e" +
            "wgQC49SMugS6lhiijOrUAFd8gKgt_brZMcr02vP37_lUDVT_5Lt_-koTYfohcODnneeSWVUL8DP8OVHMtMk7qUTHVkKAT362to" +
            "8zpbqX1m9jB3RSbli1pA6-AelMT9mEn5nSxQJhjczo7dZ8ulgNee03sHe9jw5vTha3nYDgxty-6xmLovl62mblGzgidkbSvj5c" +
            "pz_DtZPCFsaxNRipjiXlcRpz7kHrziCQBOnphtwm-GQWHDziu9_F_SFC-h0zYpBY7zb03bV2CT0rQ";

    public static DetailedConsentResource getDetailedConsentResource() {
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID("1234");
        detailedConsentResource.setCurrentStatus("awaitingAuthorization");
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setUpdatedTime(System.currentTimeMillis() / 1000);
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(CDSConsentAuthorizeTestConstants.getAuthorizationResource());
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(new ArrayList<>());

        return detailedConsentResource;
    }

    public static AuthorizationResource getAuthorizationResource() {
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID("DummyAuthId");
        authorizationResource.setAuthorizationStatus("created");
        authorizationResource.setUpdatedTime((long) 163979797);
        return authorizationResource;
    }
}
