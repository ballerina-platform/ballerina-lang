/*
 * Copyright (c) (2022), WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;

public class GetClosureMap {

    public static BMap<?, ?> getBlockClosureMap(BObject obj) {
        BMap<?, ?> closureMap = (BMap<?, ?>) obj.getBlockClosureMap();
        return closureMap;
    }

    public static BMap<?, ?> getParamClosureMap(BObject obj, IntegerType level) {
        BMap<?, ?> closureMap = (BMap<?, ?>) obj.getParamClosureMap(level);
        return closureMap;
    }
}
