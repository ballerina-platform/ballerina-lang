/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.kafka.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;

import java.util.Map;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.BALLERINA_STRAND;

/**
 * Represents a deserializer class for ballerina kafka module.
 */
public class BallerinaKafkaDeserializer implements Deserializer {

    private ObjectValue deserializerObject = null;
    private BRuntime runtime = null;

    @Override
    public void configure(Map configs, boolean isKey) {
        this.runtime = (BRuntime) configs.get(BALLERINA_STRAND);
        if (isKey) {
            this.deserializerObject = (ObjectValue) configs.get(KafkaConstants.CONSUMER_KEY_DESERIALIZER_CONFIG);
        } else {
            this.deserializerObject = (ObjectValue) configs.get(KafkaConstants.CONSUMER_VALUE_DESERIALIZER_CONFIG);
        }
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        BArray bData = BValueCreator.createArrayValue(data);
        Object[] args = new Object[]{bData, false};
        return this.runtime.getSyncMethodInvokeResult(this.deserializerObject, KafkaConstants.FUNCTION_DESERIALIZE,
                                                      args);
    }

    @Override
    public void close() {
        BRuntime.getCurrentRuntime().getSyncMethodInvokeResult(this.deserializerObject, KafkaConstants.FUNCTION_CLOSE);
    }
}
