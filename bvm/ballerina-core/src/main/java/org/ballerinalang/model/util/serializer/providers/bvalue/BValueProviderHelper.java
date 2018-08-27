/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.util.serializer.providers.bvalue;

import org.ballerinalang.model.util.serializer.JsonSerializerConst;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Hold useful helper functions used in mapping between Java objects and BValue objects.
 *
 * @since 0.98.1
 */
public class BValueProviderHelper {

    public static BallerinaException deserializationIncorrectType(BValue source, String target) {
        return new BallerinaException(
                String.format("Can not convert %s to %s", source, target));
    }


    public static BMap<String, BValue> wrap(String typeName, BValue payload) {
        BMap<String, BValue> wrapper = new BMap<>();
        wrapper.put(JsonSerializerConst.TYPE_TAG, new BString(typeName));
        wrapper.put(JsonSerializerConst.PAYLOAD_TAG, payload);
        return wrapper;
    }


    public static BValue getPayload(BMap<String, BValue> wrapper) {
        return wrapper.get(JsonSerializerConst.PAYLOAD_TAG);
    }

    public static boolean isWrapperOfType(BMap<String, BValue> wrapper, String type) {
        return wrapper.get(JsonSerializerConst.TYPE_TAG).stringValue().equals(type);
    }
}
