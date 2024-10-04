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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.retryer;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

/**
 * Test class for Retryer on MetaData Update Task.
 */
public class RetryerTest {

    Retryer<Integer> retryer;

    @BeforeClass
    public void init() {
        this.retryer = new Retryer<>(100, 3);
    }

    @Test
    public void testExecute() throws Exception {
        final int actual = this.retryer.execute(() -> 10);
        Assert.assertEquals(actual, 10);

        Callable<Integer> callableMock = Mockito.mock(TestCallable.class);
        Mockito.when(callableMock.call())
                .thenThrow(new NullPointerException())
                .thenReturn(10);

        this.retryer.execute(callableMock);
        Mockito.verify(callableMock, Mockito.times(2)).call();
    }

    @Test(description = "when all attempts fails, should throw OpenBankingException",
            expectedExceptions = OpenBankingException.class)
    public void testExecuteWithException() throws Exception {
        Callable<Integer> callableMock = Mockito.mock(TestCallable.class);
        Mockito.when(callableMock.call())
                .thenThrow(new NullPointerException())
                .thenThrow(new IndexOutOfBoundsException())
                .thenThrow(new ClassNotFoundException());

        this.retryer.execute(callableMock);
    }

    private static class TestCallable implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            return 10;
        }
    }
}
