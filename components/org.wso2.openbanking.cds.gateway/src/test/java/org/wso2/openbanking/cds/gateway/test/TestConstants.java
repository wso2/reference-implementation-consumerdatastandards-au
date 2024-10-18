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

package org.wso2.openbanking.cds.gateway.test;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test constants.
 */
public class TestConstants {

    public static final String INVALID_EXECUTOR_CLASS =
            "com.wso2.openbanking.accelerator.gateway.executor.test.executor.InvalidClass";
    public static final String VALID_EXECUTOR_CLASS =
            "org.wso2.openbanking.cds.gateway.test.executor.MockOBExecutor";

    public static final Map<Integer, String> VALID_EXECUTOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(1, VALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static final Map<Integer, String> INVALID_EXECUTOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(1, INVALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final String CUSTOM_PAYLOAD = "{\"custom\":\"payload\"}";
    public static final Map<String, Map<Integer, String>> FULL_VALIDATOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("Default", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("DCR", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("CDS", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("CDSCommon", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("CDSUnauthenticated", VALID_EXECUTOR_MAP))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
