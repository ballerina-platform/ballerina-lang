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

/**
 * Function code snippet generator.
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class FunctionGenerator extends CodeSnippetGenerator {

    @Override
    public String generate() {
        return generateRandomFunction();
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.FUNCTION;
    }

    public String generateRandomFunction() {
        String name = "fn";
        String returnType = "string";
        return generateRandomFunction(name, returnType);
    }

    public String generateRandomFunction(String name, String returnType) {
        return "\npublic function " + name + "() returns " + returnType + " {\n" +
                "    " + getRandomFunctionBody(returnType) + "\n" +
                "}\n";
    }

    public String getRandomFunctionBody(String returnType) {
        StatementGenerator statementGenerator = Generators.getGenerator(Generators.Type.STATEMENT);
        String body = "";
        body += "\t" + statementGenerator.getRandomStatement();
        body += "\t" + statementGenerator.getRandomStatement();
        body += "\t" + statementGenerator.getRandomStatement();
        body += "\treturn " + returnType + ";";
        return body;
    }
}
