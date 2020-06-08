/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langlib.future;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import static org.ballerinalang.util.BLangCompilerConstants.FUTURE_VERSION;

/**
 * Extern function future.cancel().
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.future", version = FUTURE_VERSION,
        functionName = "cancel",
        args = {@Argument(name = "f", type = TypeKind.FUTURE)},
        isPublic = true
)
public class Cancel {

    public static void cancel(Strand strand, FutureValue futureValue) {
        futureValue.cancel();
    }
    
}
