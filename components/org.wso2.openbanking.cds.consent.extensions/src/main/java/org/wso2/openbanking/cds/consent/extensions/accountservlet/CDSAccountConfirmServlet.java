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

package org.wso2.openbanking.cds.consent.extensions.accountservlet;

import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This servlet is executed after confirming the account selection.
 */
public class CDSAccountConfirmServlet extends HttpServlet {

    private static final long serialVersionUID = 7306276594632678191L;
    private static final Log log = LogFactory.getLog(CDSAccountConfirmServlet.class);

    @Generated(message = "Excluding from code coverage since this requires a service call")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Publish data related to abandoned consent flow (Metrics).
        Object requestUriKey = request.getSession().getAttribute(CommonConstants.REQUEST_URI_KEY);
        if (requestUriKey != null) {
            Map<String, Object> abandonedConsentFlowData = CDSCommonUtils.generateAbandonedConsentFlowDataMap(
                    requestUriKey.toString(), null, AuthorisationStageEnum.ACCOUNT_SELECTED);
            log.debug("Publishing abandoned consent flow data in the account selection confirmed stage.");
            CDSDataPublishingService.getCDSDataPublishingService().publishAbandonedConsentFlowData(
                    abandonedConsentFlowData);
        } else {
            log.warn("Request URI key not found in session attributes. Continuing without publishing abandoned " +
                    "consent flow data in the account selection confirmed stage.");
        }

        RequestDispatcher dispatcher = this.getServletContext()
                .getRequestDispatcher("/oauth2_authz_displayconsent.jsp");
        dispatcher.forward(request, response);
    }

}
