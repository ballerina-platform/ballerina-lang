/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.map;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Extern function to get key arrays from the map.
 * ballerina.model.map:keys()
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.map",
//        functionName = "keys",
//        args = {@Argument(name = "m", type = TypeKind.MAP)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
//        isPublic = true
//)
public final class GetKeys {

    private GetKeys() {
    }

    public static BArray keys(BMap<?, ?> m) {
        return ValueCreator.createArrayValue((BString[]) m.getKeys());
    }
}
