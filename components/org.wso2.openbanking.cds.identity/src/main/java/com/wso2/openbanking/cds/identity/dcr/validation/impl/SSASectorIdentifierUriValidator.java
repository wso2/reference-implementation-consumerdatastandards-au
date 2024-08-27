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

package org.wso2.openbanking.cds.identity.dcr.validation.impl;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.model.CDSSoftwareStatementBody;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSASectorIdentifierUri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the sector identifier uri in ssa.
 */
public class SSASectorIdentifierUriValidator implements ConstraintValidator<ValidateSSASectorIdentifierUri, Object> {

    private static final Log log = LogFactory.getLog(SSASectorIdentifierUriValidator.class);

    @Override
    public boolean isValid(Object cdsSoftwareStatementBody, ConstraintValidatorContext constraintValidatorContext) {

        if (Boolean.parseBoolean(OpenBankingCDSConfigParser.getInstance().getConfiguration()
                .get(CDSValidationConstants.DCR_VALIDATE_SECTOR_IDENTIFIER_URI).toString()
                .toLowerCase(Locale.ENGLISH))) {

            CDSSoftwareStatementBody cdsSoftwareStatementBodyObject;
            if (cdsSoftwareStatementBody instanceof CDSSoftwareStatementBody) {
                cdsSoftwareStatementBodyObject = (CDSSoftwareStatementBody) cdsSoftwareStatementBody;
            } else {
                return false;
            }

            String ssaSectorIdentifierUri = cdsSoftwareStatementBodyObject.getSectorIdentifierUri();
            List<String> ssaCallBackUris = cdsSoftwareStatementBodyObject.getCallbackUris();
            return validateSectorIdentifierURI(ssaSectorIdentifierUri, ssaCallBackUris);
        } else {
            return true;
        }
    }

    /**
     * Retrieve redirect uri list by calling sector identifier uri.
     *
     * @param sectorIdentifierUri sector identifier uri
     * @param callBackUris        redirect uri list from ssa
     * @return true if all the hostname of ssa callback uris matches hostnames of callback uris returned from
     * sector identifier uri
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    private boolean validateSectorIdentifierURI(String sectorIdentifierUri, List<String> callBackUris) {

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpGet request = new HttpGet(new URI(sectorIdentifierUri));
            request.addHeader("Content-Type", "application/json");
            HttpResponse dataResponse = client.execute(request);

            String data;
            if (dataResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Calling sector identifier uri failed with status code: " +
                        dataResponse.getStatusLine().getStatusCode() + ", due to " + dataResponse.getStatusLine()
                        .getReasonPhrase());
                return false;
            } else {
                InputStream in = dataResponse.getEntity().getContent();
                data = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            }

            JSONArray jsonArray = new JSONArray(data.trim());
            ArrayList<String> listOfUris = new ArrayList<>();

            for (int index = 0; index < jsonArray.length(); index++) {
                listOfUris.add(jsonArray.getString(index).trim());
            }

            return matchRedirectURI(callBackUris, listOfUris);

        } catch (IOException | URISyntaxException | OpenBankingException e) {
            log.error("error occurred while calling sector identifier url", e);
        }
        return false;
    }

    /**
     * Check whether the redirect uris in the ssa are a subset of the redirect uris returned after calling sector.
     * identifier uri
     */
    private boolean matchRedirectURI(List<String> callbackUris, List<String> callbackUrisFromSectorIdentifier) {

        int matchedURis = 0;
        for (String requestURI : callbackUris) {
            for (String callbackUriValueFromSectorIdentifier : callbackUrisFromSectorIdentifier) {
                if (requestURI.equals(callbackUriValueFromSectorIdentifier)) {
                    matchedURis = matchedURis + 1;
                }
            }
        }
        return matchedURis == callbackUris.size();
    }
}
