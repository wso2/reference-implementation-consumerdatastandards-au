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

package org.wso2.openbanking.cds.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * This class contains the common utility methods used for CDS Consent steps.
 */
public class CDSConsentCommonUtil {

    private static final Log log = LogFactory.getLog(CDSConsentCommonUtil.class);


    /**
     * Method to get the userId with tenant domain.
     *
     * @param userId
     * @return
     */
    public static String getUserIdWithTenantDomain(String userId) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (userId.endsWith(tenantDomain)) {
            return userId;
        } else {
            return userId + "@" + tenantDomain;
        }
    }

    /**
     * Method to get the customer type from the customer endpoint.
     *
     * @param consentData Consent data
     * @return Customer type
     */
    public static String getCustomerType(ConsentData consentData) {

        if (CDSConsentExtensionConstants.TRUE.equalsIgnoreCase(OpenBankingCDSConfigParser.getInstance()
                .getConfiguration().get(CDSConsentExtensionConstants.ENABLE_CUSTOMER_DETAILS).toString())) {

            String customerEPURL = OpenBankingCDSConfigParser.getInstance().getConfiguration()
                    .get(CDSConsentExtensionConstants.CUSTOMER_DETAILS_RETRIEVE_ENDPOINT).toString();

            if (StringUtils.isNotBlank(customerEPURL)) {
                String customerDetails = getCustomerFromEndpoint(customerEPURL, consentData.getUserId());
                if (StringUtils.isNotBlank(customerDetails)) {
                    try {
                        JSONObject customerDetailsJson = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                                .parse(customerDetails);
                        return customerDetailsJson.get(CDSConsentExtensionConstants.CUSTOMER_TYPE).toString();
                    } catch (ParseException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Unable to load customer data for the customer: " + consentData.getUserId());
                        }
                        return CDSConsentExtensionConstants.ORGANISATION;
                    }
                }
            }
        }
        return CDSConsentExtensionConstants.ORGANISATION;
    }

    /**
     * Method to get the customer details from the customer endpoint.
     *
     * @param customerDetailsUrl Customer details endpoint
     * @param user               User
     * @return Customer details
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    public static String getCustomerFromEndpoint(String customerDetailsUrl, String user) {
        user = user.substring(0, user.lastIndexOf("@"));
        String url = customerDetailsUrl.replace("{userId}", user);
        if (log.isDebugEnabled()) {
            log.debug("Customer Details endpoint : " + url);
        }

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving customer details failed");
                return null;
            } else {
                InputStream in = response.getEntity().getContent();
                return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            }
        } catch (IOException | OpenBankingException e) {
            log.error("Exception occurred while retrieving sharable accounts", e);
        }
        return null;
    }

}
