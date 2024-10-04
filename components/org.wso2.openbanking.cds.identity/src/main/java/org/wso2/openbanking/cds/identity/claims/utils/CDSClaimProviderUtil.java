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
package org.wso2.openbanking.cds.identity.claims.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Utility methods used for CDS Claim Provider service.
 */
public class CDSClaimProviderUtil {

    private static Log log = LogFactory.getLog(CDSClaimProviderUtil.class);

    /**
     * Method to obtain Hash Value for a given String.
     *
     * @param value String value that required to be Hashed
     * @return Hashed String
     * @throws IdentityOAuth2Exception
     */
    @Generated(message = "Ignoring since the method require a service call")
    public static String getHashValue(String value, String digestAlgorithm) throws IdentityOAuth2Exception {

        if (digestAlgorithm == null) {
            if (log.isDebugEnabled()) {
                log.debug("Digest algorithm not provided. Therefore loading digest algorithm from identity.xml");
            }
            JWSAlgorithm digAlg = OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(
                    OAuthServerConfiguration.getInstance().getIdTokenSignatureAlgorithm());
            digestAlgorithm = OAuth2Util.mapDigestAlgorithm(digAlg);
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityOAuth2Exception("Error creating the hash value. Invalid Digest Algorithm: " +
                    digestAlgorithm, e);
        }

        messageDigest.update(value.getBytes(Charsets.UTF_8));
        byte[] digest = messageDigest.digest();
        int leftHalfBytes = 16;
        if ("SHA-384".equals(digestAlgorithm)) {
            leftHalfBytes = 24;
        } else if ("SHA-512".equals(digestAlgorithm)) {
            leftHalfBytes = 32;
        }
        byte[] leftmost = new byte[leftHalfBytes];
        System.arraycopy(digest, 0, leftmost, 0, leftHalfBytes);
        return Base64.encodeBase64URLSafeString(leftmost);
    }

    /**
     * convert consent expirytime to epoch time.
     *
     * @param expiryTime consent expiry time in seconds
     * @return expiry time as an epoch time
     */
    public static long getEpochDateTime(long expiryTime) {

        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        return currentTime.plusSeconds(expiryTime).toEpochSecond();
    }
}
