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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.jwt.crypto.JWSVerifier;
import org.ballerinalang.stdlib.internal.jwt.crypto.RSAVerifier;
import org.ballerinalang.stdlib.internal.jwt.crypto.TrustStoreHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Extern function ballerinalang.jwt:verifySignature.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "verifySignature",
        args = {
                @Argument(name = "data", type = TypeKind.STRING),
                @Argument(name = "signature", type = TypeKind.STRING),
                @Argument(name = "algorithm", type = TypeKind.STRING),
                @Argument(name = "trustStore", type = TypeKind.RECORD, structType = "TrustStoreHolder",
                        structPackage = "ballerina/internal")
        },
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = STRUCT_GENERIC_ERROR, structPackage =
                        BALLERINA_BUILTIN_PKG)
        },
        isPublic = true
)
public class VerifySignature extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(VerifySignature.class);
    private static final String CERT_ALIAS = "certificateAlias";
    private static final String TRUST_STORE_PATH = "trustStoreFilePath";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    @Override
    public void execute(Context context) {
        String data = context.getStringArgument(0);
        String signature = context.getStringArgument(1);
        String algorithm = context.getStringArgument(2);
        BMap<String, BValue> trustStore = (BMap<String, BValue>) context.getRefArgument(0);
        char[] trustStorePassword = trustStore.get(TRUST_STORE_PASSWORD).stringValue().toCharArray();
        RSAPublicKey publicKey;
        String msg = null;
        try {
            X509Certificate certificate = (X509Certificate) TrustStoreHolder.getInstance().getTrustedCertificate(
                    trustStore.get(CERT_ALIAS).stringValue(),
                    PathResolver.getResolvedPath(trustStore.get(TRUST_STORE_PATH).stringValue()), trustStorePassword);
            certificate.checkValidity();
            publicKey = (RSAPublicKey) certificate.getPublicKey();

            JWSVerifier verifier = new RSAVerifier(publicKey);
            if (!verifier.verify(data, signature, algorithm)) {
                msg = "Invalid signature";
            }
        } catch (CertificateExpiredException e) {
            msg = "Certificate with alias " + trustStore.get(CERT_ALIAS) + " has expired";
        } catch (CertificateNotYetValidException e) {
            msg = "Certificate with alias " + trustStore.get(CERT_ALIAS) + " is not yet valid";
        } catch (Exception e) {
            msg = "Error in verifying signature";
            log.error(msg, e);
        }
        if (msg != null) {
            context.setReturnValues(BLangVMErrors.createError(context, msg));
        }
    }
}
