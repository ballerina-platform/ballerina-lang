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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.internal.builtin;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.langlib.string.utils.StringUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.model.strings:contains.
 *
 * @since 0.992.0-m2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "contains",
        args = {@Argument(name = "src", type = TypeKind.STRING),
                @Argument(name = "substring", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class Contains {
    public static boolean contains(Strand strand, String value, String subString) {
        StringUtils.checkForNull(value, subString);
        return value.contains(subString);
    }
}
