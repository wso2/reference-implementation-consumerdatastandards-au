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
package org.wso2.openbanking.cds.identity.filter;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * CDS Par Filter.
 * Enforces security to the par endpoint.
 */
public class CDSParFilter extends CDSBaseFilter {

    private static final Log log = LogFactory.getLog(CDSParFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initializeFilterValidators();
    }

    /**
     * Load filter validators form configuration.
     */
    @Generated(message = "Excluded from code coverage")
    private void initializeFilterValidators() {
        if (validators.isEmpty()) {
            log.debug("Adding CDSParFilter validators");
            for (Object element : OpenBankingCDSConfigParser.getInstance().getParFilterValidators()) {
                validators.add((OBIdentityFilterValidator) OpenBankingUtils.
                        getClassInstanceFromFQN(element.toString()));
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Added %s as a CDSParFilter validator", element));
                }
            }
        }
    }
}
