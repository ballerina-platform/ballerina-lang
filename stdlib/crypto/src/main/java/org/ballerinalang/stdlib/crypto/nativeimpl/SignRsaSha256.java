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

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

import java.security.PrivateKey;

/**
 * Extern function ballerina.crypto:signRsaSha256.
 *
 * @since 0.990.3
 */
public class SignRsaSha256 {

    public static Object signRsaSha256(ArrayValue inputValue, MapValue<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("SHA256withRSA", key, input);
    }
}
