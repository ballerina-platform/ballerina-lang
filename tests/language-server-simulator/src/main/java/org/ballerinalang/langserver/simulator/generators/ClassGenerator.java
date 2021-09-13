/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.simulator.generators;

import org.ballerinalang.annotation.JavaSPIService;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class code snippet generator.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class ClassGenerator extends CodeSnippetGenerator {

    @Override
    public String generate() {
        return generateRandomClass();
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.CLASS;
    }

    private String generateRandomClass() {
        FunctionGenerator functionGenerator = Generators.getGenerator(Generators.Type.FUNCTION);

        int numOfFunctions = 1 + random.nextInt(100);
        String body = IntStream.range(0, numOfFunctions)
                .mapToObj(i -> functionGenerator.generateRandomFunction("fn" + i, "string"))
                .collect(Collectors.joining("\n"));
        return "class AClass {\n" +
                "    " + body + "\n" +
                "}";
    }
}
