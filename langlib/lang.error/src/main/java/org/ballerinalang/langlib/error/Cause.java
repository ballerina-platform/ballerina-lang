/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.langlib.error;

import io.ballerina.runtime.api.values.BError;

/**
 * Get error cause.
 *
 * @since 2.0.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.error", version = ERROR_VERSION,
//        functionName = "cause",
//        args = {@Argument(name = "value", type = TypeKind.ERROR)},
//        returnType = {@ReturnType(type = TypeKind.UNION)})
public class Cause {


    @Deprecated
    public static Object cause(BError value) {
        return value.getCause();
    }
}
