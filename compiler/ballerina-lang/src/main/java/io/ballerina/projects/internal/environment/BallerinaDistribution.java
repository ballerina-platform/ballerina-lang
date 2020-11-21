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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.repos.BallerinaDistributionRepository;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Files;
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
    private static final String langLibBootstrapPhase = System.getProperty("BOOTSTRAP_LANG_LIB");

    private final Path ballerinaHomeDirPath;
    private final BallerinaDistributionRepository distributionRepository;

    private BallerinaDistribution(Environment environment, Path ballerinaHomeDirPath) {
        this.ballerinaHomeDirPath = ballerinaHomeDirPath;
        this.distributionRepository = BallerinaDistributionRepository.from(environment, this.ballerinaHomeDirPath);
    }

    public static BallerinaDistribution from(Environment environment) {
        String ballerinaHome = System.getProperty(BALLERINA_HOME_KEY);
        if (ballerinaHome == null) {
            throw new IllegalStateException("ballerina.home property is not set");
        }
        return from(environment, Paths.get(ballerinaHome));
    }

    public static BallerinaDistribution from(Environment environment, Path ballerinaHomeDirPath) {
        validateBallerinaHomeDir(ballerinaHomeDirPath);
        return new BallerinaDistribution(environment, ballerinaHomeDirPath);
    }

    public BallerinaDistributionRepository packageRepository() {
        return distributionRepository;
    }

    public void loadLangLibPackages(CompilerContext compilerContext, PackageCache packageCache) {
        if (langLibBootstrapPhase == null) {
            Bootstrap bootstrap = new Bootstrap(new LangLibResolver(distributionRepository, packageCache));
            bootstrap.loadLangLibSymbols(compilerContext);
        }
    }

    private static void validateBallerinaHomeDir(Path ballerinaHomeDirPath) {
        if (Files.notExists(ballerinaHomeDirPath)) {
            throw new ProjectException("Ballerina distribution directory does not exists in `" +
                    ballerinaHomeDirPath + "'");
        }

        if (!Files.isDirectory(ballerinaHomeDirPath)) {
            throw new ProjectException("Invalid Ballerina distribution directory: " + ballerinaHomeDirPath);
        }

        if (langLibBootstrapPhase != null) {
            return;
        }

        // TODO Validate whether the runtime jar exists this validation
        // TODO At the moment runtime jar is loaded from the class path during the Gradle build. It is wrong!
    }
}
