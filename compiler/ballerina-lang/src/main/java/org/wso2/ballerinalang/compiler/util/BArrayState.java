/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.ballerinalang.compiler.util;

import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to hold the state of a {@link BArrayType}.
 */
public enum BArrayState {
    CLOSED((byte) 1),
    INFERRED((byte) 2),
    OPEN((byte) 3);

    byte value;
    private static final Map<Byte, BArrayState> map = new HashMap<>();

    BArrayState(byte value) {
        this.value = value;
    }

    static {
        for (BArrayState pageType : BArrayState.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static BArrayState valueOf(byte state) {
        return map.get(state);
    }

    public byte getValue() {
        return this.value;
    }

}
