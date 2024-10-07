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
package org.wso2.openbanking.cds.identity.auth.extensions.request.validator.model;

import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.annotation.ValidateSharingDuration;

/**
 * Model class for CDS request object.
 */
@ValidateSharingDuration(message = "Negative sharing_duration")
public class CDSRequestObject extends OBRequestObject {

    private static final long serialVersionUID = -8397385780422294126L;

    public CDSRequestObject(OBRequestObject obRequestObject) {

        super(obRequestObject);
    }
}
