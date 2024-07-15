/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.codegen.optimizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Test cases to verify optimized class files from `bal build --optimize` command.
 *
 * @since 2201.10.0
 */
public class NativeDependencyOptimizationTests extends BaseTest {

    private static final String NATIVE_DEPENDENCY_OPTIMIZATION_REPORT = "native_dependency_optimization_report.json";
    private static final String TEST_PROJECT_LOCATION = "src/test/resources/codegen-optimizer/native-interop-projects/";
    private static final String NATIVE_LIB_SOURCES_LOCATION =
            "src/test/resources/codegen-optimizer/native-lib-sources/";
    private static final String USED_CLASSES = "usedClasses";
    private static final String UNUSED_CLASSES = "unusedClasses";
    private Path tempBalProjectPath;
    private Path nativeLibSourcePaths;

    private static JsonObject fileContentAsObject(Path filePath) throws IOException {
        String contentAsString = new String(Files.readAllBytes(filePath));
        return JsonParser.parseString(contentAsString).getAsJsonObject();
    }

    private static List<String> getFunctionNamesFromJson(JsonObject parentJson, String fieldName) {
        return parentJson.get(fieldName).getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
    }

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        Path parentProjectPath = Files.createTempDirectory("b7a-codegen-optimization-test-" + System.nanoTime());
        tempBalProjectPath = parentProjectPath.resolve("native-interop-test-resources");
        nativeLibSourcePaths = parentProjectPath.resolve("native-libs");

        FileUtils.copyDirectory(Path.of(TEST_PROJECT_LOCATION).toAbsolutePath().normalize().toFile(),
                tempBalProjectPath.toFile());
        FileUtils.copyDirectory(Path.of(NATIVE_LIB_SOURCES_LOCATION).toAbsolutePath().normalize().toFile(),
                nativeLibSourcePaths.toFile());

        buildAndCopyNativeLibs();
    }

    public void buildAndCopyNativeLibs() throws IOException, InterruptedException {
        // Native lib jars are not packed with the tests. Only the source files of the native libs are packed.
        // Compiled jars of native dependencies are generated for each test run.
        for (File balProject : Objects.requireNonNull(
                nativeLibSourcePaths.toAbsolutePath().normalize().toFile().listFiles())) {
            for (File nativeLib : Objects.requireNonNull(balProject.listFiles())) {
                Path nativeLibJarPath = buildClassesAndGetJarFilePath(nativeLib.toPath());
                Path destPath = tempBalProjectPath.resolve(balProject.getName()).resolve("libs")
                        .resolve(nativeLibJarPath.getFileName().toString());
                Files.createDirectory(destPath.getParent());
                Files.copy(nativeLibJarPath, destPath);
            }
        }
    }

    private Path buildClassesAndGetJarFilePath(Path parentPackagePath) throws IOException, InterruptedException {
        Path classCache = parentPackagePath.resolve("generated");
        Path generatedJar = parentPackagePath.getParent().resolve(parentPackagePath.getFileName() + ".jar");
        compileJavaFiles(parentPackagePath, classCache);
        packClassFilesToJar(generatedJar, classCache);
        return generatedJar;
    }

    private void compileJavaFiles(Path javaFileDirectory, Path classFileDirectory)
            throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>();
        commands.add("javac");
        for (File file : Objects.requireNonNull(javaFileDirectory.toFile().listFiles())) {
            commands.add(file.getPath());
        }
        commands.add("-d");
        commands.add(classFileDirectory.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.start().waitFor();
    }

    private void packClassFilesToJar(Path generatedJarPath, Path classFileDirectory)
            throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>();
        commands.add("jar");
        commands.add("cf");
        commands.add(generatedJarPath.toString());
        for (File file : Objects.requireNonNull(classFileDirectory.toFile().listFiles())) {
            commands.add(file.getName());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(classFileDirectory.toFile());
        processBuilder.start().waitFor();
    }

    @Test(dataProvider = "InteropInfoProvider")
    private void testAll(String projectName, String[] usedClassNames, String[] unusedClassNames)
            throws BallerinaTestException, IOException {

        String projectPath = tempBalProjectPath.resolve(projectName).toString();
        emitOptimizationReports(projectPath);
        JsonObject emittedJsonObject = fileContentAsObject(
                Path.of(projectPath).resolve("target").resolve(NATIVE_DEPENDENCY_OPTIMIZATION_REPORT));

        Assert.assertTrue(getFunctionNamesFromJson(emittedJsonObject, USED_CLASSES).containsAll(
                Arrays.stream(usedClassNames).toList()));
        Assert.assertTrue(getFunctionNamesFromJson(emittedJsonObject, UNUSED_CLASSES).containsAll(
                Arrays.stream(unusedClassNames).toList()));
    }

    @DataProvider(name = "InteropInfoProvider")
    public Object[][] getInteropInfo() {
        return new Object[][]{
                {"vanilla_native_interop_call", new String[]{"Foo.class"}, new String[]{"Bar.class"}},
                {"native_interop_call_with_transitive_classes", new String[]{"Foo.class", "Baz.class"},
                        new String[]{"Bar.class"}},
                {"native_interop_call_with_inheritance", new String[]{"Foo.class", "Bar.class"},
                        new String[]{"Baz.class"}},
                {"native_interop_call_with_interface_usage", new String[]{"Foo.class", "Bar.class"},
                        new String[]{"Baz.class", "Quz.class"}},
                {"native_interop_call_with_class_usage",
                        new String[]{"Foo.class", "Bar.class", "Quz.class", "Corge.class"}, new String[]{"Baz.class"}},
        };
    }

    private void emitOptimizationReports(String projectPath) throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.runMain("build", new String[]{"--optimize", "--optimize-report"}, envProperties, null,
                new LogLeecher[]{}, projectPath);
    }
}
