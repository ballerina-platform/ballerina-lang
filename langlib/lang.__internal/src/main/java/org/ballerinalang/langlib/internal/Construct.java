/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * Native implementation of lang.internal:construct(typeDesc, iterator).
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.__internal", functionName = "construct",
//        args = {
//                @Argument(name = "td", type = TypeKind.TYPEDESC),
//                @Argument(name = "iteratorObj", type = TypeKind.OBJECT)
//        },
//        returnType = {@ReturnType(type = TypeKind.STREAM)}
//)
public class Construct {

    public static BStream construct(BTypedesc td, BObject iteratorObj) {
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(td.getDescribingType()), iteratorObj);
    }
}
