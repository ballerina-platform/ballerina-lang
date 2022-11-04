/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.util;

import io.ballerina.projects.CompilationOptions;
import io.ballerina.projects.IDLClientGeneratorResult;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.environment.ResolutionOptions;

import java.nio.file.Files;

/**
 * Project dependencies related util methods.
 *
 * @since 2.0.0
 */
public class DependencyUtils {

    private DependencyUtils() {

    }

    /**
     * Pull missing dependencies from central.
     *
     * @param project project
     */
    public static void pullMissingDependencies(Project project) {
        CompilationOptions.CompilationOptionsBuilder compilationOptionsBuilder = CompilationOptions.builder();
        compilationOptionsBuilder.setOffline(false).setSticky(false);
        project.currentPackage().getResolution(compilationOptionsBuilder.build());
    }

    /**
     * Generate missing IDL client modules.
     *
     * @param project project
     * @return IDLClientGeneratorResult idl generation result containing diagnostics and the updated package
     */
    public static IDLClientGeneratorResult generateIDLClientModules(Project project) {
        CompilationOptions.CompilationOptionsBuilder compilationOptionsBuilder = CompilationOptions.builder();
        compilationOptionsBuilder.setOffline(false);
        return project.currentPackage().runIDLGeneratorPlugins(ResolutionOptions.builder().setOffline(false).build());
    }

    /**
     * Check if the module is a generated module.
     * This is temporary API gievn till the public API is finalized.
     *
     * @param module module instance
     * @return whether the module is a generated one or not
     */
    public static boolean isGeneratedModule(Module module) {
        if (!module.project().kind().equals(ProjectKind.BUILD_PROJECT)) {
            return false;
        }
        if (module.isDefaultModule()) {
            return false;
        }
        return Files.exists(module.project().sourceRoot().resolve(ProjectConstants.GENERATED_MODULES_ROOT)
                .resolve(module.moduleName().moduleNamePart()));
    }
}
