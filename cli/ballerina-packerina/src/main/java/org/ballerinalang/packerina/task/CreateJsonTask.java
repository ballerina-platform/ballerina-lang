/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.entity.TestJsonData;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Task for create json for test execution.
 */
public class CreateJsonTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        buildContext.out().println();
        buildContext.out().println("Creating json");
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        Path targetPath = buildContext.get(BuildContextField.TARGET_DIR);

        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        moduleBirMap.stream()
                .forEach(bLangPackage -> {

                    Map<BLangPackage, String> programFileMap = new HashMap<>();
                    Path testJarPath = buildContext.getTestJarPathFromTargetCache(bLangPackage.packageID);
                    Path modulejarPath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID).getFileName();
                    // subsitute test jar if module jar if tests not exists
                    if (Files.notExists(testJarPath)) {
                        testJarPath = modulejarPath;
                    }
                    HashSet<Path> moduleDependencies = buildContext.moduleDependencyPathMap
                            .get(bLangPackage.packageID).platformLibs;
                    if (bLangPackage.containsTestablePkg()) {
                        for (BLangTestablePackage testablePackage : bLangPackage.getTestablePkgs()) {
                            // find the set of dependency jar paths for running test for this module and update
                            updateDependencyJarPaths(testablePackage.symbol.imports, buildContext, moduleDependencies);
                        }
                    }
                    String modulejarName = modulejarPath != null ? modulejarPath.toString() : "";
                    programFileMap.put(bLangPackage, modulejarName);
                    Path jsonCachePath = targetPath
                            .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                            .resolve(ProjectDirConstants.JSON_CACHE_DIR_NAME)
                            .resolve(modulejarName);
                    try {
                        Files.createDirectories(jsonCachePath);
                    } catch (Exception e) {
                        throw LauncherUtils.createLauncherException("Couldn't create the directories: " + e.toString());
                    }
                    writeJsonDataFromBLangPackage(programFileMap, sourceRootPath, testJarPath, modulejarName,
                            jsonCachePath, moduleDependencies);
                    buildContext.out().println("\t" + modulejarName + "/" + TesterinaConstants.TESTERINA_TEST_SUITE);
                });
    }

    /**
     * Write the content into a json.
     *
     * @param testMetaData Data that are parsed to the json
     */
    private static void writeDataToJsonFile(TestJsonData testMetaData, Path jsonPath) {
        Path tmpJsonPath = Paths.get(jsonPath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),  StandardCharsets.UTF_8)) {
            Gson gson = new Gson();

            String json = gson.toJson(testMetaData);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException("Couldn't read data from the Json file : " + e.toString());
        }
    }

    /**
     * Extract data from the given bLangPackage.
     *
     * @param programFileMap map of the bLangPackage and TesterinaClassLoader
     */
    private static void writeJsonDataFromBLangPackage(Map<BLangPackage, String> programFileMap,
                                                      Path sourceRootPath, Path jarPath, String moduleJarName,
                                                      Path jsonPath, HashSet<Path> moduleDependencies) {
        programFileMap.forEach((source, jarName) -> {
            String initFunctionName = source.initFunction.name.value;
            String startFunctionName = source.startFunction.name.value;
            String stopFunctionName = source.stopFunction.name.value;
            String testInitFunctionName = source.getTestablePkg().initFunction.name.value;
            String testStartFunctionName = source.getTestablePkg().startFunction.name.value;
            String testStopFunctionName = source.getTestablePkg().stopFunction.name.value;
            String orgName = source.packageID.getOrgName().value;
            String version = source.packageID.getPackageVersion().value;
            String packageName;
            if (source.packageID.getName().getValue().equals(".")) {
                packageName = source.packageID.getName().getValue();
            } else {
                packageName = orgName + "/" + source.packageID.getName().value + ":" + version;
                //packageName = TesterinaUtils.getFullModuleName(source.packageID.getName().getValue());
            }
            String hasTestablePackages = Boolean.toString(source.hasTestablePackage());

            HashMap<String, String> normalFunctionNames = new HashMap<>();
            HashMap<String, String> testFunctionNames = new HashMap<>();

            source.functions.stream().forEach(function -> {
                try {
                    String functionClassName = BFileUtil.getQualifiedClassName(source.packageID.orgName.value,
                            source.packageID.name.value,
                            getClassName(function.pos.src.cUnitName));
                    normalFunctionNames.put(function.name.value, functionClassName);
                } catch (RuntimeException e) {
                    // we do nothing here
                }
            });

            source.getTestablePkg().functions.stream().forEach(function -> {
                try {
                    String functionClassName = BFileUtil.getQualifiedClassName(source.packageID.orgName.value,
                            source.packageID.name.value,
                            getClassName(function.pos.src.cUnitName));
                    testFunctionNames.put(function.name.value, functionClassName);
                } catch (RuntimeException e) {
                    // we do nothing here
                }
            });

            String [] pathList = new String[moduleDependencies.size()];
            int iterator = 0;
            for (Path path : moduleDependencies) {
                pathList[iterator++] = path.toString();
            }
            // set data
            TestJsonData testJsonData = new TestJsonData();
            testJsonData.setInitFunctionName(initFunctionName);
            testJsonData.setStartFunctionName(startFunctionName);
            testJsonData.setStopFunctionName(stopFunctionName);
            testJsonData.setTestInitFunctionName(testInitFunctionName);
            testJsonData.setTestStartFunctionName(testStartFunctionName);
            testJsonData.setTestStopFunctionName(testStopFunctionName);
            testJsonData.setCallableFunctionNames(normalFunctionNames);
            testJsonData.setTestFunctionNames(testFunctionNames);
            testJsonData.setPackageName(packageName);
            testJsonData.setHasTestablePackages(hasTestablePackages);
            testJsonData.setSourceRootPath(sourceRootPath.toString());
            testJsonData.setJarPath(jarPath.toString());
            testJsonData.setModuleJarName(moduleJarName);
            testJsonData.setPackageID(source.packageID);
            testJsonData.setDependencyJarPaths(pathList);
            // write to json
            writeDataToJsonFile(testJsonData, jsonPath);
        });
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

    private void updateDependencyJarPaths(List<BPackageSymbol> importPackageSymbols, BuildContext buildContext,
                                          HashSet<Path> dependencyJarPaths) {
        for (BPackageSymbol importPackageSymbol : importPackageSymbols) {
            PackageID importPkgId = importPackageSymbol.pkgID;
            if (!buildContext.moduleDependencyPathMap.containsKey(importPkgId)) {
                continue;
            }
            // add imported module's dependent jar paths
            HashSet<Path> testDependencies = buildContext.moduleDependencyPathMap.get(importPkgId).platformLibs;
            dependencyJarPaths.addAll(testDependencies);

            // add imported module's jar path
            Path testJarPath = buildContext.getTestJarPathFromTargetCache(importPkgId);
            Path moduleJarPath = buildContext.getJarPathFromTargetCache(importPkgId);
            if (Files.exists(testJarPath)) {
                dependencyJarPaths.add(testJarPath);
            } else if (Files.exists(moduleJarPath)) {
                dependencyJarPaths.add(moduleJarPath);
            }
            updateDependencyJarPaths(importPackageSymbol.imports, buildContext, dependencyJarPaths);
        }
    }

}
