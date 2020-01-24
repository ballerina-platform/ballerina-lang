/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.coverage.ExecutionCoverageBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {
    private boolean generateCoverage;

    public RunTestsTask(boolean generateCoverage) {
        this.generateCoverage = generateCoverage;
    }

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        Path targetDirPath = buildContext.get(BuildContextField.TARGET_DIR);

        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (BLangPackage bLangPackage : moduleBirMap) {
            PackageID packageID = bLangPackage.packageID;

            if (!buildContext.moduleDependencyPathMap.containsKey(packageID)) {
                continue;
            }

            // todo following is some legacy logic check if we need to do this.
            // if (bLangPackage.containsTestablePkg()) {
            // } else {
            // In this package there are no tests to be executed. But we need to say to the users that
            // there are no tests found in the package to be executed as :
            // Running tests
            //     <org-name>/<package-name>:<version>
            //         No tests found
            // }
            Path jarPath = buildContext.getTestJarPathFromTargetCache(packageID);
            Path modulejarPath = buildContext.getJarPathFromTargetCache(packageID);
            Path jarFileName = modulejarPath.getFileName();
            String moduleJarName = jarFileName != null ? jarFileName.toString() : "";
            // subsitute test jar if module jar if tests not exists
            if (Files.notExists(jarPath)) {
                jarPath = modulejarPath;
            }

            if (this.generateCoverage) {
                String orgName = bLangPackage.packageID.getOrgName().toString();
                String packageName = bLangPackage.packageID.getName().toString();
                generateCoverageReportForTestRun(moduleJarName, jarPath, sourceRootPath, targetDirPath,
                        orgName, packageName, buildContext);
            } else {
                readDataFromJsonAndMockTheTestSuit(moduleJarName, targetDirPath, jarPath, buildContext);
            }
        }
    }

    private void generateCoverageReportForTestRun(String moduleJarName, Path testJarPath, Path sourceRootPath,
                                                  Path targetDirPath, String orgName, String packageName,
                                                  BuildContext buildContext) {
        ExecutionCoverageBuilder coverageBuilder = new ExecutionCoverageBuilder(sourceRootPath, targetDirPath,
                testJarPath, orgName, moduleJarName, packageName);
        boolean execFileGenerated = coverageBuilder.generateExecFile();
        buildContext.out().println("\nGenerating the coverage report");
        if (execFileGenerated) {
            buildContext.out().println("\tballerina.exec is generated");
            // unzip the compiled source
            coverageBuilder.unzipCompiledSource();
            // copy the content as described with package naming
            buildContext.out().println("\tCreating source file directory");
            coverageBuilder.createSourceFileDirectory();
            // generate the coverage report
            coverageBuilder.generateCoverageReport();
            buildContext.out().println("\nReport is generated. visit target/coverage to see the report.");
        } else {
            buildContext.out().println("Couldn't create the ballerina.exec file");
        }
    }

    private void readDataFromJsonAndMockTheTestSuit(String moduleJarName, Path targetPath, Path testJarPath,
                                                    BuildContext buildContext) {
        Path jsonPath = targetPath.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.JSON_CACHE_DIR_NAME).resolve(moduleJarName);
        Path balDependencyPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB);
        String javaCommand = System.getProperty("java.command");
        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;
        // TODO: remove the Djava.ext.dirs
        String runningCommand = javaCommand + " -Djava.ext.dirs=" + balDependencyPath.toString() + " -cp "
                + testJarPath.toString() + " " + mainClassName + " " + jsonPath.toString();
        try {
            Process proc = Runtime.getRuntime().exec(runningCommand);
            proc.waitFor();

            // Then retrieve the process output
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            int outputStreamLength;

            byte[] b = new byte[in.available()];
            outputStreamLength = in.read(b, 0, b.length);
            if (outputStreamLength > 0) {
                buildContext.out().println(new String(b, StandardCharsets.UTF_8));
            }

            byte[] c = new byte[err.available()];
            outputStreamLength = err.read(c, 0, c.length);
            if (outputStreamLength > 0) {
                buildContext.out().println(new String(c, StandardCharsets.UTF_8));
            }
        } catch (IOException | InterruptedException e) {
            buildContext.err().println(e);
        }
    }
}
