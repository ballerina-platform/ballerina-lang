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

package org.ballerinalang.stdlib.internal.jwt.signature;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.jwt.crypto.JWSSigner;
import org.ballerinalang.stdlib.internal.jwt.crypto.KeyStoreHolder;
import org.ballerinalang.stdlib.internal.jwt.crypto.RSASigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;

/**
 * Native function ballerinalang.jwt:sign.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "sign",
        args = {
                @Argument(name = "", type = TypeKind.STRING),
                @Argument(name = "algorithm", type = TypeKind.STRING),
                @Argument(name = "keyStore", type = TypeKind.RECORD, structType = "KeyStore",
                        structPackage = "ballerina/internal")
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Sign extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Sign.class);
    private static final String KEY_ALIAS_FIELD = "keyAlias";
    private static final String KEY_PASSWORD_FIELD = "keyPassword";
    private static final String KEY_STORE_PATH_FIELD = "keyStoreFilePath";
    private static final String KEY_STORE_PASSWORD_FIELD = "keyStorePassword";

    @Override
    public void execute(Context context) {
        String data = context.getStringArgument(0);
        String algorithm = context.getStringArgument(1);
        BMap<String, BValue> keyStore = (BMap<String, BValue>) context.getRefArgument(0);
        char[] keyPassword = keyStore.get(KEY_PASSWORD_FIELD).stringValue().toCharArray();
        char[] keyStorePassword = keyStore.get(KEY_STORE_PASSWORD_FIELD).stringValue().toCharArray();
        String signature = null;
        PrivateKey privateKey;
        try {
            privateKey = KeyStoreHolder.getInstance().getPrivateKey(keyStore.get(KEY_ALIAS_FIELD).stringValue(),
                    keyPassword, PathResolver.getResolvedPath(keyStore.get(KEY_STORE_PATH_FIELD).stringValue()),
                    keyStorePassword);
            JWSSigner signer = new RSASigner(privateKey);
            signature = signer.sign(data, algorithm);
            context.setReturnValues(new BString(signature));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(new BString(null), BLangVMErrors.createError(context, e.getMessage()));
        }
    }
}
