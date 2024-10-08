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

package org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.model;

import java.util.List;
import javax.validation.Valid;

/**
 * Disclosure Options Management - DOMSStatusUpdateDTOList.
 */
public class DOMSStatusUpdateListDTO {

    @Valid
    private List<DOMSStatusUpdateDTO> data;

    public List<DOMSStatusUpdateDTO> getData() {
        return data;
    }

    public void setData(List<DOMSStatusUpdateDTO> data) {
        this.data = data;
    }

}
