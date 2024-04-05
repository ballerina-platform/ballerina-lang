/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.formatter.core.configurations;

import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.FormatterUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test resolution of formatting configuration file.
 *
 * @since 2201.9.0
 */
public class FormatFileResolutionTest {

    private final Path resDir = Path.of("src", "test", "resources", "configurations", "resolution");
    private final String validRemoteUrl =
            "https://gist.githubusercontent.com/ballerina-bot/ae54cc7303e9d474d730d732c1594c61/raw" +
                    "/8dc3204aec3f158105a811a6a67488bef45ff742/format.toml";
    private final Path validRemote = resDir.resolve("validRemote");
    private final Path withTarget = resDir.resolve("withTarget");
    private final Path invalidLocal = resDir.resolve("invalidLocal");

    @Test(description = "Test for local formatting configuration file")
    public void resolutionOfLocalFormatFileTest() throws FormatterException {
        Path validLocal = resDir.resolve("validLocal");
        FormatterUtils.getFormattingConfigurations(validLocal, validLocal.resolve("Format.toml").toString());
    }

    @Test(description = "Test for remote formatting configuration file")
    public void resolutionOfRemoteFormatFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(validRemote, validRemoteUrl);
    }

    @Test(description = "Test for cached remote formatting configuration file")
    public void resolutionOfCachedRemoteFormatFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(resDir.resolve("cached"), validRemoteUrl);
    }

    @Test(description = "Test caching of configuration file with target directory present")
    public void cacheWithTargetDirectoryPresent() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(withTarget, validRemoteUrl);
    }

    @Test(description = "Test invalid local formatting configuration files",
            expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to retrieve local formatting configuration file.*")
    public void invalidLocalFormatFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(invalidLocal, invalidLocal.resolve("directory.toml").toString());
    }

    @Test(description = "Test invalid remote cached formatting configuration files",
            expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to read cached formatting configuration file")
    public void invalidRemoteCachedFormatFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(resDir.resolve(Path.of("invalidCached")), validRemoteUrl);
    }

    @Test(description = "Test invalid remote file protocol", expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "configuration file remote url is not an HTTP url:.*")
    public void invalidRemoteFileProtocol() throws FormatterException {
        Path invalidUrl = resDir.resolve("invalidUrl");
        FormatterUtils.getFormattingConfigurations(invalidUrl, "ftp://example.com/Format.toml");
    }

    @Test(description = "Test invalid remote formatting configuration file url",
            expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to retrieve remote file. HTTP response code:.*")
    public void invalidRemoteFormatFileURLTest() throws FormatterException {
        Path invalidUrl = resDir.resolve("invalidUrl");
        FormatterUtils.getFormattingConfigurations(invalidUrl,
                "https://gist.github.com/ballerina-bot/ae54cc7303e9d474d730d732c1594c61/Format.toml");
    }

    @Test(description = "Test invalid formatting configuration files", expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to retrieve formatting configuration file.*")
    public void getInvalidFormatFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(invalidLocal, invalidLocal.resolve("t.toml").toString());
    }

    @Test(description = "Test invalid formatting configuration files", expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to create format configuration cache directory")
    public void failureToCreateFormatCacheFolderTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(resDir.resolve("invalidCacheTarget"), validRemoteUrl);
    }

    @Test(description = "Test invalid formatting configuration files", expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "failed to write format configuration cache file")
    public void failureToWriteCacheFileTest() throws FormatterException {
        FormatterUtils.getFormattingConfigurations(resDir.resolve("invalidCacheWrite"), validRemoteUrl);
    }

    @AfterClass
    public void tearDown() {
        ProjectUtils.deleteDirectory(validRemote.resolve("target"));
        ProjectUtils.deleteDirectory(withTarget.resolve("target").resolve("format"));
    }

}
