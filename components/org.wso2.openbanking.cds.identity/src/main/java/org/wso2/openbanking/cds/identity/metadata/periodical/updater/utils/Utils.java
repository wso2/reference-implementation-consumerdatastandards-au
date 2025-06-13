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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Utility functions to perform tasks.
 */
public class Utils {

    private static final Log LOG = LogFactory.getLog(Utils.class);

    private Utils() {
    }

    @Nullable
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    public static JSONObject readJsonFromUrl(String url, Map<String, String> registerApiRequestHeaders)
            throws IOException, OpenBankingException {

        if (!StringUtils.isEmpty(url)) {
            try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient()) {

                HttpGet httpGet = new HttpGet(url);
                setRequestHeaders(httpGet, registerApiRequestHeaders);
                CloseableHttpResponse response = httpclient.execute(httpGet);

                return getResponseJson(response);
            } catch (NullPointerException e) {
                LOG.debug("Unable to retrieve status from Directory. " +
                        "Possible because Common HttpPool is not initialized yet.");
            }
        }
        return null;
    }

    /**
     * Set request headers to the HttpGet request.
     *
     * @param httpGet                   HttpGet request
     * @param registerApiRequestHeaders Request headers
     */
    private static void setRequestHeaders(HttpGet httpGet, Map<String, String> registerApiRequestHeaders) {
        if (registerApiRequestHeaders != null) {
            for (Map.Entry<String, String> entry : registerApiRequestHeaders.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private static JSONObject getResponseJson(HttpResponse response) throws IOException, OpenBankingException {

        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            final String responseStr = EntityUtils.toString(responseEntity);

            int statusCode = response.getStatusLine().getStatusCode();

            if (HttpStatus.SC_OK == statusCode) {
                LOG.debug("Received success(200) metadata response from ACCC");
                return new JSONObject(responseStr);
            } else if (HttpStatus.SC_BAD_REQUEST == statusCode || HttpStatus.SC_NOT_ACCEPTABLE == statusCode) {
                LOG.error("Received error( " + statusCode + ") metadata response from ACCC, response=" + responseStr);
                return null;
            }
            LOG.error("Received unexpected error( " + statusCode + ") metadata response from ACCC, response=" +
                    responseStr);
            throw new OpenBankingException("Metadata cache update was not performed. " +
                    "Unexpected response received from ACCC");
        } else {
            throw new OpenBankingException("Metadata cache update was not performed. " +
                    "Empty response received from ACCC");
        }

    }
}
