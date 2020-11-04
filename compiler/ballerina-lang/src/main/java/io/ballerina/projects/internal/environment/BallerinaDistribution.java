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
package io.ballerina.projects.internal.environment;

import io.ballerina.projects.Bootstrap;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.GlobalPackageCache;
import io.ballerina.projects.repos.BallerinaDistributionRepository;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the Ballerina distribution and responsible for initializing the {@code CompilerContext}, distribution
 * repository, lang lib loading, etc.
 *
 * @since 2.0.0
 */
public final class BallerinaDistribution {
    private static final String BALLERINA_HOME_KEY = "ballerina.home";

    private final Path distributionPath;
    private final BallerinaDistributionRepository distributionRepository;

    public BallerinaDistribution(Environment environment) {
        this(environment, System.getProperty(BALLERINA_HOME_KEY));
    }

    public BallerinaDistribution(Environment environment, String ballerinaDistributionPath) {
        if (ballerinaDistributionPath == null) {
            throw new IllegalStateException("ballerina.home property is not set");
        }

        // TODO Validate whether the given path is a valid Ballerina distribution home
        this.distributionPath = Paths.get(ballerinaDistributionPath);
        this.distributionRepository = new BallerinaDistributionRepository(environment, distributionPath);

        // TODO init compilationContext
    }

    public BallerinaDistributionRepository packageRepository() {
        return distributionRepository;
    }

    public void loadLangLibPackages(CompilerContext compilerContext, GlobalPackageCache packageCache) {
        String bootstrapLangLibName = System.getProperty("BOOTSTRAP_LANG_LIB");
        if (bootstrapLangLibName == null) {
            Bootstrap bootstrap = new Bootstrap(new LangLibResolver(distributionRepository, packageCache));
            bootstrap.loadLangLibSymbols(compilerContext);
        }
    }
}
