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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Where a keyless signature gets its identity token from, worked out from the environment.
 *
 * @since 2201.14.0
 */
public final class SigningEnvironment {

    public static final String ID_TOKEN_ENV = "SIGSTORE_ID_TOKEN";
    public static final Path DEFAULT_TOKEN_FILE = Path.of("/var/run/sigstore/cosign/oidc-token");

    /**
     * Source of the identity token.
     */
    public enum Kind {
        ID_TOKEN_ENV("the identity token in " + SigningEnvironment.ID_TOKEN_ENV),
        GITHUB_ACTIONS("the GitHub Actions identity token"),
        TOKEN_FILE("the identity token file"),
        BROWSER("a browser login");

        private final String description;

        Kind(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    private final Kind kind;
    private final Path tokenFile;

    private SigningEnvironment(Kind kind, Path tokenFile) {
        this.kind = kind;
        this.tokenFile = tokenFile;
    }

    public Kind kind() {
        return kind;
    }

    Path tokenFile() {
        return tokenFile;
    }

    /**
     * Work out how to get an identity token for keyless signing.
     *
     * @param env         environment variables
     * @param tokenFile   file a Kubernetes pod mounts a token with the sigstore audience at
     * @param interactive whether a person is at the terminal to log in through a browser
     * @return the signing environment
     * @throws SigningException with setup instructions when no identity is available
     */
    public static SigningEnvironment detect(Map<String, String> env, Path tokenFile, boolean interactive)
            throws SigningException {
        if (isSet(env, ID_TOKEN_ENV)) {
            return new SigningEnvironment(Kind.ID_TOKEN_ENV, null);
        }
        boolean githubActions = "true".equalsIgnoreCase(env.get("GITHUB_ACTIONS"));
        if (githubActions && isSet(env, "ACTIONS_ID_TOKEN_REQUEST_URL") &&
                isSet(env, "ACTIONS_ID_TOKEN_REQUEST_TOKEN")) {
            return new SigningEnvironment(Kind.GITHUB_ACTIONS, null);
        }
        if (tokenFile != null && Files.isRegularFile(tokenFile)) {
            return new SigningEnvironment(Kind.TOKEN_FILE, tokenFile);
        }
        if (githubActions) {
            throw new SigningException("GitHub Actions detected but no identity token is available. " +
                    "Add 'permissions: id-token: write' to the workflow or job.");
        }
        if ("true".equalsIgnoreCase(env.get("GITLAB_CI"))) {
            throw new SigningException("GitLab CI detected but no identity token is available. Add to the job:" +
                    System.lineSeparator() + "  id_tokens:" + System.lineSeparator() + "    " + ID_TOKEN_ENV +
                    ":" + System.lineSeparator() + "      aud: sigstore");
        }
        if (isSet(env, "JENKINS_URL")) {
            throw new SigningException("Jenkins detected but no identity token is available. Jenkins can sign " +
                    "keyless only when its agents run on Kubernetes in EKS, GKE or AKS: mount a service account " +
                    "token with the audience 'sigstore' at " + DEFAULT_TOKEN_FILE + " or set " + ID_TOKEN_ENV + ".");
        }
        if ("true".equalsIgnoreCase(env.get("CI")) || isSet(env, "TF_BUILD")) {
            throw new SigningException("CI detected but no identity token is available. Set " + ID_TOKEN_ENV +
                    " to an OIDC token with the audience 'sigstore' from an issuer Sigstore trusts.");
        }
        if (interactive) {
            return new SigningEnvironment(Kind.BROWSER, null);
        }
        throw new SigningException("no identity token is available. Set " + ID_TOKEN_ENV +
                " to an OIDC token with the audience 'sigstore', or run the command in a terminal to log in " +
                "through a browser.");
    }

    private static boolean isSet(Map<String, String> env, String name) {
        String value = env.get(name);
        return value != null && !value.isBlank();
    }
}
