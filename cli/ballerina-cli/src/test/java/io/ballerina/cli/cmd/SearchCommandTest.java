/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.projects.Settings;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.maven.bala.client.model.PackageSearchEntry;
import org.ballerinalang.maven.bala.client.model.PkgSearchMavenMetadata;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Search command tests.
 *
 * @since 2.0.
 */
public class SearchCommandTest extends BaseCommandTest {

    @Test(description = "Search without keyword")
    public void testSearchWithoutKeyword() throws IOException {
        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs();
        searchCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: no keyword given"));
        Assert.assertTrue(actual.contains("bal search [<org>|<package>|<text>]"));
    }

    @Test(description = "Search with too many args")
    public void testPullWithTooManyArgs() throws IOException {
        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs("wso2", "tests");
        searchCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal search [<org>|<package>|<text>]"));
    }

    @Test(description = "Test search command with argument and a help")
    public void testSearchCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs(args);
        searchCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-search - Search Ballerina Central for packages"));
    }

    @Test(description = "Test search command with help flag")
    public void testSearchCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "-h" };
        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs(args);
        searchCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-search - Search Ballerina Central for packages"));
    }

    @Test(description = "Test search via Maven proxy - package found")
    public void testSearchPackageFoundInMvnProxy() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        PackageSearchEntry entry = new PackageSearchEntry();
        entry.setOrg("myorg");
        entry.setName("mypkg");
        entry.setVersion("1.0.0");
        List<PackageSearchEntry> packages = new ArrayList<>();
        packages.add(entry);
        PkgSearchMavenMetadata metadata = new PkgSearchMavenMetadata();
        metadata.setPackages(packages);

        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs("mypkg");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getPkgSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenReturn(metadata))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            searchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("myorg"), "Expected output to contain 'myorg'");
        Assert.assertTrue(output.contains("mypkg"), "Expected output to contain 'mypkg'");
        Assert.assertTrue(output.contains("1.0.0"), "Expected output to contain '1.0.0'");
    }

    @Test(description = "Test search via Maven proxy - no packages found")
    public void testSearchNoPackagesFoundInMvnProxy() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        PkgSearchMavenMetadata metadata = new PkgSearchMavenMetadata();
        metadata.setPackages(new ArrayList<>());

        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs("unknownpkg");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getPkgSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenReturn(metadata))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            searchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("no modules found"), "Expected output to contain 'no modules found'");
    }

    @Test(description = "Test search via Maven proxy - client throws exception")
    public void testSearchMvnProxyClientException() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        SearchCommand searchCommand = new SearchCommand(printStream, printStream, false);
        new CommandLine(searchCommand).parseArgs("mypkg");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getPkgSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenThrow(new MavenResolverClientException("proxy error")))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            searchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("proxy error"), "Expected output to contain 'proxy error'");
    }
}
