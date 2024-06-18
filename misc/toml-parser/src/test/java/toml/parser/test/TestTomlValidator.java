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

package toml.parser.test;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A test class that prints results based on a schema and sample toml file.
 *
 * @since 2.0.0
 */
public class TestTomlValidator {
    private static final PrintStream OUT = System.out;

    private TestTomlValidator() {
    }

    public static void main(String [] args) throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "validator", "sample-schema.json");
        TomlValidator validator = new TomlValidator(Schema.from(resourceDirectory));

        Path sampleInput = Paths.get("src", "test", "resources", "validator", "sample.toml");

        Toml toml = Toml.read(sampleInput);
        validator.validate(toml);

        for (Diagnostic d: toml.diagnostics()) {
            OUT.println(d);
        }
    }
}
