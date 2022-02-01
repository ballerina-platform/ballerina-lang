/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package toml.parser.test.validator;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains test cases to to validate the custom error feature.
 *
 * @since 2.0.0
 */
public class CustomErrorTest {

    private final Path basePath = Paths.get("src", "test", "resources", "validator", "custom-error");

    @Test
    public void testClean() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("clean.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        int diagSize = toml.diagnostics().size();
        Assert.assertEquals(diagSize, 0);
    }

    @Test
    public void testRegexMessage() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("regex.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic customDiagMessage = toml.diagnostics().get(0);
        Assert.assertEquals(customDiagMessage.message(), "org can only contain a-z chars");

        Diagnostic defaultDiagMessage = toml.diagnostics().get(1);
        Assert.assertEquals(defaultDiagMessage.message(), "value for key 'version' expected to match the regex: ^" +
                "(?!\\s*$).+");
    }

    @Test
    public void testTypeMessage() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("type.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic customDiagMessage = toml.diagnostics().get(0);
        Assert.assertEquals(customDiagMessage.message(), "invalid type");

        Diagnostic defaultDiagMessage = toml.diagnostics().get(1);
        Assert.assertEquals(defaultDiagMessage.message(),
                "incompatible type for key 'org': expected 'STRING', found 'INTEGER'");
    }

    @Test
    public void testTableMessage() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("table.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic customDiagMessageRequired = toml.diagnostics().get(0);
        Assert.assertEquals(customDiagMessageRequired.message(), "field 'org' is required");

        Diagnostic customDiagMessageAddtionalProp = toml.diagnostics().get(1);
        Assert.assertEquals(customDiagMessageAddtionalProp.message(), "field 'additional' is not supported");
    }

    @Test
    public void testMinMaxLengthMessage() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("string-length.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic maxLenDiag = toml.diagnostics().get(0);
        Assert.assertEquals(maxLenDiag.message(), "Custom message for exceeding max length goes here");

        Diagnostic minLenDiag = toml.diagnostics().get(1);
        Assert.assertEquals(minLenDiag.message(), "Custom message for min length goes here");
    }
}
