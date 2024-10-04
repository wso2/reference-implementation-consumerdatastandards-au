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

package org.wso2.openbanking.cds.common.idpermanence;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handling the AES encryption and decryption.
 */
public class IdEncryptorDecryptor {

    private static final Log log = LogFactory.getLog(IdEncryptorDecryptor.class);

    private static SecretKeySpec secretKey;
    private static byte[] key;

    /**
     * Set resource ID encryption/decryption key.
     *
     * @param secret secret key
     */
    public static void setKey(String secret) {
        MessageDigest sha = null;
        try {
            key = secret.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-512");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while setting the encryption key", e);
        }
    }

    /**
     * Encrypt a string using given secret.
     *
     * @param strToEncrypt string to be encrypted
     * @param secret       encryption key
     * @return encrypted string
     */
    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeBase64URLSafeString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
            log.error("Error while setting the encryption key", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            log.error("Error while encrypting", e);
        }
        return null;
    }

    /**
     * Decrypt a string using given secret.
     *
     * @param strToDecrypt string to be decrypted
     * @param secret       decryption key
     * @return decrypted string
     */
    public static String decrypt(String strToDecrypt, String secret) throws IllegalArgumentException {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] test = cipher.doFinal(Base64.decodeBase64(strToDecrypt.getBytes(StandardCharsets.UTF_8)));
            return new String(test, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            log.error("Error while setting the decryption key", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            log.error("Error while decrypting", e);
        }
        return null;
    }
}
