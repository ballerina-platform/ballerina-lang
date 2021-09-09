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
 * Statement code snippet generator.
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class StatementGenerator extends CodeSnippetGenerator {

    @Override
    public String generate() {
        return getRandomStatement();
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.STATEMENT;
    }

    public String getRandomStatement() {
        switch (random.nextInt(2)) {
            case 0:
                return Generators.generate(Generators.Type.MATCH_STATEMENT);
            case 1:
            default:
                return Generators.generate(Generators.Type.VARIABLE_DECLARATION_STATEMENT);
        }
    }

}
