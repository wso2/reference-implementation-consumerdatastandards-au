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

package org.wso2.openbanking.cds.metrics.model;

import org.wso2.openbanking.cds.metrics.util.AspectEnum;

import java.util.Objects;

/**
 * Model class for server outages data for availability calculations.
 */
public class ServerOutageDataModel {

    private String outageId;
    private long timestamp;
    private String type;
    private long timeFrom;
    private long timeTo;
    private AspectEnum aspect;

    public ServerOutageDataModel(String outageId, long timestamp, String type, long timeFrom, long timeTo,
                                 AspectEnum aspect) {

        this.outageId = outageId;
        this.timestamp = timestamp;
        this.type = type;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.aspect = aspect;
    }

    public String getOutageId() {

        return outageId;
    }

    public void setOutageId(String outageId) {

        this.outageId = outageId;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public long getTimeFrom() {

        return timeFrom;
    }

    public void setTimeFrom(long timeFrom) {

        this.timeFrom = timeFrom;
    }

    public long getTimeTo() {

        return timeTo;
    }

    public void setTimeTo(long timeTo) {

        this.timeTo = timeTo;
    }

    public AspectEnum getAspect() {
        return aspect;
    }

    public void setAspect(AspectEnum aspect) {
        this.aspect = aspect;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerOutageDataModel dataModel = (ServerOutageDataModel) o;
        return timeFrom == dataModel.timeFrom && timeTo == dataModel.timeTo && Objects.equals(type, dataModel.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, timeFrom, timeTo);
    }
}
