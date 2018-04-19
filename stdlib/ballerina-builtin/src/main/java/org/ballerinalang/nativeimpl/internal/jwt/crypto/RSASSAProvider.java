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

package org.ballerinalang.nativeimpl.internal.jwt.crypto;

/**
 * Provides the supported algorithms.
 *
 * <p>See RFC 7518, sections
 * <a href="https://tools.ietf.org/html/rfc7518#section-3.3">3.3</a> for more
 * information.
 *
 * @since 0.964.0
 */
public class RSASSAProvider {

    /**
     * Gets the matching JCA algorithm name for the specified RSA-based JSON Web Algorithm.
     *
     * @param algorithm The JSON Web Algorithm (JWA). Must not {@code null}.
     * @return The matching JCA algorithm name.
     * @throws JWSException If the algorithm is not supported.
     */
    protected static String getJCAAlgorithmName(final String algorithm)
            throws JWSException {
        if (JWSAlgorithm.RS256.equals(algorithm)) {
            return "SHA256withRSA";
        } else if (JWSAlgorithm.RS384.equals(algorithm)) {
            return "SHA384withRSA";
        } else if (JWSAlgorithm.RS512.equals(algorithm)) {
            return "SHA512withRSA";
        } else {
            throw new JWSException("Unsupported JWS algorithm" + algorithm);
        }
    }
}
