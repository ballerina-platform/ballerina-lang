/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.CompilationOptionsBuilder;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.ProjectEnvironment;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

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
        // Disable offline flag
        CompilationOptionsBuilder compilationOptionsBuilder = new CompilationOptionsBuilder();
        compilationOptionsBuilder.offline(false);

        PackageResolution resolution = project.currentPackage().getResolution(compilationOptionsBuilder.build());
        ProjectEnvironment projectEnvContext = project.projectEnvironmentContext();
        PackageCache packageCache = projectEnvContext.getService(PackageCache.class);

        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }

        for (ResolvedPackageDependency dependency : resolution.allDependencies()) {
            if (packageCache.getPackage(dependency.packageId()).isEmpty()) {
                Path packagePathInBalaCache = ProjectUtils.createAndGetHomeReposPath()
                        .resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME)
                        .resolve(dependency.packageInstance().packageOrg().value())
                        .resolve(dependency.packageInstance().packageName().value());
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        initializeProxy(settings.getProxy()), getAccessTokenOfCLI(settings));
                for (String supportedPlatform : SUPPORTED_PLATFORMS) {
                    try {
                        client.pullPackage(dependency.packageInstance().packageOrg().value(),
                                dependency.packageInstance().packageName().value(),
                                dependency.packageInstance().packageVersion().toString(),
                                packagePathInBalaCache, supportedPlatform, RepoUtils.getBallerinaVersion(), false);
                    } catch (CentralClientException e) {
                        // ignore when get package fail
                    }
                }
            }
        }
    }
}
