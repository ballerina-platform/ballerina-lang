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
import io.ballerina.projects.environment.EnvironmentContext;
import io.ballerina.projects.environment.GlobalPackageCache;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.projects.repos.DistributionPackageCache;
import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Represents the {@code EnvironmentContext} of the build project.
 *
 * @since 2.0.0
 */
public class BuildEnvContext extends EnvironmentContext {
    private static final BuildEnvContext instance = new BuildEnvContext();
    private Map<Class<?>, Object> services = new HashMap<>();

    CompilerContext compilerContext;

    public static BuildEnvContext getInstance() {
        return instance;
    }

    private BuildEnvContext() {
        populateCompilerContext();
        initGlobalPackageCache();
        initDistPackageCache();
    }

    private void initDistPackageCache() {
        services.put(DistributionPackageCache.class, new DistributionPackageCache());
    }

    public ProjectEnvironmentContext projectEnvironmentContext(Project project) {
        return BuildProjectEnvContext.from(project, this);
    }

    @Override
    public CompilerContext compilerContext() {
        return compilerContext;
    }

    private void populateCompilerContext() {
        compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(PROJECT_DIR, "../../langlib/lang.annotations/src/main/ballerina");
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());

        // TODO This is a temporary property to compile lang lib modules from source
        System.setProperty("BALLERINA_DEV_COMPILE_BALLERINA_ORG", "true");

        // This is a temporary workaround. This can be removed once we migrate all the old projects to the new model
        SourceDirectoryManager.getInstance(compilerContext);
    }

    private void initGlobalPackageCache() {
        services.put(GlobalPackageCache.class, new GlobalPackageCache());
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) services.get(clazz);
    }

}
