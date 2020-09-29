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

import io.ballerina.toml.Toml;
import io.ballerina.toml.ast.TomlNode;
import io.ballerina.toml.ast.TomlTransformer;
import io.ballerina.toml.internal.parser.ParserFactory;
import io.ballerina.toml.internal.parser.TomlParser;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.syntax.tree.ModulePartNode;
import io.ballerina.toml.syntax.tree.Node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test the parser.
 */
public class TestToml {

    private static final PrintStream OUT = System.out;

    public static void main(String[] args) throws IOException {
        String path = "src/test/resources/basic-toml.toml";

        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
//        testParser(content);
        testAPI(path);
    }

    private static void testParser(String content) {
        long sTime = System.currentTimeMillis();
        TomlParser parser = ParserFactory.getParser(content);
        STNode node = parser.parse();
        OUT.println("__________________________________________________");
        OUT.println(node);
        OUT.println("__________________________________________________");
        OUT.println("Time: " + (System.currentTimeMillis() - sTime) / 1000.0);
        Node externalNode = node.createUnlinkedFacade();
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlNode transform = nodeTransformer.transform((ModulePartNode) externalNode);
        OUT.println(transform);
    }

    private static void testAPI(String path) throws IOException {
        Toml toml = new Toml();
        InputStream inputStream = new FileInputStream(path);
        Toml read = toml.read(inputStream);
        Boolean key1 = read.getBoolean("key1");
        OUT.println(key1);

        Boolean key2 = read.getBoolean("key2");
        OUT.println(key2);
    }

}
