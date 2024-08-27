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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility;

import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Test class for Data Holder Responsibilities Executor.
 */
public class DataHolderResponsibilitiesExecutorTest {

    DataHolderResponsibilitiesExecutor uut;

    @Test
    public void testExecute() {
        DataHolderResponsibility responsibilityMock1 = Mockito.mock(DataHolderResponsibility.class);
        Mockito.when(responsibilityMock1.shouldPerform()).thenReturn(true);
        Mockito.when(responsibilityMock1.getResponsibilityId()).thenReturn("appName1-1-CleanupRegistration");
        DataHolderResponsibility responsibilityMock2 = Mockito.mock(DataHolderResponsibility.class);
        Mockito.when(responsibilityMock2.getResponsibilityId()).thenReturn("appName1-1-InvalidateConsents");

        uut = DataHolderResponsibilitiesExecutor.getInstance();
        uut.addResponsibility(responsibilityMock1);
        uut.addResponsibility(responsibilityMock2);

        uut.execute();

        Mockito.verify(responsibilityMock1, Mockito.times(1)).shouldPerform();
        Mockito.verify(responsibilityMock1, Mockito.times(1)).perform();
        Mockito.verify(responsibilityMock2, Mockito.times(1)).shouldPerform();
        Mockito.verify(responsibilityMock2, Mockito.times(0)).perform();
    }

}
