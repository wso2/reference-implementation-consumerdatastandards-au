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
package org.wso2.openbanking.cds.identity.auth.extensions.request.validator.impl;

import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.annotation.ValidateSharingDuration;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.model.CDSRequestObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the sharing duration claim in the request object.
 */
public class SharingDurationValidator implements ConstraintValidator<ValidateSharingDuration, Object> {

    private static Log log = LogFactory.getLog(SharingDurationValidator.class);

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        String sharingDurationString = StringUtils.EMPTY;
        JSONObject claims = (JSONObject) ((CDSRequestObject) object).getClaim("claims");
        if (claims != null && claims.containsKey("sharing_duration")) {
            sharingDurationString = claims.get("sharing_duration").toString();
        }

        int sharingDuration;
        try {
            sharingDuration = StringUtils.isEmpty(sharingDurationString) ? 0 : Integer.parseInt(sharingDurationString);
        } catch (NumberFormatException e) {
            return false;
        }

        //If the sharing_duration value is negative then the authorisation should fail.
        return sharingDuration >= 0;
    }

}
