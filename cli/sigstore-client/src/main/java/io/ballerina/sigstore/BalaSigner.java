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

import dev.sigstore.KeylessSigner;
import dev.sigstore.bundle.Bundle;
import dev.sigstore.oidc.client.GithubActionsOidcClient;
import dev.sigstore.oidc.client.OidcClient;
import dev.sigstore.oidc.client.OidcClients;
import dev.sigstore.oidc.client.TokenStringOidcClient;
import dev.sigstore.oidc.client.WebOidcClient;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Signs a bala keyless with the public Sigstore instance and writes the bundle next to it.
 *
 * @since 2201.14.0
 */
public final class BalaSigner {

    public static final String BUNDLE_EXT = ".sigstore.json";
    static final String SIGSTORE_OIDC_ISSUER = "https://oauth2.sigstore.dev/auth";
    private static final int SAN_EMAIL = 1;
    private static final int SAN_URI = 6;
    private static final Logger SIGSTORE_LOGGER = Logger.getLogger("dev.sigstore");

    private BalaSigner() {
    }

    /**
     * Result of signing a bala.
     *
     * @param bundlePath path of the bundle written next to the bala
     * @param identity   identity in the signing certificate, such as a workflow URL or an email
     */
    public record Result(Path bundlePath, String identity) {
    }

    /**
     * Sign a bala and write {@code <bala>.sigstore.json} next to it.
     *
     * @param balaPath    bala to sign
     * @param environment where the identity token comes from
     * @param env         environment variables
     * @param outStream   stream for the browser login link
     * @return the bundle path and the signer identity
     * @throws SigningException if signing fails
     */
    public static Result sign(Path balaPath, SigningEnvironment environment, Map<String, String> env,
                              PrintStream outStream) throws SigningException {
        SIGSTORE_LOGGER.setLevel(Level.WARNING);
        Path bala = balaPath.toAbsolutePath().normalize();
        Path balaFileName = bala.getFileName();
        Path balaDir = bala.getParent();
        if (balaFileName == null || balaDir == null) {
            throw new SigningException("invalid bala path: " + balaPath);
        }
        Bundle bundle;
        try (KeylessSigner signer = KeylessSigner.builder()
                .sigstorePublicDefaults()
                // Central verifies Rekor v1 entries only
                .enableRekorV2(false)
                .oidcClients(OidcClients.of(oidcClient(environment, env, outStream)))
                .build()) {
            bundle = signer.signFile(bala);
        } catch (SigningException e) {
            throw e;
        } catch (Exception e) {
            throw new SigningException("signing failed: " + rootMessage(e), e);
        }

        Path bundlePath = balaDir.resolve(balaFileName + BUNDLE_EXT);
        Path tmp = null;
        try {
            tmp = Files.createTempFile(balaDir, ".sigstore", ".tmp");
            Files.writeString(tmp, bundle.toJson(), StandardCharsets.UTF_8);
            Files.move(tmp, bundlePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new SigningException("could not write the Sigstore bundle " + bundlePath + ": " + e.getMessage(),
                    e);
        } finally {
            deleteQuietly(tmp);
        }
        return new Result(bundlePath, signerIdentity(bundle));
    }

    private static OidcClient oidcClient(SigningEnvironment environment, Map<String, String> env,
                                         PrintStream outStream) throws SigningException {
        switch (environment.kind()) {
            case ID_TOKEN_ENV:
                return TokenStringOidcClient.from(env.get(SigningEnvironment.ID_TOKEN_ENV).trim());
            case GITHUB_ACTIONS:
                return GithubActionsOidcClient.builder().build();
            case TOKEN_FILE:
                try {
                    return TokenStringOidcClient.from(
                            Files.readString(environment.tokenFile(), StandardCharsets.UTF_8).trim());
                } catch (IOException e) {
                    throw new SigningException("could not read the identity token file " +
                            environment.tokenFile() + ": " + e.getMessage(), e);
                }
            case BROWSER:
            default:
                return WebOidcClient.builder()
                        .setIssuer(SIGSTORE_OIDC_ISSUER)
                        .setBrowser(url -> openBrowser(url, outStream))
                        .build();
        }
    }

    private static void openBrowser(String url, PrintStream outStream) {
        outStream.println("Log in through the browser. If it does not open, go to:");
        outStream.println("  " + url);
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> command;
        if (os.contains("win")) {
            command = List.of("rundll32", "url.dll,FileProtocolHandler", url);
        } else if (os.contains("mac")) {
            command = List.of("open", url);
        } else {
            command = List.of("xdg-open", url);
        }
        try {
            new ProcessBuilder(command).redirectErrorStream(true).start();
        } catch (IOException e) {
            // The link is printed above
        }
    }

    static String signerIdentity(Bundle bundle) {
        List<? extends Certificate> certs = bundle.getCertPath().getCertificates();
        if (certs.isEmpty() || !(certs.get(0) instanceof X509Certificate cert)) {
            return "";
        }
        try {
            Collection<List<?>> names = cert.getSubjectAlternativeNames();
            if (names == null) {
                return "";
            }
            for (List<?> name : names) {
                Object type = name.get(0);
                if (type instanceof Integer i && (i == SAN_URI || i == SAN_EMAIL)) {
                    return String.valueOf(name.get(1));
                }
            }
        } catch (CertificateParsingException e) {
            return "";
        }
        return "";
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Only a leftover temp file
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null ? cause.getClass().getSimpleName() : message;
    }
}
