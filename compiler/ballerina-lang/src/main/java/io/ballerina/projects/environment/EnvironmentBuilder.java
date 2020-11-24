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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import io.ballerina.projects.internal.environment.DefaultPackageResolver;
import io.ballerina.projects.internal.environment.EnvironmentPackageCache;
import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_API_INITIATED_COMPILATION;

/**
 * This class allows API users to build a custom {@code Environment}.
 *
 * @since 2.0.0
 */
public class EnvironmentBuilder {

    private PackageRepository ballerinaCentralRepo;
    private Path ballerinaHome;

    public static EnvironmentBuilder getBuilder() {
        return new EnvironmentBuilder();
    }

    public static Environment buildDefault() {
        return new EnvironmentBuilder().build();
    }

    public EnvironmentBuilder setBallerinaHome(Path ballerinaHome) {
        this.ballerinaHome = ballerinaHome;
        return this;
    }

    public EnvironmentBuilder setBallerinaCentralRepository(PackageRepository ballerinaCentralRepo) {
        this.ballerinaCentralRepo = ballerinaCentralRepo;
        return this;
    }

    public Environment build() {
        DefaultEnvironment environment = new DefaultEnvironment();

        // Environment creation logic needs to validate following things
        //    BallerinaDistribution
        //    BallerinaHomeCache--> create if not exists get the .ballerina
        //      centralRepo = BallerinaHomeCache.getCentralRepository()

        // Creating a Ballerina distribution instance
        BallerinaDistribution ballerinaDistribution = getBallerinaDistribution(environment);
        PackageRepository distributionRepository = ballerinaDistribution.packageRepository();
        environment.addService(PackageRepository.class, distributionRepository);

        PackageCache packageCache = new EnvironmentPackageCache();
        environment.addService(PackageCache.class, packageCache);

        if (ballerinaCentralRepo == null) {
            // TODO Creating a dummy impl for now
            ballerinaCentralRepo = new DummyPackageRepository();
        }

        PackageResolver packageResolver = new DefaultPackageResolver(distributionRepository,
                ballerinaCentralRepo, packageCache);
        environment.addService(PackageResolver.class, packageResolver);

        CompilerContext compilerContext = populateCompilerContext();
        environment.addService(CompilerContext.class, compilerContext);
        ballerinaDistribution.loadLangLibPackages(compilerContext, packageResolver);
        return environment;
    }

    private BallerinaDistribution getBallerinaDistribution(DefaultEnvironment environment) {
        return (ballerinaHome != null) ?
                BallerinaDistribution.from(environment, ballerinaHome) :
                BallerinaDistribution.from(environment);
    }


    private static CompilerContext populateCompilerContext() {
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        // TODO Remove the following line, once we fully migrate the old project structures
        options.put(PROJECT_API_INITIATED_COMPILATION, Boolean.toString(true));
        return compilerContext;
    }

    private static class DummyPackageRepository implements PackageRepository {
        @Override
        public Optional<Package> getPackage(ResolutionRequest resolutionRequest) {
            return Optional.empty();
        }

        @Override
        public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
            return Collections.emptyList();
        }

        @Override
        public Map<String, List<String>> getPackages() {
            return Collections.emptyMap();
        }
    }
}
