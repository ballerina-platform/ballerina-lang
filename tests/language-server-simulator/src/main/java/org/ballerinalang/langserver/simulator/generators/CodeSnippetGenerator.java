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

import java.util.List;
import java.util.Random;

/**
 * Abstract implementation of code snippet generator for the LS simulator.
 *
 * @since 2.0.0
 */
public abstract class CodeSnippetGenerator {

    protected final List<String> primitiveTypes = List.of("string", "int", "float", "decimal", "boolean");
    protected Random random = new Random();

    public abstract String generate();

    public abstract Generators.Type type();
}
