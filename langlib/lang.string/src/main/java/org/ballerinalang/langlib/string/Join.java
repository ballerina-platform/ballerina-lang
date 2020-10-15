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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.util.StringJoiner;

/**
 * Extern function lang.string:join(string, string...).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.string", functionName = "join",
//        args = {@Argument(name = "separator", type = TypeKind.STRING), @Argument(name = "strs", type = TypeKind
//        .ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.STRING)},
//        isPublic = true
//)
public class Join {

    public static BString join(BString separator, Object... strs) {
        StringJoiner stringJoiner = new StringJoiner(separator.getValue());
        int size = strs.length;
        for (int i = 0; i < size; i++) {
            String str = ((BString) strs[i]).getValue();
            stringJoiner.add(str);
        }
        return StringUtils.fromString(stringJoiner.toString());
    }
}
