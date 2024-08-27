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

package com.wso2.cds.test.framework.utility

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Handling the AES encryption and decryption
 */
class AUIdEncryptorDecryptor {

    private static SecretKeySpec secretKey
    private static byte[] key

    /**
     * Set resource ID encryption/decryption key
     * @param secret : secret key
     */
    static void setKey(String secret) {
        MessageDigest sha
        key = secret.getBytes(StandardCharsets.UTF_8)
        sha = MessageDigest.getInstance("SHA-512")
        key = sha.digest(key)
        key = Arrays.copyOf(key, 16)
        secretKey = new SecretKeySpec(key, "AES")

    }

    /**
     * Encrypt a string using given secret
     * @param strToEncrypt : string to be encrypted
     * @param secret : encryption key
     * @return encrypted string
     */
    static String encrypt(String strToEncrypt, String secret) {
        setKey(secret)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.getUrlEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)))
    }

    /**
     * Decrypt a string using given secret
     * @param strToDecrypt : string to be decrypted
     * @param secret : decryption key
     * @return decrypted string
     */
    static String decrypt(String strToDecrypt, String secret) {
        setKey(secret)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        byte[] test = cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt))
        return new String(test, StandardCharsets.UTF_8)
    }

}

