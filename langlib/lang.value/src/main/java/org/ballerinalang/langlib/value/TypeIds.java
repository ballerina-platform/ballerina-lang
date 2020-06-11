/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.typedesc;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function lang.typedesc:typeIds.
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.typedesc", functionName = "typeIds",
        args = {
                @Argument(name = "t", type = TypeKind.TYPEDESC),
                @Argument(name = "primaryOnly", type = TypeKind.BOOLEAN)
        },
        returnType = {
                @ReturnType(type = TypeKind.ARRAY)
        },
        isPublic = true
)
public class TypeIds {

    public static ArrayValue typeIds(Strand strand, TypedescValue t, boolean primaryOnly) {
        return null;
    }
}
