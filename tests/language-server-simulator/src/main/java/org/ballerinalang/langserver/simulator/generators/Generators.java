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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory to access {@link CodeSnippetGenerator}s.
 *
 * @since 2.0.0
 */
public class Generators {

    private static final Logger logger = LoggerFactory.getLogger(Generators.class);

    private static final String PROP_SKIPPED_GENERATORS = "ls.simulation.skipGenerators";
    private static final Generators instance = new Generators();
    private final Map<Type, CodeSnippetGenerator> generators;

    private Generators() {
        // Get skipped generators
        String property = System.getProperty(PROP_SKIPPED_GENERATORS, "");
        Set<Type> skippedGenerators = Stream.of(property.split(","))
                .map(String::trim)
                .map(Type::valueOf)
                .collect(Collectors.toSet());
        logger.info("Skipping generators of type: " + skippedGenerators);

        // Load generators
        generators = new HashMap<>();
        ServiceLoader.load(CodeSnippetGenerator.class)
                .forEach(generator -> {
                    if (!skippedGenerators.contains(generator.type())) {
                        generators.put(generator.type(), generator);
                    }
                });
    }

    /**
     * Generate a code snippet of provided type.
     *
     * @param type Type of the required code snippet.
     * @return Generated code snippet.
     */
    public static String generate(Type type) {
        if (getInstance().generators.containsKey(type)) {
            return instance.generators.get(type).generate();
        }

        return "";
    }

    public static <T> T getGenerator(Type type) {
        return (T) instance.generators.get(type);
    }

    public static Generators getInstance() {
        return instance;
    }

    /**
     * Different types of code snippets which can be generated.
     */
    public enum Type {
        FUNCTION(true),
        CLASS(true),
        SERVICE(true),
        TYPE_DEFINITION(true),
        STATEMENT(false),
        MATCH_STATEMENT(false),
        VARIABLE_DECLARATION_STATEMENT(true),

        IMPORT_STATEMENT(true);

        private final boolean topLevelNode;

        Type(boolean topLevelNode) {
            this.topLevelNode = topLevelNode;
        }

        public boolean isTopLevelNode() {
            return topLevelNode;
        }
    }
}
