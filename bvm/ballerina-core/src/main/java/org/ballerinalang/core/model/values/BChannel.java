/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;

import java.util.Map;

/**
 * The {@code BChannel} represents a channel in Ballerina.
 *
 * @since 0.982.0
 */
public class BChannel implements BRefType<Object> {

    private String channelName;
    private BType constraintType;

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeChannel;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public BType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(BType constraintType) {
        this.constraintType = constraintType;
    }
}
