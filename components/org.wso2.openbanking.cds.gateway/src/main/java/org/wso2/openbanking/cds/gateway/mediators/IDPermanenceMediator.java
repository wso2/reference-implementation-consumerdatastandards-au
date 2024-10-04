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
package org.wso2.openbanking.cds.gateway.mediators;

import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceConstants;

import java.util.Map;

/**
 * IDPermanence Mediator class to alter message context urls.
 * This mediator will replace urls with encrypted esource-ids with decrypted ones.
 */
public class IDPermanenceMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(IDPermanenceMediator.class);

    private static final String REST_API_CONTEXT = "REST_API_CONTEXT";
    private static final String URI_RESOURCE = "api.ut.resource";
    private static final String REST_FULL_REQUEST_PATH = "REST_FULL_REQUEST_PATH";
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private static final String AXIS2_TRANSPORT_URL = "TransportInURL";
    private static final String AXIS2_REST_URL_POSTFIX = "REST_URL_POSTFIX";

    @Override
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public boolean mediate(MessageContext messageContext) {

        log.debug("Engaging CDS IDPermanenceMediator");
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        // Check if the decrypted uri is available in transport headers
        if (headers.containsKey(IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH)) {
            log.debug("Decrypted url found in transport headers. Altering message context urls");
            String decryptedSubRequestPath = headers.get(IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH).toString();
            String decryptedFullRequestPath = messageContext.getProperty(REST_API_CONTEXT).toString() +
                    decryptedSubRequestPath;

            // Replace message context parameters
            messageContext.setProperty(REST_FULL_REQUEST_PATH, decryptedFullRequestPath);
            messageContext.setProperty(URI_RESOURCE, decryptedSubRequestPath);
            messageContext.setProperty(REST_SUB_REQUEST_PATH, decryptedSubRequestPath);
            messageContext.setTo(new EndpointReference(decryptedFullRequestPath));
            axis2MessageContext.setProperty(AXIS2_TRANSPORT_URL, decryptedFullRequestPath);
            axis2MessageContext.setProperty(AXIS2_REST_URL_POSTFIX, decryptedSubRequestPath);

            // Remove decrypted uri from transport headers
            log.debug("Removing decrypted url from transport headers.");
            headers.remove(IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH);
            axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        return true;
    }
}
