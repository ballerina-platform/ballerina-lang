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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Tests how the signing environment is detected.
 */
public class SigningEnvironmentTest {

    private static final Map<String, String> GITHUB = Map.of("GITHUB_ACTIONS", "true",
            "ACTIONS_ID_TOKEN_REQUEST_URL", "https://token", "ACTIONS_ID_TOKEN_REQUEST_TOKEN", "t");
    private Path tokenFile;
    private final Path missingFile = Path.of("does-not-exist-oidc-token");

    @BeforeClass
    public void setUp() throws IOException {
        tokenFile = Files.createTempFile("oidc-token", "");
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(tokenFile);
    }

    @Test
    public void testIdTokenEnvWinsEverywhere() throws SigningException {
        Map<String, String> env = Map.of("SIGSTORE_ID_TOKEN", "x", "GITHUB_ACTIONS", "true", "JENKINS_URL", "u");
        Assert.assertEquals(SigningEnvironment.detect(env, tokenFile, true).kind(),
                SigningEnvironment.Kind.ID_TOKEN_ENV);
    }

    @Test
    public void testGithubActions() throws SigningException {
        Assert.assertEquals(SigningEnvironment.detect(GITHUB, missingFile, false).kind(),
                SigningEnvironment.Kind.GITHUB_ACTIONS);
    }

    @Test
    public void testGithubActionsWithoutIdTokenPermission() {
        SigningException e = Assert.expectThrows(SigningException.class, () -> SigningEnvironment.detect(
                Map.of("GITHUB_ACTIONS", "true", "CI", "true"), missingFile, false));
        Assert.assertTrue(e.getMessage().contains("id-token: write"), e.getMessage());
    }

    @Test
    public void testTokenFile() throws SigningException {
        SigningEnvironment environment = SigningEnvironment.detect(Map.of("JENKINS_URL", "u"), tokenFile, false);
        Assert.assertEquals(environment.kind(), SigningEnvironment.Kind.TOKEN_FILE);
        Assert.assertEquals(environment.tokenFile(), tokenFile);
    }

    @Test
    public void testGitlabWithoutToken() {
        SigningException e = Assert.expectThrows(SigningException.class, () -> SigningEnvironment.detect(
                Map.of("GITLAB_CI", "true", "CI", "true"), missingFile, false));
        Assert.assertTrue(e.getMessage().contains("aud: sigstore"), e.getMessage());
    }

    @Test
    public void testJenkinsWithoutToken() {
        SigningException e = Assert.expectThrows(SigningException.class, () -> SigningEnvironment.detect(
                Map.of("JENKINS_URL", "https://jenkins"), missingFile, true));
        Assert.assertTrue(e.getMessage().startsWith("Jenkins detected"), e.getMessage());
    }

    @Test
    public void testOtherCiWithoutToken() {
        SigningException e = Assert.expectThrows(SigningException.class, () -> SigningEnvironment.detect(
                Map.of("CI", "true", "BUILDKITE", "true"), missingFile, true));
        Assert.assertTrue(e.getMessage().startsWith("CI detected"), e.getMessage());
    }

    @Test
    public void testLaptop() throws SigningException {
        Assert.assertEquals(SigningEnvironment.detect(Map.of(), missingFile, true).kind(),
                SigningEnvironment.Kind.BROWSER);
    }

    @Test
    public void testNoTerminalNoToken() {
        SigningException e = Assert.expectThrows(SigningException.class, () -> SigningEnvironment.detect(
                Map.of(), missingFile, false));
        Assert.assertTrue(e.getMessage().contains("SIGSTORE_ID_TOKEN"), e.getMessage());
    }

    @Test
    public void testBlankIdTokenIgnored() throws SigningException {
        Assert.assertEquals(SigningEnvironment.detect(Map.of("SIGSTORE_ID_TOKEN", " "), missingFile, true).kind(),
                SigningEnvironment.Kind.BROWSER);
    }
}
