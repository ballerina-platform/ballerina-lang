/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.nativeimpl.java;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * This class contains the implementation of the "isNull" ballerina function in ballerina/java module.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "java", version = "0.9.0",
        functionName = "isNull"
)
public class IsNull {

    public static boolean isNull(Strand strand, HandleValue value) {
        return value.getValue() == null;
    }
}
