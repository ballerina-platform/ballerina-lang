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
package io.ballerinalang.compiler.parser.test;

import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.tree.STNode;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test the parser.
 */
public final class TestParser {

    private static final PrintStream OUT = System.out;

    private TestParser() {
    }

    public static void main(String[] args) throws IOException {
        String path = TestParser.class.getClassLoader().getResource("test_parser.bal").getPath();
        String content = new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
        testParser(content);
    }

    private static void testParser(String content) {
        long sTime = System.currentTimeMillis();
        BallerinaParser parser = ParserFactory.getParser(content);
        STNode node = parser.parse();
        OUT.println("__________________________________________________");
        OUT.println(node);
        OUT.println("__________________________________________________");
        OUT.println("Time: " + (System.currentTimeMillis() - sTime) / 1000.0);
    }
}
