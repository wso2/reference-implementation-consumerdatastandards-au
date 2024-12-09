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

package org.wso2.cds.test.framework.data_provider


import org.testng.annotations.DataProvider
import org.wso2.cds.test.framework.constant.AUConstants

/**
 *  Data provide class for Accounts tests
 */
class ConsentDataProviders {

    @DataProvider(name = "AccountsRetrievalFlow")
    Object[] getAccountsRetrievalFlow() {

        def accounts = new ArrayList<>()
        accounts.add(AUConstants.BULK_ACCOUNT_PATH as Object)
        accounts.add(AUConstants.BULK_BALANCES_PATH as Object)
        accounts.add(AUConstants.GET_TRANSACTIONS as Object)
        return accounts
    }

    @DataProvider(name = "BankingApis")
    Object[] getBankingAPis() {

        def accounts = new ArrayList<>()
        accounts.add(AUConstants.BULK_ACCOUNT_PATH as Object)
        accounts.add(AUConstants.BULK_BALANCES_PATH as Object)
        accounts.add(AUConstants.GET_TRANSACTIONS as Object)
        accounts.add(AUConstants.BULK_DIRECT_DEBITS_PATH as Object)
        accounts.add(AUConstants.BULK_SCHEDULE_PAYMENTS_PATH as Object)
        accounts.add(AUConstants.BULK_PAYEES as Object)
        return accounts
    }

    @DataProvider(name = "BankingApisBusinessProfile")
    Object[] getBankingApisBusinessProfile() {

        def accounts = new ArrayList<>()
        accounts.add(AUConstants.BULK_ACCOUNT_PATH as Object)
        accounts.add(AUConstants.BULK_BALANCES_PATH as Object)
        accounts.add(AUConstants.GET_BUSINESS_ACCOUNT_TRANSACTIONS as Object)
        accounts.add(AUConstants.BULK_DIRECT_DEBITS_PATH as Object)
        accounts.add(AUConstants.BULK_SCHEDULE_PAYMENTS_PATH as Object)
        accounts.add(AUConstants.BULK_PAYEES as Object)
        return accounts
    }

    @DataProvider(name = "httpMethods")
    Object[] getHttpMethods() {

        def httpMethod = new ArrayList<>()
        httpMethod.add(AUConstants.HTTP_METHOD_PATCH as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_HEAD as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_OPTIONS as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_TRACE as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_CONNECT as Object)
        return httpMethod
    }

    @DataProvider(name = "unsupportedHttpMethods")
    Object[] getUnsupportedHttpMethods() {

        def httpMethod = new ArrayList<>()
        httpMethod.add(AUConstants.HTTP_METHOD_COPY as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_LINK as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_UNLINK as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_PURGE as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_LOCK as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_UNLOCK as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_PROPFIND as Object)
        httpMethod.add(AUConstants.HTTP_METHOD_VIEW as Object)
        return httpMethod
    }

}

