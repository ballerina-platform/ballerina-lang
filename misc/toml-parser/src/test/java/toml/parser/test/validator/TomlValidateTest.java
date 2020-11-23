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

    @Test
    public void testClean() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "c2c-schema.json");
        Path sampleInput = Paths.get("src", "test", "resources", "validator", "c2c-clean.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        int diagSize = toml.diagnostics().size();
        Assert.assertEquals(diagSize, 0);
    }

    @Test
    public void testInvalidType() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "c2c-schema.json");
        Path sampleInput = Paths.get("src", "test", "resources", "validator", "c2c-invalid-type.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(), "Key \"base\" expects STRING . Found integer");
    }

    @Test
    public void testInvalidRegex() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "c2c-schema.json");
        Path sampleInput = Paths.get("src", "test", "resources", "validator", "c2c-invalid-regex.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(), "Key \"name\" value does not match the Regex provided in Schema " +
                "[a-zA-Z0-9][a-zA-Z0-9_.-]+");
    }

    @Test
    public void testInvalidMinMax() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "c2c-schema.json");
        Path sampleInput = Paths.get("src", "test", "resources", "validator", "c2c-invalid-min-max.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        List<Diagnostic> validate = toml.diagnostics();

        Diagnostic diagnostic = validate.get(0);
        Assert.assertEquals(diagnostic.message(), "Key \"cpu\" value can't be lower than 1.000000");

        Diagnostic diagnostic1 = validate.get(1);
        Assert.assertEquals(diagnostic1.message(), "Key \"memory\" value can't be higher than 100.000000");
    }

    @Test
    public void testInvalidAdditionalProperty() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "c2c-schema.json");
        Path sampleInput = Paths.get("src", "test", "resources", "validator", "c2c-invalid-additional-properties.toml");

        Toml toml = Toml.read(sampleInput, Schema.from(resourceDirectory));

        Diagnostic diagnostic = toml.diagnostics().get(0);
        Assert.assertEquals(diagnostic.message(), "Unexpected Property \"field\"");
    }
}
