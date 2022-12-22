/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.architecturemodelgenerator.generators;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Module;

import java.nio.file.Path;

/**
 * Model Generator Abstract Class.
 *
 * @since 2201.4.0
 */
public abstract class ModelGenerator {
    private final SemanticModel semanticModel;
    private final Module module;
    private final Path moduleRootPath;

    public ModelGenerator(SemanticModel semanticModel, Module module) {

        this.semanticModel = semanticModel;
        this.module = module;
        Path moduleRootPath = module.project().sourceRoot().toAbsolutePath();
        if (module.moduleName().moduleNamePart() != null) {
            moduleRootPath = moduleRootPath.resolve(module.moduleName().moduleNamePart());
        }
        this.moduleRootPath = moduleRootPath;
    }

    public SemanticModel getSemanticModel() {
        return this.semanticModel;
    }

    public Module getModule() {
        return this.module;
    }

    public Path getModuleRootPath() {
        return moduleRootPath;
    }
}
