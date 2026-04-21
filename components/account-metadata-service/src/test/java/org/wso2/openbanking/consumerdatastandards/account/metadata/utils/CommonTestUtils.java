/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.account.metadata.utils;

import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;

/**
 * Common helpers shared by unit tests in account-metadata-service.
 */
public final class CommonTestUtils {

    private CommonTestUtils() {

    }

    /**
     * Builds a secondary instruction test item.
     *
     * @param accountId account id
     * @param userId secondary user id
     * @param otherAccountsAvailable whether other accounts are available
     * @param status instruction status
     * @return populated test item
     */
    public static SecondaryAccountInstructionItem buildSecondaryItem(String accountId, String userId,
                                                                     boolean otherAccountsAvailable,
                                                                     String status) {

        SecondaryAccountInstructionItem item = new SecondaryAccountInstructionItem();
        item.setAccountId(accountId);
        item.setSecondaryUserId(userId);
        item.setOtherAccountsAvailability(otherAccountsAvailable);
        item.setSecondaryAccountInstructionStatus(
                SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.fromValue(status));
        return item;
    }
}