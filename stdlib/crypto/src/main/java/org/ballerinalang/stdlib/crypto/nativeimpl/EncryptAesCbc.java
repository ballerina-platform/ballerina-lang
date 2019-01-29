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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Extern function ballerina.crypto:signRsaMd5.
 *
 * @since 0.991.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "crypto", functionName = "encryptAesCbc", isPublic = true)
public class EncryptAesCbc extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue inputBValue = context.getRefArgument(0);
        byte[] input = ((BValueArray) inputBValue).getBytes();
        BValue keyBValue = context.getRefArgument(1);
        byte[] key = ((BValueArray) keyBValue).getBytes();
        String padding = context.getRefArgument(2).stringValue();
        BValue ivBValue = context.getNullableRefArgument(3);
        byte[] iv = null;
        if (ivBValue != null) {
            iv = ((BValueArray) ivBValue).getBytes();
        }
        CryptoUtils.aesEncryptDecrypt(context, CryptoUtils.CipherMode.ENCRYPT, Constants.CBC, padding, key, input, iv,
                -1);
    }
}
