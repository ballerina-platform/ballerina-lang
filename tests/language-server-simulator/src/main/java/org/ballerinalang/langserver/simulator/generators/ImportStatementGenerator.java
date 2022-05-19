/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.simulator.EditorSimulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Import statement snippet generator.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class ImportStatementGenerator extends CodeSnippetGenerator {

    private static final String PACKAGE_NAME = "nballerina";

    @Override
    public String generate() {
        //Look for modules in the source and generate import statements for them.
        String projectPath = System.getProperty(EditorSimulator.PROP_SOURCE_DIR);
        if (projectPath == null) {
            return "";
        }
        Path path = Paths.get(projectPath);
        Path modulesPath = path.resolve("modules");
        if (Files.exists(modulesPath)) {
            try {
                List<String> imports = Files.list(modulesPath)
                        .filter(Files::isDirectory)
                        .map(p -> "import " + PACKAGE_NAME + "." + p.getFileName() + ";")
                        .collect(Collectors.toList());
                if (!imports.isEmpty()) {
                    return imports.get(random.nextInt(imports.size()));
                }
            } catch (IOException e) {
                //ignore
            }
        }
        return "";
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.IMPORT_STATEMENT;
    }
}
