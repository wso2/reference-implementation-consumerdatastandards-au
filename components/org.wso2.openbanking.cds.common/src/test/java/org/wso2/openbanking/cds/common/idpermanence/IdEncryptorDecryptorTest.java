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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test class for IdEncryptorDecryptor.
 */
public class IdEncryptorDecryptorTest {

    int appId = 3;
    String memberId = "5@carbon.super";
    int realResourceId = 222222222;
    String secret = "wso2";
    String encryptedString;
    List<String> urlUnsafeCharacters = Arrays.asList("!", "*", "'", "(", ")", ";", ":", "@", "&", "=", "+",
            "$", ",", "/", "?", "%", "#", "[", "]", " ", "\"", "<", ">", "%", "{", "}", "|", "\\", "^", "`");

    @Test
    public void testEncryption() {

        encryptedString = IdEncryptorDecryptor.
                encrypt(memberId + ":" + appId + ":" + realResourceId, secret);

        Assert.assertNotNull(encryptedString);
        Assert.assertFalse(urlUnsafeCharacters.stream().anyMatch(encryptedString::contains));
    }

    @Test(dependsOnMethods = "testEncryption")
    public void testDecryption() {

        String decryptedString = IdEncryptorDecryptor.decrypt(encryptedString, secret);
        Assert.assertEquals(decryptedString, memberId + ":" + appId + ":" + realResourceId);
    }

}
