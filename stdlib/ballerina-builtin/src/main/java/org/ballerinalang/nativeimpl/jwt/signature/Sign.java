/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.jwt.signature;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.nativeimpl.jwt.crypto.JWSSigner;
import org.ballerinalang.nativeimpl.jwt.crypto.RSASigner;
import org.ballerinalang.nativeimpl.security.KeyStore;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.security.PrivateKey;

/**
 * Native function ballerinalang.jwt.signature:verify.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jwt",
        functionName = "sign",
        args = {
                @Argument(name = "data", type = TypeKind.STRING),
                @Argument(name = "algorithm", type = TypeKind.STRING),
                @Argument(name = "keyAlias", type = TypeKind.STRING),
                @Argument(name = "keyPassword", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Sign extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String data = context.getStringArgument(0);
        String algorithm = context.getStringArgument(1);
        String keyAlias = context.getStringArgument(2);
        char[] keyPassword = context.getStringArgument(3).toCharArray();
        String signature = null;
        PrivateKey privateKey;
        try {
            if (keyAlias != null && !keyAlias.isEmpty()) {
                privateKey = KeyStore.getKeyStore().getPrivateKey(keyAlias, keyPassword);
            } else {
                privateKey = KeyStore.getKeyStore().getDefaultPrivateKey();
            }
            JWSSigner signer = new RSASigner(privateKey);
            signature = signer.sign(data, algorithm);
            context.setReturnValues(new BString(signature));
        } catch (Exception e) {
            context.setReturnValues(new BString(null), BLangVMErrors.createError(context, 0, e.getMessage()));
        }        
    }
}
