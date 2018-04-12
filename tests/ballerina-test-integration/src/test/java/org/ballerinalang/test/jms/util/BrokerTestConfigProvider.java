/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.jms.util;

import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration provider implementation for tests.
 */
public class BrokerTestConfigProvider implements BrokerConfigProvider {

    private Map<String, Object> configMap = new HashMap<>();

    @Override
    public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) {
        return configurationClass.cast(configMap.get(namespace));
    }

    public void registerConfigurationObject(String namespace, Object configObject) {
        configMap.put(namespace, configObject);
    }

}
