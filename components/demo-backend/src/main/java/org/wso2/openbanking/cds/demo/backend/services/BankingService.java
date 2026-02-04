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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.demo.backend.model.RequestAccountIds;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Banking Service.
 */
@Path("/")
public class BankingService {

    private static final String XV_HEADER = "x-v";
    private static final String INTERACTION_ID_HEADER = "x-fapi-interaction-id";
    private static final String ACCOUNT_INFO_HEADER = "Account-Request-Information";
    private static JsonParser jsonParser = new JsonParser();

    private static final Log log =
            LogFactory.getLog(BankingService.class);

    @GET
    @Path("/accounts")
    @Produces("application/json")
    public Response getAccounts(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                @HeaderParam(XV_HEADER) String apiVersion,
                                @HeaderParam(ACCOUNT_INFO_HEADER) String accountRequestInfo) throws ParseException {

        JSONObject accountRequestInformation = getRequest(accountRequestInfo);
        List<String> accountRequestIds = getAccountIds(accountRequestInformation);
        JsonObject data = new JsonObject();
        JsonArray accounts = new JsonArray();

        for (String accountId : accountRequestIds) {
            accounts.add(getSampleAccount(accountId));
        }

        data.add("accounts", accounts);
        String response = getResponse(data, getSampleLinksPaginated("/accounts"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/balances")
    @Produces("application/json")
    public Response getBulkBalances(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                    @HeaderParam(XV_HEADER) String apiVersion,
                                    @HeaderParam(ACCOUNT_INFO_HEADER) String accountRequestInfo) throws ParseException {

        JSONObject accountRequestInformation = getRequest(accountRequestInfo);
        List<String> accountRequestIds = getAccountIds(accountRequestInformation);
        JsonObject data = new JsonObject();
        JsonArray balances = new JsonArray();


        for (String accountId : accountRequestIds) {
            balances.add(getSampleBalance(accountId));
        }

        data.add("balances", balances);
        String response = getResponse(data, getSampleLinksPaginated("/accounts/balances"),
                getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @POST
    @Path("/accounts/balances")
    @Produces("application/json")
    public Response getBalancesForSpecificAccounts(String requestString,
                                                   @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                                   @HeaderParam(XV_HEADER) String apiVersion) {

        Gson gson = new Gson();
        RequestAccountIds requestAccountIds = gson.fromJson(requestString, RequestAccountIds.class);

        JsonObject data = new JsonObject();
        JsonArray balances = new JsonArray();

        try {
            for (String accountID : requestAccountIds.getData().getAccountIds()) {
                JsonObject balanceItem = getSampleBalance(accountID);
                balances.add(balanceItem);
            }
        } catch (NullPointerException e) {
            return Response.status(400).entity(getErrorResponseForInvalidRequestPayload().toString()).header
                    (XV_HEADER, apiVersion).build();
        }
        data.add("balances", balances);

        String response = getResponse(data, getSampleLinksPaginated("/accounts/balances"),
                getSampleMetaPaginated());
        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}/balance")
    @Produces("application/json")
    public Response getAccountBalance(@PathParam("accountId") String accountID,
                                      @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                      @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject balanceItem = getSampleBalance(accountID);
        String response = getResponse(balanceItem,
                getSampleLinks("/accounts/" + accountID + "/balance"),
                new JsonObject());
        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}")
    @Produces("application/json")
    public Response getAccountDetail(@PathParam("accountId") String accountID,
                                     @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                     @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject accountsItem = getSampleAccountDetail(accountID);
        String response = getResponse(accountsItem,
                getSampleLinks("/accounts/" + accountID), new JsonObject());
        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}/transactions")
    @Produces("application/json")
    public Response getTransactionsForAccount(@PathParam("accountId") String accountID,
                                              @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                              @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray transactions = new JsonArray();
        JsonObject transactionItem = getSampleTransaction(accountID);
        transactions.add(transactionItem);
        data.add("transactions", transactions);
        String response = getResponse(data,
                getSampleLinksPaginated("/accounts/" + accountID + "/transactions"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}/transactions/{transactionId}")
    @Produces("application/json")
    public Response getTransactionDetail(@PathParam("accountId") String accountID, @PathParam("transactionId") String
            transactionID, @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                         @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject transactionDetail = getSampleTransactionDetail(accountID, transactionID);
        String response = getResponse(transactionDetail,
                getSampleLinks("/accounts/" + accountID + "/transactions/" + transactionID),
                new JsonObject());
        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}/direct-debits")
    @Produces("application/json")
    public Response getDirectDebitsForAccount(@PathParam("accountId") String accountID,
                                              @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                              @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray directDebits = new JsonArray();
        JsonObject directDebitItem = getSampleDirectDebit(accountID);
        directDebits.add(directDebitItem);
        data.add("directDebitAuthorisations", directDebits);
        String response = getResponse(data,
                getSampleLinksPaginated("/accounts/" + accountID + "/direct-debits"),
                getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/direct-debits")
    @Produces("application/json")
    public Response getBulkDirectDebits(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                        @HeaderParam(XV_HEADER) String apiVersion,
                                        @HeaderParam(ACCOUNT_INFO_HEADER) String accountRequestInfo)
            throws ParseException {

        JSONObject accountRequestInformation = getRequest(accountRequestInfo);
        List<String> accountRequestIds = getAccountIds(accountRequestInformation);
        JsonObject data = new JsonObject();
        JsonArray directDebits = new JsonArray();

        for (String accountId : accountRequestIds) {
            directDebits.add(getSampleDirectDebit(accountId));
        }

        data.add("directDebitAuthorisations", directDebits);
        String response = getResponse(data,
                getSampleLinksPaginated("/accounts/direct-debits"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @POST
    @Path("/accounts/direct-debits")
    @Produces("application/json")
    public Response getBulkDirectDebitsForSpecificAccounts(String requestString,
                                                       @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                                       @HeaderParam(XV_HEADER) String apiVersion) {

        Gson gson = new Gson();
        RequestAccountIds requestAccountIds = gson.fromJson(requestString, RequestAccountIds.class);

        JsonObject data = new JsonObject();
        JsonArray directDebits = new JsonArray();
        try {
            for (String accountID : requestAccountIds.getData().getAccountIds()) {
                JsonObject directDebitItem = getSampleDirectDebit(accountID);
                directDebits.add(directDebitItem);
            }
        } catch (NullPointerException e) {
            return Response.status(400).entity(getErrorResponseForInvalidRequestPayload().toString()).header
                    (XV_HEADER, apiVersion).build();
        }

        data.add("directDebitAuthorisations", directDebits);
        String response = getResponse(data,
                getSampleLinksPaginated("/accounts/direct-debits"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/accounts/{accountId}/payments/scheduled")
    @Produces("application/json")
    public Response getScheduledPaymentsForAccount(@PathParam("accountId") String accountID,
                                                   @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                                   @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray scheduledPayments = new JsonArray();
        JsonObject scheduledPaymentsItem = getSampleScheduledPayment(accountID);
        scheduledPayments.add(scheduledPaymentsItem);
        data.add("scheduledPayments", scheduledPayments);
        String response = getResponse(data,
                getSampleLinksPaginated("/accounts/" + accountID + "/payments/scheduled"),
                getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/payments/scheduled")
    @Produces("application/json")
    public Response getBulkScheduledPayments(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                             @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray scheduledPayments = new JsonArray();
        JsonObject scheduledPaymentsItem1 = getSampleScheduledPayment("01234");
        JsonObject scheduledPaymentsItem2 = getSampleScheduledPayment("56789");
        scheduledPayments.add(scheduledPaymentsItem1);
        scheduledPayments.add(scheduledPaymentsItem2);
        data.add("scheduledPayments", scheduledPayments);
        String response = getResponse(data,
                getSampleLinksPaginated("/payments/scheduled"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @POST
    @Path("/payments/scheduled")
    @Produces("application/json")
    public Response getScheduledPaymentsForSpecificAccounts(String requestString,
                                                        @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                                        @HeaderParam(XV_HEADER) String apiVersion) {
        Gson gson = new Gson();
        RequestAccountIds requestAccountIds = gson.fromJson(requestString, RequestAccountIds.class);

        JsonObject data = new JsonObject();
        JsonArray scheduledPayments = new JsonArray();
        try {
            for (String accountID : requestAccountIds.getData().getAccountIds()) {
                JsonObject scheduledPaymentsItem = getSampleScheduledPayment(accountID);
                scheduledPayments.add(scheduledPaymentsItem);
            }
        } catch (NullPointerException e) {
            return Response.status(400).entity(getErrorResponseForInvalidRequestPayload().toString()).header
                    (XV_HEADER, apiVersion).build();
        }
        data.add("scheduledPayments", scheduledPayments);
        String response = getResponse(data,
                getSampleLinksPaginated("/payments/scheduled"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/payees")
    @Produces("application/json")
    public Response getPayees(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                              @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray payees = new JsonArray();
        JsonObject payeeItem = getSamplePayee();
        payees.add(payeeItem);
        data.add("payees", payees);
        String response = getResponse(data, getSampleLinksPaginated("/payees"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/payees/{payeeId}")
    @Produces("application/json")
    public Response getPayeeDetail(@PathParam("payeeId") String payeeID,
                                   @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                   @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject payee = getSamplePayeeDetail(payeeID);
        String response = getResponse(payee, getSampleLinks("/payees/" + payeeID), new JsonObject());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/products")
    @Produces("application/json")
    public Response getProducts(@HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject data = new JsonObject();
        JsonArray products = new JsonArray();
        JsonObject productItem = getSampleProduct();
        products.add(productItem);
        data.add("products", products);
        String response = getResponse(data, getSampleLinksPaginated("/products"), getSampleMetaPaginated());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    @GET
    @Path("/products/{productId}")
    @Produces("application/json")
    public Response getProductDetail(@PathParam("productId") String productID,
                                     @HeaderParam(INTERACTION_ID_HEADER) String xFapiInteractionId,
                                     @HeaderParam(XV_HEADER) String apiVersion) {

        JsonObject product = getSampleProductDetail(productID);
        String response = getResponse(product, getSampleLinks("/products/" + productID), new JsonObject());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion)
                .header(INTERACTION_ID_HEADER, xFapiInteractionId).build();
    }

    protected static String getResponse(JsonObject data, JsonObject links, JsonObject meta) {

        JsonObject response = new JsonObject();
        response.add("data", data);
        response.add("links", links);
        response.add("meta", meta);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedResponse = gson.toJson(response);
        return formattedResponse;
    }

    protected static JsonObject getSampleLinks(String requestUrl) {

        String linksJson = " {  \n" +
                "      \"self\":\"https://api.alphabank.com/cds-au/v1/banking" + requestUrl + "\"\n" +
                "   }\n";
        return jsonParser.parse(linksJson).getAsJsonObject();
    }

    private static JsonObject getSampleLinksPaginated(String requestUrl) {

        String linksJson = " {  \n" +
                "      \"self\":\"https://api.alphabank.com/cds-au/1.2.0/banking" + requestUrl + "\",\n" +
                "      \"first\":\"https://api.alphabank.com/cds-au/1.2.0/banking" + requestUrl + "/1\",\n" +
                "      \"prev\":\"https://api.alphabank.com/cds-au/1.2.0/banking" + requestUrl + "/1\",\n" +
                "      \"next\":\"https://api.alphabank.com/cds-au/1.2.0/banking" + requestUrl + "/2\",\n" +
                "      \"last\":\"https://api.alphabank.com/cds-au/1.2.0/banking" + requestUrl + "/3\"\n" +
                "   }\n";
        return jsonParser.parse(linksJson).getAsJsonObject();
    }

    private static JsonObject getSampleMetaPaginated() {

        String metaJson = "{  \n" +
                "      \"totalRecords\":10,\n" +
                "      \"totalPages\":10\n" +
                "   }\n";
        return jsonParser.parse(metaJson).getAsJsonObject();
    }

    private JsonObject getSampleAccount(String accountId) {

        String accountJson = "{\n" +
                "        \"accountId\": \"" + accountId + "\",\n" +
                "        \"creationDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "        \"displayName\": \"account_1\",\n" +
                "        \"nickname\": \"Alpha\",\n" +
                "        \"openStatus\": \"OPEN\",\n" +
                "        \"isOwned\": true,\n" +
                "        \"maskedNumber\": \"1234\",\n" +
                "        \"productCategory\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
                "        \"accountOwnership\": \"UNKNOWN\",\n" +
                "        \"productName\": \"Product name\"\n" +
                "      }";
        return jsonParser.parse(accountJson).getAsJsonObject();
    }

    private JsonObject getSampleAccountDetail(String accountID) {

        String accountJson = "{\n" +
                "    \"accountId\": \"" + accountID + "\",\n" +
                "    \"creationDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"displayName\": \"account_1\",\n" +
                "    \"nickname\": \"Alpha\",\n" +
                "    \"openStatus\": \"OPEN\",\n" +
                "    \"isOwned\": true,\n" +
                "    \"maskedNumber\": \"1234\",\n" +
                "    \"productCategory\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
                "    \"accountOwnership\": \"UNKNOWN\",\n" +
                "    \"productName\": \"Product name\",\n" +
                "    \"bsb\": \"bsb\",\n" +
                "    \"accountNumber\": \"12345\",\n" +
                "    \"bundleName\": \"bundleName\",\n" +
                "    \"specificAccountUType\": \"termDeposit\",\n" +
                "    \"termDeposit\": [" +
                "      {\n" +
                "        \"lodgementDate\": \"2019-09-01T15:43:00.12345Z\",\n" +
                "        \"maturityDate\": \"2019-09-01T15:43:00.12345Z\",\n" +
                "        \"maturityAmount\": \"200.00\",\n" +
                "        \"maturityCurrency\": \"AUD\",\n" +
                "        \"maturityInstructions\": \"ROLLED_OVER\"\n" +
                "      }" +
                "    ],\n" +
                "    \"loan\": {\n" +
                "       \"originalStartDate\": \"2019-09-01T15:43:00.12345Z\",\n" +
                "       \"originalLoanAmount\": \"200.00\",\n" +
                "       \"originalLoanCurrency\": \"AUD\",\n" +
                "       \"loanEndDate\": \"2020-09-01T15:43:00.12345Z\",\n" +
                "       \"nextInstalmentDate\": \"2019-10-01T15:43:00.12345Z\",\n" +
                "       \"minInstalmentAmount\": \"10.00\",\n" +
                "       \"minInstalmentCurrency\": \"AUD\",\n" +
                "       \"maxRedraw\": \"200.00\",\n" +
                "       \"maxRedrawCurrency\": \"AUD\",\n" +
                "       \"minRedraw\": \"10.00\",\n" +
                "       \"minRedrawCurrency\": \"AUD\",\n" +
                "       \"offsetAccountEnabled\": true,\n" +
                "       \"offsetAccountIds\": [\n" +
                "           \"1111\",\n" +
                "           \"2222\"\n" +
                "       ],\n" +
                "       \"repaymentType\": \"INTEREST_ONLY\"\n" +
                "       },\n" +
                "    \"depositRate\": \"23\",\n" +
                "    \"lendingRate\": \"34\",\n" +
                "    \"depositRates\": [\n" +
                "      {\n" +
                "        \"depositRateType\": \"FIXED\",\n" +
                "        \"rate\": \"43\",\n" +
                "        \"calculationFrequency\": \"111\",\n" +
                "        \"applicationFrequency\": \"222\",\n" +
                "        \"tiers\": [\n" +
                "          {\n" +
                "            \"name\": \"string\",\n" +
                "            \"unitOfMeasure\": \"DOLLAR\",\n" +
                "            \"minimumValue\": 0,\n" +
                "            \"maximumValue\": 0,\n" +
                "            \"rateApplicationMethod\": \"WHOLE_BALANCE\",\n" +
                "            \"subTier\": {\n" +
                "              \"name\": \"name\",\n" +
                "              \"unitOfMeasure\": \"DOLLAR\",\n" +
                "              \"minimumValue\": 0,\n" +
                "              \"maximumValue\": 0,\n" +
                "              \"rateApplicationMethod\": \"WHOLE_BALANCE\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"features\": [\n" +
                "      {\n" +
                "        \"featureType\": \"CARD_ACCESS\",\n" +
                "        \"isActivated\": true\n" +
                "      }\n" +
                "    ],\n" +
                "    \"fees\": [\n" +
                "      {\n" +
                "        \"name\": \"fee1\",\n" +
                "        \"feeType\": \"PERIODIC\",\n" +
                "        \"amount\": \"10.00\",\n" +
                "        \"balanceRate\": \"82\",\n" +
                "        \"transactionRate\": \"32\",\n" +
                "        \"accruedRate\": \"34\",\n" +
                "        \"accrualFrequency\": \"2234\",\n" +
                "        \"currency\": \"AUD\",\n" +
                "        \"discounts\": [\n" +
                "          {\n" +
                "            \"description\": \"Description\",\n" +
                "            \"discountType\": \"BALANCE\",\n" +
                "            \"amount\": \"34.00\",\n" +
                "            \"balanceRate\": \"82\",\n" +
                "            \"transactionRate\": \"34\",\n" +
                "            \"accruedRate\": \"23\",\n" +
                "            \"feeRate\": \"45\",\n" +
                "            \"eligibility\": [\n" +
                "              {\n" +
                "                \"discountEligibilityType\": \"BUSINESS\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }";
        return jsonParser.parse(accountJson).getAsJsonObject();
    }

    private JsonObject getSampleBalance(String accountID) {

        String balanceJson = "{\n" +
                "        \"accountId\": \"" + accountID + "\",\n" +
                "        \"currentBalance\": \"1234567.89\",\n" +
                "        \"availableBalance\": \"10234567.89\",\n" +
                "        \"creditLimit\": \"100234567.89\",\n" +
                "        \"amortisedLimit\": \"1034567.89\",\n" +
                "        \"currency\": \"AUD\",\n" +
                "        \"purses\": [\n" +
                "          {\n" +
                "            \"amount\": \"10.89\",\n" +
                "            \"currency\": \"AUD\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }";
        return jsonParser.parse(balanceJson).getAsJsonObject();
    }

    private JsonObject getSampleTransaction(String accountID) {

        String transactionJson = "{\n" +
                "        \"accountId\": \"" + accountID + "\",\n" +
                "        \"transactionId\": \"001234\",\n" +
                "        \"isDetailAvailable\": true,\n" +
                "        \"type\": \"FEE\",\n" +
                "        \"status\": \"PENDING\",\n" +
                "        \"description\": \"Desc\",\n" +
                "        \"postingDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "        \"valueDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "        \"executionDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "        \"amount\": \"10.00\",\n" +
                "        \"currency\": \"AUD\",\n" +
                "        \"reference\": \"reference\",\n" +
                "        \"merchantName\": \"merchant abc\",\n" +
                "        \"merchantCategoryCode\": \"xyz\",\n" +
                "        \"billerCode\": \"abc\",\n" +
                "        \"billerName\": \"biller abc\",\n" +
                "        \"crn\": \"crn\",\n" +
                "        \"apcaNumber\": \"123\"\n" +
                "      }";
        return jsonParser.parse(transactionJson).getAsJsonObject();
    }

    private JsonObject getSampleTransactionDetail(String accountID, String transactionID) {

        String transactionJson = "{\n" +
                "    \"accountId\": \"" + accountID + "\",\n" +
                "    \"transactionId\": \"" + transactionID + "\",\n" +
                "    \"isDetailAvailable\": true,\n" +
                "    \"type\": \"FEE\",\n" +
                "    \"status\": \"PENDING\",\n" +
                "    \"description\": \"Desc\",\n" +
                "    \"postingDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"valueDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"executionDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"amount\": \"10.00\",\n" +
                "    \"currency\": \"AUD\",\n" +
                "    \"reference\": \"reference\",\n" +
                "    \"merchantName\": \"merchant abc\",\n" +
                "    \"merchantCategoryCode\": \"xyz\",\n" +
                "    \"billerCode\": \"abc\",\n" +
                "    \"billerName\": \"biller abc\",\n" +
                "    \"crn\": \"crn\",\n" +
                "    \"apcaNumber\": \"123\",\n" +
                "    \"extendedData\": {\n" +
                "      \"payer\": \"payer 01\",\n" +
                "      \"payee\": \"payee 01\",\n" +
                "      \"extensionUType\": \"x2p101Payload\",\n" +
                "      \"x2p101Payload\": {\n" +
                "        \"extendedDescription\": \"ext desc\",\n" +
                "        \"endToEndId\": \"0987\",\n" +
                "        \"purposeCode\": \"0456\"\n" +
                "      },\n" +
                "      \"service\": \"X2P1.01\"\n" +
                "    }\n" +
                "  }";
        return jsonParser.parse(transactionJson).getAsJsonObject();
    }

    private JsonObject getSampleDirectDebit(String accountID) {

        String directDebitJson = "{\n" +
                "        \"accountId\": \"" + accountID + "\",\n" +
                "        \"authorisedEntity\": {\n" +
                "          \"description\": \"authorized entity desc\",\n" +
                "          \"financialInstitution\": \"financialInstitution abc\",\n" +
                "          \"abn\": \"abn\",\n" +
                "          \"acn\": \"acn\",\n" +
                "          \"arbn\": \"arbn\"\n" +
                "        },\n" +
                "        \"lastDebitDateTime\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "        \"lastDebitAmount\": \"10.00\"\n" +
                "      }";
        return jsonParser.parse(directDebitJson).getAsJsonObject();
    }

    private JsonObject getSampleScheduledPayment(String accountID) {

        String scheduledPaymentJson = "{\n" +
                "        \"scheduledPaymentId\": \"0123\",\n" +
                "        \"nickname\": \"nickname\",\n" +
                "        \"payerReference\": \"payerReference\",\n" +
                "        \"payeeReference\": \"payeeReference\",\n" +
                "        \"status\": \"ACTIVE\",\n" +
                "        \"from\": {\n" +
                "          \"accountId\": \"" + accountID + "\"\n" +
                "        },\n" +
                "        \"paymentSet\": [\n" +
                "          {\n" +
                "            \"to\": {\n" +
                "              \"toUType\": \"biller\",\n" +
                "              \"accountId\": \"2345\",\n" +
                "              \"payeeId\": \"435f\",\n" +
                "              \"domestic\": {\n" +
                "                \"payeeAccountUType\": \"card\",\n" +
                "                \"account\": {\n" +
                "                  \"accountName\": \"accountName\",\n" +
                "                  \"bsb\": \"bsb\",\n" +
                "                  \"accountNumber\": \"accountNumber\"\n" +
                "                },\n" +
                "                \"card\": {\n" +
                "                  \"cardNumber\": \"xxxx xxxx xxxx 1234\"\n" +
                "                },\n" +
                "                \"payId\": {\n" +
                "                  \"name\": \"name\",\n" +
                "                  \"identifier\": \"0192837\",\n" +
                "                  \"type\": \"EMAIL\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"biller\": {\n" +
                "                \"billerCode\": \"1019\",\n" +
                "                \"crn\": \"crn\",\n" +
                "                \"billerName\": \"billerName\"\n" +
                "              },\n" +
                "              \"international\": {\n" +
                "                \"beneficiaryDetails\": {\n" +
                "                  \"name\": \"name\",\n" +
                "                  \"country\": \"country\",\n" +
                "                  \"message\": \"message\"\n" +
                "                },\n" +
                "                \"bankDetails\": {\n" +
                "                  \"country\": \"country\",\n" +
                "                  \"accountNumber\": \"123456\",\n" +
                "                  \"bankAddress\": {\n" +
                "                    \"name\": \"alpha bank\",\n" +
                "                    \"address\": \"a123\"\n" +
                "                  },\n" +
                "                  \"beneficiaryBankBIC\": \"beneficiaryBankBIC\",\n" +
                "                  \"fedWireNumber\": \"fedWireNumber\",\n" +
                "                  \"sortCode\": \"sortCode\",\n" +
                "                  \"chipNumber\": \"chipNumber\",\n" +
                "                  \"routingNumber\": \"routingNumber\",\n" +
                "                  \"legalEntityIdentifier\": \"legalEntityIdentifier\"\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            \"isAmountCalculated\": true,\n" +
                "            \"amount\": \"90.00\",\n" +
                "            \"currency\": \"AUD\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"recurrence\": {\n" +
                "          \"nextPaymentDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "          \"recurrenceUType\": \"onceOff\",\n" +
                "          \"onceOff\": {\n" +
                "            \"paymentDate\": \"2019-05-01T15:43:00.12345Z\"\n" +
                "          },\n" +
                "          \"intervalSchedule\": {\n" +
                "            \"finalPaymentDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "            \"paymentsRemaining\": 0,\n" +
                "            \"nonBusinessDayTreatment\": \"ONLY\",\n" +
                "            \"intervals\": [\n" +
                "              {\n" +
                "                \"interval\": \"interval\",\n" +
                "                \"dayInInterval\": \"dayInInterval\"\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          \"lastWeekDay\": {\n" +
                "            \"finalPaymentDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "            \"paymentsRemaining\": 0,\n" +
                "            \"interval\": \"interval\",\n" +
                "            \"lastWeekDay\": \"FRI\"\n" +
                "          },\n" +
                "          \"eventBased\": {\n" +
                "            \"description\": \"desc\"\n" +
                "          }\n" +
                "        }\n" +
                "      }";
        return jsonParser.parse(scheduledPaymentJson).getAsJsonObject();
    }

    private JsonObject getSamplePayee() {

        int randomInt = 1000 + new Random().nextInt(90000);
        String payeeJson = "{\n" +
                "        \"payeeId\": \"" + randomInt + "\",\n" +
                "        \"nickname\": \"nickname\",\n" +
                "        \"description\": \"description\",\n" +
                "        \"type\": \"DOMESTIC\",\n" +
                "        \"creationDate\": \"2019-05-01T15:43:00.12345Z\"\n" +
                "      }";
        return jsonParser.parse(payeeJson).getAsJsonObject();
    }

    private JsonObject getSamplePayeeDetail(String payeeID) {

        String payeeJson = "{\n" +
                "    \"payeeId\": \"" + payeeID + "\",\n" +
                "    \"nickname\": \"nickname\",\n" +
                "    \"description\": \"description\",\n" +
                "    \"type\": \"DOMESTIC\",\n" +
                "    \"creationDate\": \"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"payeeUType\": \"domestic\",\n" +
                "    \"domestic\": {\n" +
                "      \"payeeAccountUType\": \"account\",\n" +
                "      \"account\": {\n" +
                "        \"accountName\": \"1234\",\n" +
                "        \"bsb\": \"bsb\",\n" +
                "        \"accountNumber\": \"012345\"\n" +
                "      },\n" +
                "      \"card\": {\n" +
                "        \"cardNumber\": \"xxxx xxxx xxxx 1234\"\n" +
                "      },\n" +
                "      \"payId\": {\n" +
                "        \"name\": \"name\",\n" +
                "        \"identifier\": \"identifier\",\n" +
                "        \"type\": \"EMAIL\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"biller\": {\n" +
                "      \"billerCode\": \"billerCode\",\n" +
                "      \"crn\": \"crn\",\n" +
                "      \"billerName\": \"billerName\"\n" +
                "    },\n" +
                "    \"international\": {\n" +
                "      \"beneficiaryDetails\": {\n" +
                "        \"name\": \"name\",\n" +
                "        \"country\": \"country\",\n" +
                "        \"message\": \"message\"\n" +
                "      },\n" +
                "      \"bankDetails\": {\n" +
                "        \"country\": \"country\",\n" +
                "        \"accountNumber\": \"accountNumber\",\n" +
                "        \"bankAddress\": {\n" +
                "          \"name\": \"name\",\n" +
                "          \"address\": \"address\"\n" +
                "        },\n" +
                "        \"beneficiaryBankBIC\": \"beneficiaryBankBIC\",\n" +
                "        \"fedWireNumber\": \"fedWireNumber\",\n" +
                "        \"sortCode\": \"sortCode\",\n" +
                "        \"chipNumber\": \"chipNumber\",\n" +
                "        \"routingNumber\": \"routingNumber\",\n" +
                "        \"legalEntityIdentifier\": \"legalEntityIdentifier\"\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        return jsonParser.parse(payeeJson).getAsJsonObject();
    }

    private JsonObject getSampleProduct() {

        int randomInt = 1000 + new Random().nextInt(90000);
        String productJson = "         {  \n" +
                "            \"productId\":\"" + randomInt + "\",\n" +
                "            \"effectiveFrom\":\"CURRENT\",\n" +
                "            \"effectiveTo\":\"2019-09-01T15:43:00.12345Z\",\n" +
                "            \"lastUpdated\":\"2019-05-01T15:43:00.12345Z\",\n" +
                "            \"productCategory\":\"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
                "            \"name\":\"Product name\",\n" +
                "            \"description\":\"Product description\",\n" +
                "            \"brand\":\"Product brand\",\n" +
                "            \"brandName\":\"Product brandName\",\n" +
                "            \"applicationUri\":\"http://www.wso2.com/applicationUri\",\n" +
                "            \"isTailored\":true,\n" +
                "            \"additionalInformation\":{  \n" +
                "               \"overviewUri\":\"http://www.wso2.com/overview\",\n" +
                "               \"termsUri\":\"http://www.wso2.com/terms\",\n" +
                "               \"eligibilityUri\":\"http://www.wso2.com/eligibility\",\n" +
                "               \"feesAndPricingUri\":\"http://www.wso2.com/fees\",\n" +
                "               \"bundleUri\":\"http://www.wso2.com/bundle\"\n" +
                "            }\n" +
                "         }\n";
        return jsonParser.parse(productJson).getAsJsonObject();
    }

    private JsonObject getSampleProductDetail(String productID) {

        String productJson = "{\n" +
                "    \"productId\": \"" + productID + "\",\n" +
                "    \"effectiveFrom\":\"CURRENT\",\n" +
                "    \"effectiveTo\":\"2019-09-01T15:43:00.12345Z\",\n" +
                "    \"lastUpdated\":\"2019-05-01T15:43:00.12345Z\",\n" +
                "    \"productCategory\":\"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
                "    \"name\":\"Product name\",\n" +
                "    \"description\":\"Product description\",\n" +
                "    \"brand\":\"Product brand\",\n" +
                "    \"brandName\":\"Product brandName\",\n" +
                "    \"applicationUri\":\"http://www.wso2.com/applicationUri\",\n" +
                "    \"isTailored\":true,\n" +
                "    \"bundles\": [\n" +
                "      {\n" +
                "        \"name\": \"bundle name\",\n" +
                "        \"description\": \"bundle description\",\n" +
                "        \"productIds\": [\n" +
                "          \"0987\"\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"features\": [\n" +
                "      {\n" +
                "        \"featureType\": \"CARD_ACCESS\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"constraints\": [\n" +
                "      {\n" +
                "        \"constraintType\": \"MIN_BALANCE\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"eligibility\": [\n" +
                "      {\n" +
                "        \"eligibilityType\": \"BUSINESS\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"fees\": [\n" +
                "      {\n" +
                "        \"name\": \"fee1\",\n" +
                "        \"feeType\": \"PERIODIC\",\n" +
                "        \"amount\": \"10.00\",\n" +
                "        \"balanceRate\": \"82\",\n" +
                "        \"transactionRate\": \"32\",\n" +
                "        \"accruedRate\": \"34\",\n" +
                "        \"accrualFrequency\": \"2234\",\n" +
                "        \"currency\": \"AUD\",\n" +
                "        \"discounts\": [\n" +
                "          {\n" +
                "            \"description\": \"Description\",\n" +
                "            \"discountType\": \"BALANCE\",\n" +
                "            \"amount\": \"34.00\",\n" +
                "            \"balanceRate\": \"82\",\n" +
                "            \"transactionRate\": \"34\",\n" +
                "            \"accruedRate\": \"23\",\n" +
                "            \"feeRate\": \"45\",\n" +
                "            \"eligibility\": [\n" +
                "              {\n" +
                "                \"discountEligibilityType\": \"BUSINESS\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"depositRates\": [\n" +
                "      {\n" +
                "        \"depositRateType\": \"FIXED\",\n" +
                "        \"rate\": \"43\",\n" +
                "        \"calculationFrequency\": \"111\",\n" +
                "        \"applicationFrequency\": \"222\",\n" +
                "        \"tiers\": [\n" +
                "          {\n" +
                "            \"name\": \"string\",\n" +
                "            \"unitOfMeasure\": \"DOLLAR\",\n" +
                "            \"minimumValue\": 0,\n" +
                "            \"maximumValue\": 0,\n" +
                "            \"rateApplicationMethod\": \"WHOLE_BALANCE\",\n" +
                "            \"subTier\": {\n" +
                "              \"name\": \"string\",\n" +
                "              \"unitOfMeasure\": \"DOLLAR\",\n" +
                "              \"minimumValue\": 0,\n" +
                "              \"maximumValue\": 0,\n" +
                "              \"rateApplicationMethod\": \"WHOLE_BALANCE\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }";
        return jsonParser.parse(productJson).getAsJsonObject();
    }

    private JsonObject getErrorResponseItem(String code, String title, String detail) {

        String errorJson = "{\n" +
                "    \"code\": \"" + code + "\",\n" +
                "    \"title\": \"" + title + "\",\n" +
                "    \"detail\": \"" + detail + "\"\n" +
                "  }";
        return jsonParser.parse(errorJson).getAsJsonObject();
    }

    private JsonObject getErrorResponseForInvalidRequestPayload() {

        JsonObject errorItem = getErrorResponseItem("0001", "Invalid account",
                "Account ID not found in the request body.");
        JsonObject errorResponse = new JsonObject();
        JsonArray errors = new JsonArray();
        errors.add(errorItem);
        errorResponse.add("errors", errors);
        return errorResponse;
    }

    private static JSONObject getRequest(String json) throws ParseException {
        log.info("request" + json);
        String[] splitString = json.split("\\.");
        String base64EncodedBody = splitString[1];
        String decodedString = new String(java.util.Base64.getDecoder()
                .decode(base64EncodedBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(decodedString);
        return jsonObject;
    }

    private static List<String> getAccountIds(JSONObject json) {
        List<String> accountIds = new ArrayList<>();
        JSONArray mappingResources = (JSONArray) json.get("consentMappingResources");
        for (int i = 0; i < mappingResources.size(); i++) {
            JSONObject resource = (JSONObject) mappingResources.get(i);
            accountIds.add((String) resource.get("accountId"));
        }
        return accountIds;
    }
}
