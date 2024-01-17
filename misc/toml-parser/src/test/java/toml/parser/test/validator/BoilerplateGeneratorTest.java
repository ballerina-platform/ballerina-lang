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

import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.validator.BoilerplateGenerator;
import io.ballerina.toml.validator.schema.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains test cases to to boilerplate toml generation functionality.
 *
 * @since 2.0.0
 */
public class BoilerplateGeneratorTest {
    @Test
    public void testC2cSchema() throws IOException {
        Path schemaPath = Paths.get("src", "test", "resources", "validator", "boilerplate", "c2c-schema.json");
        Schema rootSchema = Schema.from(schemaPath);
        BoilerplateGenerator generator = new BoilerplateGenerator(rootSchema);
        StringBuilder actual = new StringBuilder();
        for (DocumentMemberDeclarationNode s : generator.getNodes().values()) {
            actual.append(s.toSourceCode());
        }
        Path expectedToml = Paths.get("src", "test", "resources", "validator", "boilerplate", "c2c-schema.toml");
        String expected = Files.readString(expectedToml);
        Assert.assertEquals(actual.toString(), expected);
    }

    @Test
    public void testBasicSchema() throws IOException {
        Path schemaPath = Paths.get("src", "test", "resources", "validator", "boilerplate", "basic-schema.json");
        Schema rootSchema = Schema.from(schemaPath);
        BoilerplateGenerator generator = new BoilerplateGenerator(rootSchema);
        StringBuilder actual = new StringBuilder();
        for (DocumentMemberDeclarationNode s : generator.getNodes().values()) {
            actual.append(s.toSourceCode());
        }
        Path expectedToml = Paths.get("src", "test", "resources", "validator", "boilerplate", "basic-schema.toml");
        String expected = Files.readString(expectedToml);
        Assert.assertEquals(actual.toString(), expected);
    }
}
