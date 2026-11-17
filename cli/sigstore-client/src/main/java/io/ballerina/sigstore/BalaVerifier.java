/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.sigstore;

import dev.sigstore.KeylessVerifier;
import dev.sigstore.VerificationOptions;
import dev.sigstore.bundle.Bundle;
import dev.sigstore.bundle.BundleParseException;
import dev.sigstore.strings.StringMatcher;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks that a bala was signed by an expected identity, using the public Sigstore instance.
 *
 * @since 2201.14.0
 */
public final class BalaVerifier {

    private static final Logger SIGSTORE_LOGGER = Logger.getLogger("dev.sigstore");
    private static KeylessVerifier verifier;

    private BalaVerifier() {
    }

    /**
     * Verify a bala against its Sigstore bundle.
     *
     * @param balaPath bala to verify
     * @param bundle   Sigstore bundle JSON
     * @param issuer   OIDC issuer the signing certificate must name
     * @param subject  identity the signing certificate must name, such as a workflow URL or an email
     * @throws VerificationException if the bundle doesn't prove that the identity signed the bala
     * @throws SigningException      if the Sigstore trusted root can't be loaded
     */
    public static void verify(Path balaPath, String bundle, String issuer, String subject)
            throws VerificationException, SigningException {
        Bundle parsed;
        try {
            parsed = Bundle.from(new StringReader(bundle));
        } catch (BundleParseException e) {
            throw new VerificationException("invalid Sigstore bundle: " + e.getMessage(), e);
        }
        VerificationOptions options = VerificationOptions.builder()
                .addCertificateMatchers(VerificationOptions.CertificateMatcher.fulcio()
                        .issuer(StringMatcher.string(issuer))
                        .subjectAlternativeName(StringMatcher.string(subject))
                        .build())
                .build();
        try {
            getVerifier().verify(balaPath, parsed, options);
        } catch (dev.sigstore.KeylessVerificationException e) {
            throw new VerificationException(e.getMessage(), e);
        }
    }

    private static synchronized KeylessVerifier getVerifier() throws SigningException {
        if (verifier == null) {
            SIGSTORE_LOGGER.setLevel(Level.WARNING);
            try {
                verifier = KeylessVerifier.builder().sigstorePublicDefaults().build();
            } catch (Exception e) {
                throw new SigningException("could not load the Sigstore trusted root: " + e.getMessage(), e);
            }
        }
        return verifier;
    }

    /**
     * Thrown when a bundle doesn't prove that the expected identity signed the bala.
     */
    public static class VerificationException extends Exception {

        public VerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
