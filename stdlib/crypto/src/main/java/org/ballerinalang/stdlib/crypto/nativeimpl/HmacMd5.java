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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.crypto.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

/**
 * Extern function ballerina.crypto:hmacMd5.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "hmacMd5", isPublic = true
)
public class HmacMd5 {

    public static ArrayValue hmacMd5(Strand strand, ArrayValue inputValue, ArrayValue keyValue) {
        return new ArrayValueImpl(CryptoUtils.hmac("HmacMD5", keyValue.getBytes(), inputValue.getBytes()));
    }
}
