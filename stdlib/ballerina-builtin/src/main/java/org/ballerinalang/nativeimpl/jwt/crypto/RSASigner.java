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

package org.ballerinalang.nativeimpl.jwt.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

/**
 * Sign with RSA algorithms. Expects a private RSA key.
 *
 * <p>See RFC 7518, sections
 * <a href="https://tools.ietf.org/html/rfc7518#section-3.3">3.3</a> for more
 * information.
 *
 * @since 0.964.0
 */
public class RSASigner implements JWSSigner {

    /**
     * The private RSA key.
     */
    private final PrivateKey privateKey;

    /**
     * Creates a new RSA signer.
     *
     * @param privateKey The private RSA key, algorithm must be "RSA".
     *                   Must not be {@code null}.
     */
    public RSASigner(final PrivateKey privateKey) {
        if (!"RSA".equalsIgnoreCase(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("The private key algorithm must be RSA");
        }
        this.privateKey = privateKey;
    }

    @Override
    public String sign(String data, String algorithm) throws JWSException {
        byte[] signatureBytes;
        final Signature signature;
        try {
            signature = Signature.getInstance(RSASSAProvider.getJCAAlgorithmName(algorithm));
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            signatureBytes = signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new JWSException(e.getMessage(), e);
        }
        return new String(Base64.getUrlEncoder().encode(signatureBytes), StandardCharsets.UTF_8);
    }
}
