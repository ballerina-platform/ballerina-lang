/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.HashMap;

/**
 * <p>
 * Interface to be implemented by all the ballerina objects.
 * </p>
 *
 * @since 1.1.0
 */
public interface BObject extends BRefValue {

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    @Deprecated(since = "2201.6.0", forRemoval = true)
    Object call(Strand strand, String funcName, Object... args);

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    @Deprecated(since = "2201.6.0", forRemoval = true)
    BFuture start(Strand strand, String funcName, Object... args);

    /**
     * Gets the type of ballerina object.
     *
     * @return Ballerina object type.
     * @deprecated use {@link BObject#getOriginalType()} ()} instead.
     * The API {@link BValue#getType()} should be used after fixing the issue #39850.
     */
    @Deprecated
    ObjectType getType();

    default Type getOriginalType() {
        return TypeUtils.getType(this);
    }

    Object get(BString fieldName);

    long getIntValue(BString fieldName);

    double getFloatValue(BString fieldName);

    BString getStringValue(BString fieldName);

    boolean getBooleanValue(BString fieldName);

    BMap getMapValue(BString fieldName);

    BObject getObjectValue(BString fieldName);

    BArray getArrayValue(BString fieldName);

    void addNativeData(String key, Object data);

    Object getNativeData(String key);

    HashMap<String, Object> getNativeData();

    void set(BString fieldName, Object value);
}
