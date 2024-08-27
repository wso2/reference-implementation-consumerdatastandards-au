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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.SoftwareProductStatusEnum;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.AUTH_BASIC;

/**
 * CleanupRegistrationResponsibility.
 * <p>
 * Uses to execute CDS cleanup registration data holder responsibility
 */
public class CleanupRegistrationResponsibility implements DataHolderResponsibility {

    private static final Log LOG = LogFactory.getLog(CleanupRegistrationResponsibility.class);
    private final String softwareProductStatus;
    private final String dataRecipientsStatus;
    private final ServiceProvider serviceProvider;

    public CleanupRegistrationResponsibility(String dataRecipientsStatus, String softwareProductStatus,
                                             ServiceProvider serviceProvider) {

        this.dataRecipientsStatus = dataRecipientsStatus;
        this.softwareProductStatus = softwareProductStatus;
        this.serviceProvider = serviceProvider;
    }

    /**
     * Check if the application has cascading status rules suitable to delete client applications.
     *
     * @return if validation success return true
     * @see <a href="https://cdr-register.github.io/register/#data-holder-responsibilities">
     * data-holder-responsibilities</a>
     */
    @Override
    public boolean shouldPerform() {
        if (SoftwareProductStatusEnum.REMOVED.toString().equalsIgnoreCase(this.softwareProductStatus)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Can perform cleanup registration responsibility. software product status: " +
                        this.softwareProductStatus + ", data recipient status: " + this.dataRecipientsStatus);
            }
            return true;
        }
        return false;
    }

    @Override
    public void perform() {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Deleting client application from APIM store. ApplicationName: " +
                        serviceProvider.getApplicationName());
            }
            deleteApplicationFromAPIM(serviceProvider.getApplicationName());

            for (InboundAuthenticationRequestConfig config :
                    this.serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()) {

                LOG.debug("Deleting service provider from IS. ClientId: " + config.getInboundAuthKey());
                deleteServiceProviderFromIS(config.getInboundAuthKey());
            }
        } catch (OpenBankingException e) {
            LOG.error("Exception occurred while performing cleanup registration responsibility. Caused by, ", e);
        }
    }

    /**
     * Method used to delete a client application by specifying its name.
     *
     * @param applicationName APIM application name
     */
    private void deleteApplicationFromAPIM(String applicationName) throws OpenBankingException {
        final String apimApplicationSearchUrl = OpenBankingCDSConfigParser.getInstance().getApimApplicationsSearchUrl();
        try {
            executeDeleteRequest(getApplicationIdFromName(applicationName), apimApplicationSearchUrl,
                    Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED));
        } catch (IOException e) {
            throw new OpenBankingException("Error while deleting application through APIM admin API", e);
        }
    }

    /**
     * Method used to delete a service provider by specifying its id.
     *
     * @param applicationId application id
     */
    private void deleteServiceProviderFromIS(String applicationId) throws OpenBankingException {

        final String dcrInternalUrl = OpenBankingCDSConfigParser.getInstance().getDcrInternalUrl();
        try {
            executeDeleteRequest(applicationId, dcrInternalUrl, Collections.singletonList(HttpStatus.SC_NO_CONTENT));
        } catch (IOException e) {
            throw new OpenBankingException("Error while deleting application through DCR internal API", e);
        }
    }

    /**
     * To send HTTP DELETE request to delete an application from APIM or to delete a service provider from IS.
     *
     * @param applicationId       APIM application id / IS service provider id
     * @param url                 APIM application delete URL / DCR internal URL
     * @param expectedStatusCodes expected status codes from DELETE response
     * @throws OpenBankingException throws if invalid unexpected response code received
     * @throws IOException          throws when http delete request execution failed
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    protected void executeDeleteRequest(String applicationId, String url, List<Integer> expectedStatusCodes)
            throws OpenBankingException, IOException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient()) {
            HttpDelete httpDelete = new HttpDelete(generateUrl(applicationId, url));
            httpDelete.setHeader(HTTPConstants.HEADER_AUTHORIZATION,
                    AUTH_BASIC + HTTPClientUtils.getBasicAuthCredentials());

            CloseableHttpResponse responseBody = httpclient.execute(httpDelete);

            final int responseStatusCode = responseBody == null ? -1 : responseBody.getStatusLine().getStatusCode();
            if (expectedStatusCodes.contains(responseStatusCode)) {
                LOG.info("Application with client ID " + applicationId + " was deleted due to CDR status changes.");
            } else {
                throw new OpenBankingException("Could not delete application with " + applicationId +
                        ". DCR delete returned non OK response.");
            }
        }
    }

    private String generateUrl(String applicationId, String url) throws OpenBankingException {

        if (!StringUtils.isEmpty(url)) {
            if (url.endsWith("/")) {
                return url + applicationId;
            } else {
                return url + "/" + applicationId;
            }
        } else {
            throw new OpenBankingException("Configured application delete URL is empty");
        }
    }

    private String getApplicationIdFromName(String applicationName) throws OpenBankingException {

        try {
            JSONArray apimApplications = getAllApplications();
            for (int i = 0; i < apimApplications.length(); i++) {
                JSONObject apimApplication = apimApplications.getJSONObject(i);
                String apimApplicationName = apimApplication.optString(MetadataConstants.APPLICATION_NAME, "");

                if (applicationName.equals(apimApplicationName)) {
                    return apimApplication.optString(MetadataConstants.APPLICATION_ID, "");
                }
            }
        } catch (IOException e) {
            throw new OpenBankingException("Error while retrieving application data through search API", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Retrieve all existing applications from APIM store by sending HTTP GET request.
     *
     * @return JSONArray of APIM applications
     * @throws OpenBankingException throws if invalid unexpected response code received
     * @throws IOException          throws when http GET request execution failed
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    private JSONArray getAllApplications() throws OpenBankingException, IOException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient()) {
            HttpGet httpGet = new HttpGet(OpenBankingCDSConfigParser.getInstance().getApimApplicationsSearchUrl());
            httpGet.setHeader(HTTPConstants.HEADER_AUTHORIZATION,
                    AUTH_BASIC + HTTPClientUtils.getBasicAuthCredentials());

            CloseableHttpResponse response = httpclient.execute(httpGet);

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                final String responseStr = EntityUtils.toString(responseEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == statusCode) {
                    LOG.debug("Received application data from APIM store");
                    return new JSONObject(responseStr).getJSONArray(MetadataConstants.LIST);
                } else {
                    LOG.error("Received invalid response from GET all applications API, status= " + statusCode +
                            " response=" + responseStr);
                }
            } else {
                LOG.error("Received null response from get all applications API");
            }
            throw new OpenBankingException("Invalid response received when retrieving applications through search API");
        }
    }

    /**
     * Used when inserting responsibility to responsibilities map in DataHolderResponsibilitiesExecutor class.
     *
     * @return responsibilityId with format of applicationName-id-responsibilityType
     */
    @Override
    public String getResponsibilityId() {
        return String.format("%s-%s-%s", this.serviceProvider.getApplicationID(),
                this.serviceProvider.getApplicationName(), "CleanupRegistration");
    }
}
