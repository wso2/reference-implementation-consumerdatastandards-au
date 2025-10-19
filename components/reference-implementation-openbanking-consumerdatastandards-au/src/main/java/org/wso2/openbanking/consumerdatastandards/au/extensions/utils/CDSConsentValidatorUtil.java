/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for CDS Consent Validator.
 */
public class CDSConsentValidatorUtil {

    private static final Log log = LogFactory.getLog(CDSConsentValidatorUtil.class);


    /**
     * Util method to validate the Account request URI.
     *
     * @param uri  Request URI
     * @return
     */
    public static boolean isAccountURIValid(String uri) {
        List<String> accountPaths = getAccountAPIPathRegexArray();
        boolean isValid = false;

        for (String entry : accountPaths) {
            if (uri.contains(entry)) {
                isValid = true;
                break;
            }
        }

        return isValid;
    }

    /**
     * Method provides API resource paths applicable for UK Account API.
     *
     * @return map of API Resources.
     */
    public static List<String> getAccountAPIPathRegexArray() {

        List<String> requestUrls = Arrays.asList(CommonConstants.ACCOUNT_REGEX,
                CommonConstants.ACCOUNT_DETAILS_REGEX,
                CommonConstants.PRODUCTS_REGEX,
                CommonConstants.PRODUCT_DETAILS_REGEX,
                CommonConstants.BALANCES_REGEX,
                CommonConstants.BALANCES_ID_REGEX,
                CommonConstants.TRANSACTIONS_ID_REGEX,
                CommonConstants.TRANSACTION_DETAILS_REGEX,
                CommonConstants.DIRECT_DEBITS_ID_REGEX,
                CommonConstants.DIRECT_DEBITS_REGEX,
                CommonConstants.SCHEDULED_PAYMENTS_ID_REGEX,
                CommonConstants.SCHEDULED_PAYMENTS_REGEX,
                CommonConstants.PAYEES_REGEX,
                CommonConstants.PAYEES_ID_REGEX);

        return requestUrls;
    }

    /**
     * Validate whether consent is expired.
     *
     * @param expDateVal     Expiration Date Time
     * @return
     */
    public static boolean isConsentExpired(String expDateVal) {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            return OffsetDateTime.now().isAfter(expDate);
        } else {
            return false;
        }
    }
}
