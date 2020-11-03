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
package io.ballerina.projects.environment;

import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_API_INITIATED_COMPILATION;

/**
 * This class allows API users to build a custom {@code Environment}.
 *
 * @since 2.0.0
 */
public class EnvironmentBuilder {

    public static Environment buildDefault() {
        DefaultEnvironment environment = new DefaultEnvironment();

        // Creating a Ballerina distribution instance
        BallerinaDistribution ballerinaDistribution = new BallerinaDistribution(environment);
        PackageRepository packageRepository = ballerinaDistribution.packageRepository();
        environment.addService(PackageRepository.class, packageRepository);

        GlobalPackageCache packageCache = new GlobalPackageCache();
        environment.addService(GlobalPackageCache.class, packageCache);

        CompilerContext compilerContext = populateCompilerContext();
        environment.addService(CompilerContext.class, compilerContext);
        ballerinaDistribution.loadLangLibPackages(compilerContext, packageCache);
        return environment;
    }

    private static CompilerContext populateCompilerContext() {
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        // TODO Remove the following line, once we fully migrate the old project structures
        options.put(PROJECT_API_INITIATED_COMPILATION, Boolean.toString(true));
        return compilerContext;
    }
}
