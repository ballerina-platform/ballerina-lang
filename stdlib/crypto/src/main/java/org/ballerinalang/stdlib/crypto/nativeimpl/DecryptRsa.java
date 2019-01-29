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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Extern function ballerina.crypto:signRsaMd5.
 *
 * @since 0.991.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "crypto", functionName = "decryptRsa", isPublic = true)
public class DecryptRsa extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue inputBValue = context.getRefArgument(0);
        BMap<String, BValue> keyMap = (BMap<String, BValue>) context.getRefArgument(3);
        byte[] input = ((BValueArray) inputBValue).getBytes();
        String mode = context.getRefArgument(1).stringValue();
        String padding = context.getRefArgument(2).stringValue();
        BValue ivBValue = context.getNullableRefArgument(3);
        byte[] iv = null;
        if (ivBValue != null) {
            iv = ((BValueArray) ivBValue).getBytes();
        }
        try {
            Key key;
            if (keyMap.get(Constants.NATIVE_DATA_PRIVATE_KEY) != null) {
                key = (PrivateKey) keyMap.get(Constants.NATIVE_DATA_PRIVATE_KEY);
            } else if (keyMap.get(Constants.NATIVE_DATA_PUBLIC_KEY) != null) {
                key = (PublicKey) keyMap.get(Constants.NATIVE_DATA_PUBLIC_KEY);
            } else {
                throw new BallerinaException("error", context);
            }
            context.setReturnValues(new BValueArray(CryptoUtils.rsaEncryptDecrypt(context,
                    CryptoUtils.CipherMode.DECRYPT, mode, padding, key, input, iv)));
        } catch (InvalidKeyException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "invalid uninitialized key"));
        }
    }
}
