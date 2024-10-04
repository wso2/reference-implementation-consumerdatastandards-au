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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;

/**
 * Retryer
 * <p>
 * Class uses to re-execute a specific task. It waits until waitTime expires and repeatedly attempts the task up to
 * maxRetryCount times. waitTime multiplies by double for every re-execution.
 *
 * @param <T> return type of the execute method
 */
public class Retryer<T> {

    private static final Log LOG = LogFactory.getLog(Retryer.class);

    private long waitTime;
    private final int maxRetryCount;
    private int attempt;

    public Retryer(long waitTime, int maxRetryCount) {
        this.waitTime = waitTime;
        this.maxRetryCount = maxRetryCount;
        this.attempt = 0;
    }

    public T execute(Callable<T> callable) throws OpenBankingException {

        while (this.maxRetryCount > this.attempt) {
            try {
                return callable.call();
            } catch (Exception e) {
                LOG.error("Error occurred while executing Retryer command on attempt " + (this.attempt + 1) + " of "
                        + this.maxRetryCount + ". Caused by, ", e);
                this.attempt++;
            }
            blockThread(this.waitTime);
            this.waitTime = this.waitTime * 2;
        }
        throw new OpenBankingException("Retryer command has failed on all retries");
    }

    private void blockThread(long waitTime) {

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            LOG.warn("Exception occurred while blocking Retryer thread. Caused by, ", e);
            Thread.currentThread().interrupt();
        }
    }

}
