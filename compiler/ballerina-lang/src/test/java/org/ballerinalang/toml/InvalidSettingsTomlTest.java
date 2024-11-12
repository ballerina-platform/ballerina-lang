/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml;

import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.SettingsBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * Test class to test invalid `Settings.toml` file.
 */
public class InvalidSettingsTomlTest {

    @Test(description = "Test invalid Settings.toml")
    public void testParseTomlContentFromFile() throws IOException, URISyntaxException {
        URI settingsTomlURI = Objects.requireNonNull(getClass().getClassLoader().getResource("invalid-settings.toml"))
                .toURI();
        Path settingsFilePath = Path.of(settingsTomlURI);

        TomlDocument settingsTomlDocument = TomlDocument
                .from(String.valueOf(settingsFilePath.getFileName()), Files.readString(settingsFilePath));
        SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
        Settings settings = settingsBuilder.settings();
        Assert.assertTrue(settings.diagnostics().hasErrors());
        Collection<Diagnostic> errors = settings.diagnostics().errors();
        Assert.assertEquals(errors.size(), 2);
        Diagnostic firstDiagnostic = errors.iterator().next();
        Assert.assertEquals(firstDiagnostic.message(), "missing equal token");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(2:0,2:0)");
    }
}
