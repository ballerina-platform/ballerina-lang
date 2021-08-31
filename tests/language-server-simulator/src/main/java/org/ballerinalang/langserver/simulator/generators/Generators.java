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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class Generators {

    private static Generators instance;
    private final Map<Type, CodeSnippetGenerator> generators;

    private Generators() {
        generators = new HashMap<>();
        ServiceLoader.load(CodeSnippetGenerator.class)
                .forEach(generator -> generators.put(generator.type(), generator));
    }

    public static String generate(Type type) {
        if (instance == null) {
            instance = new Generators();
        }

        if (instance.generators.containsKey(type)) {
            return instance.generators.get(type).generate();
        }

        return "";
    }

    public static <T> T getGenerator(Type type) {
        return (T) instance.generators.get(type);
    }

    public enum Type {
        FUNCTION(true),
        CLASS(true),
        SERVICE(true),
        TYPE_DEFINITION(true),
        STATEMENT(false),
        MATCH_STATEMENT(false),
        VARIABLE_DECLARATION_STATEMENT(true);

        private boolean topLevelNode;

        Type(boolean topLevelNode) {
            this.topLevelNode = topLevelNode;
        }

        public boolean isTopLevelNode() {
            return topLevelNode;
        }
    }
}
