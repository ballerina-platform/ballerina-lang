/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.BType;

import java.util.Map;

/**
 * The {@code BService} represents a service value in Ballerina.
 *
 * @since 0.965.0
 */
public class BService implements BRefType {

    private BServiceType type;

    public BService(BServiceType type) {
        this.type = type;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void stamp(BType type) {

    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return null;
    }
}
