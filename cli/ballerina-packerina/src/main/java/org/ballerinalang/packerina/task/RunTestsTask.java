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

import com.google.gson.Gson;
import org.ballerinalang.packerina.OsUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.ballerinalang.test.launcher.entity.TestSuite;
import org.ballerinalang.test.launcher.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TEST_RUNTIME_JAR_PREFIX;

/**
 * Task for executing tests.
 *
 * @since 1.1.0
 */
public class RunTestsTask implements Task {

    private final String[] args;

    public RunTestsTask(String[] args) {
        this.args = args;
    }

    public RunTestsTask(String[] args, List<String> groupList, List<String> disableGroupList) {
        this.args = args;
        TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();
        if (disableGroupList != null) {
            testerinaRegistry.setGroups(disableGroupList);
            testerinaRegistry.setShouldIncludeGroups(false);
        } else if (groupList != null) {
            testerinaRegistry.setGroups(groupList);
            testerinaRegistry.setShouldIncludeGroups(true);
        }
    }

    @Override
    public void execute(BuildContext buildContext) {
        buildContext.out().println();
        buildContext.out().println("Running Tests");
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);

        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (BLangPackage bLangPackage : moduleBirMap) {
            TestSuite suite = TesterinaRegistry.getInstance().getTestSuites().get(bLangPackage.packageID.toString());
            if (suite == null) {
                buildContext.out().println();
                buildContext.out().println("\t" + bLangPackage.packageID);
                buildContext.out().println("\t" + "No tests found");
                buildContext.out().println();
                continue;
            }
            HashSet<Path> testDependencies = getTestDependencies(buildContext, bLangPackage);
            Path jsonPath = buildContext.getTestJsonPathTargetCache(bLangPackage.packageID);
            createTestJson(bLangPackage, suite, sourceRootPath, jsonPath);
            int testResult = runTestSuit(jsonPath, buildContext, testDependencies);
            if (testResult != 0) {
                throw createLauncherException("there are test failures");
            }
        }
    }

    /**
     * Extract data from the given bLangPackage.
     *
     * @param bLangPackage Ballerina package
     * @param sourceRootPath Source root path
     * @param jsonPath Path to the test json
     */
    private static void createTestJson(BLangPackage bLangPackage, TestSuite suite, Path sourceRootPath, Path jsonPath) {
        // set data
        suite.setInitFunctionName(bLangPackage.initFunction.name.value);
        suite.setStartFunctionName(bLangPackage.startFunction.name.value);
        suite.setStopFunctionName(bLangPackage.stopFunction.name.value);
        suite.setPackageName(bLangPackage.packageID.toString());
        suite.setSourceRootPath(sourceRootPath.toString());
        // add module functions
        bLangPackage.functions.forEach(function -> {
            String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                                       bLangPackage.packageID.name.value,
                                                                       getClassName(function.pos.src.cUnitName));
            suite.addTestUtilityFunction(function.name.value, functionClassName);
        });
        // add test functions
        if (bLangPackage.containsTestablePkg()) {
            suite.setTestInitFunctionName(bLangPackage.getTestablePkg().initFunction.name.value);
            suite.setTestStartFunctionName(bLangPackage.getTestablePkg().startFunction.name.value);
            suite.setTestStopFunctionName(bLangPackage.getTestablePkg().stopFunction.name.value);
            bLangPackage.getTestablePkg().functions.forEach(function -> {
                String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                                           bLangPackage.packageID.name.value,
                                                                           getClassName(function.pos.src.cUnitName));
                suite.addTestUtilityFunction(function.name.value, functionClassName);
            });
        } else {
            suite.setSourceFileName(bLangPackage.packageID.sourceFileName.value);
        }
        // write to json
        writeToJson(suite, jsonPath);
    }

    /**
     * return the function name.
     *
     * @param function String value of a function
     * @return function name
     */
    private static String getClassName(String function) {
        return function.replace(".bal", "").replace("/", ".");
    }

    /**
     * Write the content into a json.
     *
     * @param testMetaData Data that are parsed to the json
     */
    private static void writeToJson(TestSuite testMetaData, Path jsonPath) {
        Path tmpJsonPath = Paths.get(jsonPath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(testMetaData);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't read data from the Json file : " + e.toString());
        }
    }

    private int runTestSuit(Path jsonPath, BuildContext buildContext, HashSet<Path> testDependencies) {
        String javaCommand = System.getProperty("java.command");
        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;
        try {
            String classPath = getClassPath(getTestRuntimeJar(buildContext), testDependencies);
            List<String> cmdArgs = Lists.of(javaCommand, "-cp", classPath, mainClassName, jsonPath.toString());
            cmdArgs.addAll(Arrays.asList(args));
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();;
            Process proc = processBuilder.start();

//            // Then retrieve the process output
//            InputStream in = proc.getInputStream();
//            InputStream err = proc.getErrorStream();
//            int outputStreamLength;
//
//            byte[] b = new byte[in.available()];
//            outputStreamLength = in.read(b, 0, b.length);
//            if (outputStreamLength > 0) {
//                buildContext.out().println(new String(b, StandardCharsets.UTF_8));
//            }
//
//            byte[] c = new byte[err.available()];
//            outputStreamLength = err.read(c, 0, c.length);
//            if (outputStreamLength > 0) {
//                buildContext.out().println(new String(c, StandardCharsets.UTF_8));
//            }
           return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("unable to run the tests: " + e.getMessage());
        }
    }

    private HashSet<Path> getTestDependencies(BuildContext buildContext, BLangPackage bLangPackage) {
        Path testJarPath;
        if (bLangPackage.containsTestablePkg()) {
            testJarPath = buildContext.getTestJarPathFromTargetCache(bLangPackage.packageID);
        } else {
            // Single bal file test code will be in module jar
            testJarPath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID);
        }
        ExecutableJar executableJar = buildContext.moduleDependencyPathMap.get(bLangPackage.packageID);
        HashSet<Path> testDependencies = new HashSet<>(executableJar.moduleLibs);
        testDependencies.addAll(executableJar.testLibs);
        testDependencies.add(testJarPath);
        return testDependencies;
    }

    private String getClassPath(Path testRuntimeJar, HashSet<Path> testDependencies) {
        String separator = ":";
        StringBuilder classPath = new StringBuilder();
        classPath.append(testRuntimeJar);
        if (OsUtils.isWindows()) {
            separator = ";";
        }
        for (Path testDependency : testDependencies) {
            classPath.append(separator).append(testDependency);
        }
        return classPath.toString();
    }

    private Path getTestRuntimeJar(BuildContext buildContext) {
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        String runtimeJarName = TEST_RUNTIME_JAR_PREFIX + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }
}
