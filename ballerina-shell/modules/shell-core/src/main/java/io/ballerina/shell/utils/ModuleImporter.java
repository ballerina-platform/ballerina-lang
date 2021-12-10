/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.utils;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import io.ballerina.shell.Diagnostic;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Support class to add required import statements.
 *
 * @since 2.0.0
 */
public class ModuleImporter {

    private final List<Package> packageList;
    private static final String UNDEFINED_MODULE = "undefined module";
    private static final String LANG = "lang";
    private static final List<String> SKIPPED_LIBS = Arrays.asList("lang.annotations", "lang.__internal", "lang.query");

    public ModuleImporter() {
        packageList = getPackagesFromDistRepo();
    }

    /**
     * Check module availability in the distribution.
     *
     * @param module module name.
     * @return availability of the module.
     */
    public boolean isModuleInDistRepo(String module) {
        String langModule = LANG + "." + module;
        for (Package pkg : packageList) {
            if (module.equals(pkg.packageName().value()) || langModule.equals(pkg.packageName().value())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns import statement.
     *
     * @param module input module.
     * @return import statement.
     */
    public String getImportStatement(String module) {
        String importStatement = "import ";
        String langModule = LANG + "." + module;
        String finalStatement = null;
        for (Package pkg : packageList) {
            if (pkg.packageName().value().equals(module)) {
                finalStatement = importStatement + pkg.packageOrg().toString() + '/' + pkg.packageName().value();
                break;
            }
            if (pkg.packageName().value().equals(langModule)) {
                finalStatement = importStatement + pkg.packageOrg().toString() + '/' + LANG + "." + module;
                break;
            }
        }

        return finalStatement;
    }

    /**
     * Return found undefined modules.
     *
     * @param diagnostics collection of evaluator diagnostics.
     * @return distinct list of module errors.
     */
    public List<String> undefinedModules(Collection<Diagnostic> diagnostics) {
        List<String> moduleErrors = new ArrayList<>();
        for (Diagnostic diagnostic : diagnostics) {
            //TODO update following statement using error code (BCE2000) issue : #33990
            if (diagnostic.toString().contains(UNDEFINED_MODULE)) {
                moduleErrors.add(diagnostic.toString().split("\n")[0].split(" ")[3]);
            }
        }

        return moduleErrors.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Get packages available in the distributions.
     */
    private List<Package> getPackagesFromDistRepo() {
        DefaultEnvironment environment = new DefaultEnvironment();
        // Creating a Ballerina distribution instance
        BallerinaDistribution ballerinaDistribution = BallerinaDistribution.from(environment);
        PackageRepository packageRepository = ballerinaDistribution.packageRepository();
        Map<String, List<String>> pkgMap = packageRepository.getPackages();

        List<io.ballerina.projects.Package> packages = new ArrayList<>();
        pkgMap.forEach((key, value) -> {
            if (key.equals(Names.BALLERINA_INTERNAL_ORG.getValue())) {
                return;
            }
            value.forEach(nameEntry -> {
                String[] components = nameEntry.split(":");
                if (components.length != 2 || SKIPPED_LIBS.contains(components[0])) {
                    return;
                }
                String nameComponent = components[0];
                String version = components[1];
                PackageOrg packageOrg = PackageOrg.from(key);
                PackageName packageName = PackageName.from(nameComponent);
                PackageVersion pkgVersion = PackageVersion.from(version);
                PackageDescriptor pkdDesc = PackageDescriptor.from(packageOrg, packageName, pkgVersion);
                ResolutionRequest request = ResolutionRequest.from(pkdDesc, PackageDependencyScope.DEFAULT);

                Optional<Package> repoPackage = packageRepository.getPackage(request,
                        ResolutionOptions.builder().setOffline(true).build());
                repoPackage.ifPresent(packages::add);
            });
        });

        return Collections.unmodifiableList(packages);
    }
}
