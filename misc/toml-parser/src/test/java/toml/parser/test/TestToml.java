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

package toml.parser.test;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Test the parser.
 */
public class TestToml {

    private static final PrintStream OUT = System.out;

    private TestToml() {
    }

    public static void main(String[] args) throws IOException {
        String path = "src/test/resources/basic-toml.toml";
        testAPI(path);
    }

    private static void testAPI(String path) throws IOException {
        InputStream inputStream = new FileInputStream(path);
        Toml read = Toml.read(inputStream);

        List<Diagnostic> diagnostics = read.diagnostics();
        for (Diagnostic diagnostic: diagnostics) {
            OUT.println(diagnostic.location().lineRange());
            OUT.println(diagnostic.message());
        }

        TomlStringValueNode key1 = (TomlStringValueNode) read.get("key").get();
        OUT.println(key1.getValue());
    }
}
