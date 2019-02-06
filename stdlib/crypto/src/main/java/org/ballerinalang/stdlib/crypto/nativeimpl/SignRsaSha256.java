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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

import java.security.InvalidKeyException;
import java.security.PrivateKey;

/**
 * Extern function ballerina.crypto:signRsaSha256.
 *
 * @since 0.991.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "signRsaSha256",
        args = {
                @Argument(name = "input", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "privateKey", type = TypeKind.RECORD, structType = "PrivateKey",
                        structPackage = "ballerina/crypto")
        },
        returnType = {
                @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.RECORD, structType = Constants.CRYPTO_ERROR,
                        structPackage = Constants.CRYPTO_PACKAGE)
        },
        isPublic = true)
public class SignRsaSha256 extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue inputBValue = context.getRefArgument(0);
        BMap<String, BValue> privateKey = (BMap<String, BValue>) context.getRefArgument(1);
        byte[] input = ((BValueArray) inputBValue).getBytes();
        try {
            context.setReturnValues(new BValueArray(CryptoUtils.sign(context, "SHA256withRSA",
                    (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY), input)));
        } catch (InvalidKeyException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "invalid uninitialized key"));
        }
    }
}
