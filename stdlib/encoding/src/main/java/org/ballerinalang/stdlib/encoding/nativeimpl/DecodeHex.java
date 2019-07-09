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

/**
 * Extern function ballerina.encoding:decodeHex.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "encoding",
        functionName = "decodeHex", isPublic = true
)
public class DecodeHex extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object decodeHex(Strand strand, String input) {
        try {
            byte[] output = EncodingUtil.decodeHex(input);
            return new ArrayValue(output);
        } catch (IllegalArgumentException e) {
            return EncodingUtil.createEncodingError("input is not a valid Hex value");
        }
    }
}
