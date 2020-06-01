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

package org.ballerinalang.langlib.string;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.StringJoiner;

import static org.ballerinalang.util.BLangCompilerConstants.STRING_VERSION;

/**
 * Extern function lang.string:join(string, string...).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string", version = STRING_VERSION, functionName = "join",
        args = {@Argument(name = "separator", type = TypeKind.STRING), @Argument(name = "strs", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Join {

    public static BString join(Strand strand, BString separator, ArrayValue strs) {
        StringJoiner stringJoiner = new StringJoiner(separator.getValue());
        int size = strs.size();
        for (int i = 0; i < size; i++) {
            stringJoiner.add(strs.getBString(i).getValue());
        }
        return StringUtils.fromString(stringJoiner.toString());
    }
}
