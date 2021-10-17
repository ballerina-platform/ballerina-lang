/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.util.List;

/**
 * Contains test cases to to validate the validate functionality.
 *
 * @since 2.0.0
 */
public class TomlValidateTest {

    private final Path basePath = Paths.get("src", "test", "resources", "validator", "basic");

    @Test
    public void testClean() throws IOException {
        Path resourceDirectory = basePath.resolve("c2c-schema.json");
        Path sampleInput = basePath.resolve("c2c-clean.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        int diagSize = toml.diagnostics().size();
        Assert.assertEquals(diagSize, 0);
    }

    @Test
    public void testBalClean() throws IOException {
        Path resourceDirectory = basePath.resolve("dep-new.json");
        Path sampleInput = basePath.resolve("bal-clean.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        int diagSize = toml.diagnostics().size();
        Assert.assertEquals(diagSize, 0);
    }

    @Test
    public void testInvalidType() throws IOException {
        Path resourceDirectory = basePath.resolve("c2c-schema.json");
        Path sampleInput = basePath.resolve("c2c-invalid-type.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(),
                "incompatible type for key 'base': expected 'STRING', found 'INTEGER'");
    }

    @Test
    public void testInvalidRegex() throws IOException {
        Path resourceDirectory = basePath.resolve("c2c-schema.json");
        Path sampleInput = basePath.resolve("c2c-invalid-regex.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(),
                "value for key 'name' expected to match the regex: [a-zA-Z0-9][a-zA-Z0-9_.-]+");
    }

    @Test
    public void testInvalidMinMax() throws IOException {
        Path resourceDirectory = basePath.resolve("c2c-schema.json");
        Path sampleInput = basePath.resolve("c2c-invalid-min-max.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        List<Diagnostic> validate = toml.diagnostics();

        Diagnostic diagnostic = validate.get(0);
        Assert.assertEquals(diagnostic.message(), "value for key 'cpu' can't be lower than 1.000000");

        Diagnostic diagnostic1 = validate.get(1);
        Assert.assertEquals(diagnostic1.message(), "value for key 'memory' can't be higher than 100.000000");
    }

    @Test
    public void testInvalidAdditionalProperty() throws IOException {
        Path resourceDirectory = basePath.resolve("c2c-schema.json");
        Path sampleInput = basePath.resolve("c2c-invalid-additional-properties.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(), "key 'field' not supported in schema 'C2C Spec'");
    }

    @Test
    public void testArrayTablesMultipleMissingEntry() throws IOException {
        Path resourceDirectory = basePath.resolve("dependency-schema.json");
        Path sampleInput = basePath.resolve("Dependencies.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        List<Diagnostic> diagnostics = toml.diagnostics();
        Assert.assertEquals(diagnostics.size(), 2);

        Diagnostic diagnostic = diagnostics.get(0);
        Diagnostic diagnostic1 = diagnostics.get(1);
        Assert.assertEquals(diagnostic.message(), "'version' of the dependency is missing");
        Assert.assertEquals(diagnostic1.message(), "'version' of the dependency is missing");
    }

    @Test
    public void testInlineValue() throws IOException {
        Path resourceDirectory = basePath.resolve("inline-value.json");
        Path sampleInput = basePath.resolve("inline-value.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        List<Diagnostic> diagnostics = toml.diagnostics();
        Assert.assertEquals(diagnostics.size(), 2);

        Diagnostic diagnostic = diagnostics.get(0);
        Diagnostic diagnostic1 = diagnostics.get(1);
        Assert.assertEquals(diagnostic.message(), "missing required field 'name'");
        Assert.assertEquals(diagnostic1.message(), "key 'test' not supported in schema 'Dependencies Toml Spec'");
    }

    @Test
    public void testMinMaxLengthMessage() throws IOException {
        Path resourceDirectory = basePath.resolve("schema.json");
        Path sampleInput = basePath.resolve("string-length.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic maxLenDiag = toml.diagnostics().get(0);
        Assert.assertEquals(maxLenDiag.message(),
                "length of the value for key 'name' is greater than defined max length 10");

        Diagnostic minLenDiag = toml.diagnostics().get(1);
        Assert.assertEquals(minLenDiag.message(),
                "length of the value for key 'org' is lower than defined min length 3");
    }
}
