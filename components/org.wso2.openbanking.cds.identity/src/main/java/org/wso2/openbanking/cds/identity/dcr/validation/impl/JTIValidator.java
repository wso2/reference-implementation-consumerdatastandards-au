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

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.identity.dcr.cache.JwtJtiCache;
import org.wso2.openbanking.cds.identity.dcr.cache.JwtJtiCacheKey;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateJTI;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class to validate JTI.
 */
public class JTIValidator implements ConstraintValidator<ValidateJTI, Object> {

    private static final Log log = LogFactory.getLog(JTIValidator.class);
    private String registrationRequestPath;
    private String ssaPath;
    private static JwtJtiCache jtiCache = JwtJtiCache.getInstance();

    @Override
    public void initialize(ValidateJTI validateJTI) {
        this.registrationRequestPath = validateJTI.registrationRequestProperty();
        this.ssaPath = validateJTI.ssa();
    }

    @Override
    public boolean isValid(Object ukRegistrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {
            OpenBankingCDSConfigParser parser = OpenBankingCDSConfigParser.getInstance();
            //Validate JTI value in the registration request
            RegistrationRequest registrationRequest = (RegistrationRequest) new PropertyUtilsBean()
                    .getProperty(ukRegistrationRequest, registrationRequestPath);
            String requestJtiValue = registrationRequest.getJti();

            String requestJTIValidationEnabled = (String) parser.getConfiguration()
                    .get(CommonConstants.ENABLE_REQUEST_JTI_VALIDATION);

            if (Boolean.parseBoolean(requestJTIValidationEnabled) && isJTIReplayed(requestJtiValue)) {
                log.error(String.format("Rejected the replayed jti in the registration request: %s", requestJtiValue));
                return false;
            }

            //Validate JTI value in the software statement
            String softwareStatement = BeanUtils.getProperty(registrationRequest, ssaPath);
            String ssaJtiValue = (String) JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .get(CDSValidationConstants.JTI);

            String ssaJTIValidationEnabled = (String) parser.getConfiguration()
                    .get(CommonConstants.ENABLE_SSA_JTI_VALIDATION);

            if (Boolean.parseBoolean(ssaJTIValidationEnabled) && isJTIReplayed(ssaJtiValue)) {
                log.error(String.format("Rejected the replayed jti in the SSA: %s", requestJtiValue));
                return false;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
            return false;
        }

        return true;
    }

    /**
     * Check whether the given jti value is replayed.
     *
     * @param jtiValue - jti value
     * @return
     */
    public static boolean isJTIReplayed(String jtiValue) {

        // Validate JTI. Continue if jti is not present in cache
        if (getJtiFromCache(jtiValue) != null) {
            return true;
        }

        // Add jti value to cache
        JwtJtiCacheKey jtiCacheKey = JwtJtiCacheKey.of(jtiValue);
        jtiCache.addToCache(jtiCacheKey, jtiValue);
        return false;
    }

    /**
     * Try to retrieve the given jti value from cache.
     *
     * @param jtiValue - jti value
     * @return
     */
    public static String getJtiFromCache(String jtiValue) {

        JwtJtiCacheKey cacheKey = JwtJtiCacheKey.of(jtiValue);
        return jtiCache.getFromCache(cacheKey);
    }
}
