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

package org.ballerinalang.stdlib.encoding.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.encoding.EncodingUtil;

import java.util.Base64;

/**
 * Extern function ballerina.encoding:decodeBase64.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "encoding",
        functionName = "decodeBase64", isPublic = true
)
public class DecodeBase64 extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object decodeBase64(Strand strand, String input) {
        try {
            byte[] output = Base64.getDecoder().decode(input);
            return new ArrayValue(output);
        } catch (IllegalArgumentException e) {
            return EncodingUtil.createEncodingError("input is not a valid Base64 value");
        }
    }
}
