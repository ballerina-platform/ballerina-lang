/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.env;

import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Represents the per build project environment context.
 *
 * @since 2.0.0
 */
public class BuildProjectEnvContext extends ProjectEnvironmentContext {

    private Map<Class<?>, Object> services = new HashMap<>();

    public static BuildProjectEnvContext from(Project project) {
        return new BuildProjectEnvContext(project);
    }

    private BuildProjectEnvContext(Project project) {
        services.put(PackageResolver.class, new CustomPackageResolver(project));
        populateCompilerContext();
    }

    private void populateCompilerContext() {
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(PROJECT_DIR, "../../langlib/lang.annotations/src/main/ballerina");
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        // This is a temporary workaround. This can be removed once we migrate all the old projects to the new model
        SourceDirectoryManager.getInstance(compilerContext);
        services.put(CompilerContext.class, compilerContext);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) services.get(clazz);
    }
}
