/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.__internal;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Return elements matching at least one of `elemNames`
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.__internal",
        functionName = "getFilteredChildrenFlat",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML),
                @Argument(name = "index", type = TypeKind.INT),
                @Argument(name= "elemNames", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class GetFilteredChildrenFlat {


    public static String getName(Strand strand, XMLValue xmlVal, int index, ArrayValue elemNames) {

        return null;
    }
}
