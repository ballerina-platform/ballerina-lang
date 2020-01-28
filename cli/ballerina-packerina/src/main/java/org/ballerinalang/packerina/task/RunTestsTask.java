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
import org.wso2.ballerinalang.util.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {
    private boolean generateCoverage;
    private String[] programArgs;

    public RunTestsTask(boolean generateCoverage, String[] programArgs) {
        this.generateCoverage = generateCoverage;
        this.programArgs = programArgs.clone();
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
            Path testJarPath = buildContext.getTestJarPathFromTargetCache(packageID);
            Path testModuleJarPath = buildContext.getJarPathFromTargetCache(packageID);
            Path testModuleJarFile = testModuleJarPath.getFileName();
            String testModuleJarFileName = testModuleJarFile != null ? testModuleJarFile.toString() : "";
            // subsitute test jar if module jar if tests not exists
            if (Files.notExists(testJarPath)) {
                testJarPath = testModuleJarPath;
            }

            if (this.generateCoverage) {
                String orgName = bLangPackage.packageID.getOrgName().toString();
                String packageName = bLangPackage.packageID.getName().toString();
                generateCoverageReportForTestRun(testModuleJarFileName, testJarPath, sourceRootPath, targetDirPath,
                        orgName, packageName, buildContext);
            } else {
                readDataFromJsonAndLaunchTestSuit(testModuleJarFileName, targetDirPath, testJarPath, buildContext);
            }
        }
    }

    /**
     * Generate the coverage report from a test run.
     *
     * @param moduleJarName file name to search for a directory
     * @param testJarPath path to the this testable jar file
     * @param sourceRootPath path of the source root.
     * @param targetDirPath path of the target directory
     * @param orgName organization name derived from the bLangPackage
     * @param packageName package name derived from the bLangPackage
     * @param buildContext buildContext to show some basic outputs
     */
    private void generateCoverageReportForTestRun(String moduleJarName, Path testJarPath, Path sourceRootPath,
                                                  Path targetDirPath, String orgName, String packageName,
                                                  BuildContext buildContext) {
        ExecutionCoverageBuilder coverageBuilder = new ExecutionCoverageBuilder(sourceRootPath, targetDirPath,
                testJarPath, this.programArgs, orgName, moduleJarName, packageName);
        boolean execFileGenerated = coverageBuilder.generateExecFile();
        buildContext.out().println("\nGenerating the coverage report");
        if (execFileGenerated) {
            // unzip the compiled source
            coverageBuilder.unzipCompiledSource();
            // copy the content as described with package naming
            coverageBuilder.createSourceFileDirectory();
            // generate the coverage report
            coverageBuilder.generateCoverageReport();
            buildContext.out().println("\ttarget/coverage/" + moduleJarName);
        } else {
            buildContext.out().println("Couldn't create the Coverage. Please try again.");
        }
    }

    /**
     * Run the tests by reading and passing data from a json file.
     *  @param moduleJarName directory to find the json file
     * @param targetPath target path to resolve the json cache directory
     * @param testJarPath path of the thin testable jar
     * @param buildContext build context to show some basic outputs
     */
    private void readDataFromJsonAndLaunchTestSuit(String moduleJarName, Path targetPath, Path testJarPath
            , BuildContext buildContext) {
        Path jsonCachePath = targetPath.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.JSON_CACHE_DIR_NAME).resolve(moduleJarName);
        Path balDependencyPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB);
        String javaCommand = System.getProperty("java.command");
        String filePathSeparator = System.getProperty("file.separator");
        String classPathSeparator = System.getProperty("path.separator");
        String launcherClassName = TesterinaConstants.TESTERINA_EXECUTOR_CLASS_NAME;
        String classPaths = balDependencyPath.toString() + filePathSeparator + "*"
                + classPathSeparator + testJarPath.toString();

        // building the java command
        List<String> commands = new ArrayList<>();
        commands.add(javaCommand); // java command that is used by ballerina
        commands.add("-cp"); // terminal option to set the classpath
        commands.add(classPaths); // set the class paths including testable thin jat
        commands.add(launcherClassName); // launcher main class name
        if (this.programArgs != null) {
            commands.addAll(Lists.of(this.programArgs));
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.environment().put("testerina.tesetsuite.path", jsonCachePath.toString());
            Process proc = processBuilder.start();
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
