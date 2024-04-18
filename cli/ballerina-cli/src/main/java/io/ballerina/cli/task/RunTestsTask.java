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

package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.JacocoInstrumentUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TestProcessor;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.cli.utils.TestUtils.cleanTempCache;
import static io.ballerina.cli.utils.TestUtils.clearFailedTestsJson;
import static io.ballerina.cli.utils.TestUtils.generateCoverage;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static io.ballerina.cli.utils.TestUtils.loadModuleStatusFromFile;
import static io.ballerina.cli.utils.TestUtils.writeToTestSuiteJson;
import static io.ballerina.projects.util.ProjectConstants.GENERATED_MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FULLY_QULAIFIED_MODULENAME_SEPRATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.IGNORE_PATTERN;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.STANDALONE_SRC_PACKAGENAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.WILDCARD;
import static org.ballerinalang.test.runtime.util.TesterinaUtils.getQualifiedClassName;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.USER_DIR;

/**
 * Task for executing tests.
 *
 * @since 2.0.0
 */
public class RunTestsTask implements Task {
    private final PrintStream out;
    private final PrintStream err;
    private final String includesInCoverage;
    private final String excludesInCoverage;
    private String groupList;
    private String disableGroupList;
    private boolean report;
    private boolean coverage;
    private String coverageReportFormat;
    private boolean isRerunTestExecution;
    private String singleExecTests;
    private Map<String, Module> coverageModules;
    private boolean listGroups;
    private final List<String> cliArgs;

    private final boolean isParallelExecution;

    TestReport testReport;
    private static final Boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("win");
    public static final String EXCLUDES_PATTERN_PATH_SEPARATOR = isWindows ? "\\\\" : "/";

    public static final String RELATIVE_PATH_PREFIX = isWindows ? ".\\" : "./";

    public static final String PATH_SEPARATOR = isWindows ? "\\" : "/";

    public static final String UNIX_PATH_SEPARATOR = "/";


    public RunTestsTask(PrintStream out, PrintStream err, boolean rerunTests, String groupList,
                        String disableGroupList, String testList, String includes, String coverageFormat,
                        Map<String, Module> modules, boolean listGroups, String excludes, String[] cliArgs,
                        boolean isParallelExecution)  {
        this.out = out;
        this.err = err;
        this.isRerunTestExecution = rerunTests;
        this.cliArgs = List.of(cliArgs);
        this.isParallelExecution = isParallelExecution;

        if (disableGroupList != null) {
            this.disableGroupList = disableGroupList;
        } else if (groupList != null) {
            this.groupList = groupList;
        }

        if (testList != null) {
            singleExecTests = testList;
        }
        this.includesInCoverage = includes;
        this.coverageReportFormat = coverageFormat;
        this.coverageModules = modules;
        this.listGroups = listGroups;
        this.excludesInCoverage = excludes;
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }

        report = project.buildOptions().testReport();
        coverage = project.buildOptions().codeCoverage();

        if (report || coverage) {
            testReport = new TestReport();
        }

        Path cachesRoot;
        Target target;
        Path testsCachePath;
        try {
            if (project.kind() == ProjectKind.BUILD_PROJECT) {
                cachesRoot = project.sourceRoot();
                target = new Target(project.targetDir());
            } else {
                cachesRoot = Files.createTempDirectory("ballerina-test-cache" + System.nanoTime());
                target = new Target(cachesRoot);
            }

            testsCachePath = target.getTestsCachePath();
        } catch (IOException e) {
            throw createLauncherException("error while creating target directory: ", e);
        }

        boolean hasTests = false;

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();
        TestProcessor testProcessor = new TestProcessor(jarResolver);
        List<String> moduleNamesList = new ArrayList<>();
        Map<String, TestSuite> testSuiteMap = new HashMap<>();
        List<String> updatedSingleExecTests;
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "bal test" command which only executes tests
        // in packages.
        List<String> mockClassNames = new ArrayList<>();
        for (ModuleDescriptor moduleDescriptor :
                project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = project.currentPackage().module(moduleDescriptor.name());
            ModuleName moduleName = module.moduleName();

            TestSuite suite;
            try {
                suite = testProcessor.testSuite(module).orElse(null);
            } catch (ProjectException e) {
                throw createLauncherException(e.getMessage());
            }
            if (suite == null) {
                continue;
            }

            //Set 'hasTests' flag if there are any tests available in the package
            if (!hasTests) {
                hasTests = true;
            }

            if (!isRerunTestExecution) {
                clearFailedTestsJson(target.path());
            }
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                suite.setSourceFileName(project.sourceRoot().getFileName().toString());
            }
            suite.setReportRequired(report || coverage);
            String resolvedModuleName =
                    module.isDefaultModule() ? moduleName.toString() : module.moduleName().moduleNamePart();
            testSuiteMap.put(resolvedModuleName, suite);
            moduleNamesList.add(resolvedModuleName);
            Map<String, String> mockFunctionMap = suite.getMockFunctionNamesMap();
            for (Map.Entry<String, String> entry : mockFunctionMap.entrySet()) {
                String key = entry.getKey();
                String functionToMockClassName;
                // Find the first delimiter and compare the indexes
                // The first index should always be a delimiter. Which ever one that is denotes the mocking type
                if (!key.contains(MOCK_LEGACY_DELIMITER)) {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                } else if (!key.contains(MOCK_FN_DELIMITER)) {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                } else {
                    if (key.indexOf(MOCK_FN_DELIMITER) < key.indexOf(MOCK_LEGACY_DELIMITER)) {
                        functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                    } else {
                        functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                    }
                }
                mockClassNames.add(functionToMockClassName);
            }
        }

        writeToTestSuiteJson(testSuiteMap, testsCachePath);

        if (hasTests) {
            int testResult;
            try {
                Set<String> exclusionClassList = new HashSet<>();
                testResult = runTestSuite(target, project.currentPackage(), jBallerinaBackend, mockClassNames,
                             exclusionClassList);

                if (report || coverage) {
                    for (String moduleName : moduleNamesList) {
                        ModuleStatus moduleStatus = loadModuleStatusFromFile(
                                testsCachePath.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE));
                        if (moduleStatus == null) {
                            continue;
                        }

                        if (!moduleName.equals(project.currentPackage().packageName().toString())) {
                            moduleName = ModuleName.from(project.currentPackage().packageName(), moduleName).toString();
                        }
                        testReport.addModuleStatus(moduleName, moduleStatus);
                    }
                    try {
                        generateCoverage(project, testReport, jBallerinaBackend, this.includesInCoverage,
                                this.coverageReportFormat, this.coverageModules, exclusionClassList);
                        generateTesterinaReports(project, testReport, this.out, target);
                    } catch (IOException e) {
                        cleanTempCache(project, cachesRoot);
                        throw createLauncherException("error occurred while generating test report :", e);
                    }
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while running tests", e);
            }

            if (testResult != 0) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("there are test failures");
            }
        } else {
            out.println("\tNo tests found");
        }

        // Cleanup temp cache for SingleFileProject
        cleanTempCache(project, cachesRoot);
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().testingExecutionDuration = System.currentTimeMillis() - start;
        }
    }

    private int runTestSuite(Target target, Package currentPackage, JBallerinaBackend jBallerinaBackend,
                             List<String> mockClassNames, Set<String> exclusionClassList) throws IOException,
            InterruptedException, ClassNotFoundException {
        String packageName = currentPackage.packageName().toString();
        String orgName = currentPackage.packageOrg().toString();
        String classPath = getClassPath(jBallerinaBackend, currentPackage);
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        cmdArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        cmdArgs.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));

        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;
        String jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();
        if (coverage) {
            if (!mockClassNames.isEmpty()) {
                // If we have mock function we need to use jacoco offline instrumentation since jacoco doesn't
                // support dynamic class file transformations while instrumenting.
                List<URL> jarUrlList = getModuleJarUrlList(jBallerinaBackend, currentPackage);
                Path instrumentDir = target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                        .resolve(TesterinaConstants.JACOCO_INSTRUMENTED_DIR);
                JacocoInstrumentUtils.instrumentOffline(jarUrlList, instrumentDir, mockClassNames);
            }
            String agentCommand = "-javaagent:"
                    + jacocoAgentJarPath
                    + "=destfile="
                    + target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                    .resolve(TesterinaConstants.EXEC_FILE_NAME);
            if (!STANDALONE_SRC_PACKAGENAME.equals(packageName) && this.includesInCoverage == null) {
                // add user defined classes for generating the jacoco exec file
                agentCommand += ",includes=" + orgName + ".*";
            } else {
                agentCommand += ",includes=" + this.includesInCoverage;
            }

            if (!STANDALONE_SRC_PACKAGENAME.equals(packageName) && this.excludesInCoverage != null) {
                if (!this.excludesInCoverage.equals("")) {
                    List<String> exclusionSourceList = new ArrayList<>(List.of((this.excludesInCoverage).
                            split(",")));
                    getclassFromSourceFilePath(exclusionSourceList, currentPackage, exclusionClassList);
                    agentCommand += ",excludes=" + String.join(":", exclusionClassList);
                }
            }

            cmdArgs.add(agentCommand);
        }

        cmdArgs.addAll(Lists.of("-cp", classPath));
        if (isInDebugMode()) {
            cmdArgs.add(getDebugArgs(this.err));
        }
        cmdArgs.add(mainClassName);

        // Adds arguments to be read at the Test Runner
        cmdArgs.add(target.path().toString());
        cmdArgs.add(jacocoAgentJarPath);
        cmdArgs.add(Boolean.toString(report));
        cmdArgs.add(Boolean.toString(coverage));
        cmdArgs.add(this.groupList != null ? this.groupList : "");
        cmdArgs.add(this.disableGroupList != null ? this.disableGroupList : "");
        cmdArgs.add(this.singleExecTests != null ? this.singleExecTests : "");
        cmdArgs.add(Boolean.toString(isRerunTestExecution));
        cmdArgs.add(Boolean.toString(listGroups));
        cmdArgs.add(Boolean.toString(isParallelExecution));
        cmdArgs.addAll(cliArgs);

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
        Process proc = processBuilder.start();
        return proc.waitFor();
    }

    private List<Path> getAllSourceFilePaths(String projectRootString) throws IOException {
        List<Path> sourceFilePaths = new ArrayList<>();
        List<Path> paths = Files.walk(Paths.get(projectRootString), 3).toList();

        if (isWindows) {
            projectRootString = projectRootString.replace(PATH_SEPARATOR, EXCLUDES_PATTERN_PATH_SEPARATOR);
        }

        List<Path> defaultModuleSources = filterPathStream(paths.stream(), projectRootString +
                EXCLUDES_PATTERN_PATH_SEPARATOR + WILDCARD + BLANG_SOURCE_EXT);
        List<Path> generatedSources = filterPathStream(paths.stream(),
                projectRootString + EXCLUDES_PATTERN_PATH_SEPARATOR +
                        GENERATED_MODULES_ROOT + WILDCARD + WILDCARD + BLANG_SOURCE_EXT);
        List<Path> moduleSources = filterPathStream(paths.stream(),
                projectRootString + EXCLUDES_PATTERN_PATH_SEPARATOR + MODULES_ROOT +
                        EXCLUDES_PATTERN_PATH_SEPARATOR + WILDCARD + EXCLUDES_PATTERN_PATH_SEPARATOR +
                        WILDCARD + BLANG_SOURCE_EXT);
        Stream.of(defaultModuleSources, generatedSources, moduleSources).forEach(sourceFilePaths::addAll);
        return sourceFilePaths;

    }

    private static List<Path> filterPathStream(Stream<Path> pathStream, String combinedPattern) {
        return pathStream.filter(
                        FileSystems.getDefault().getPathMatcher("glob:" + combinedPattern)::matches)
                .collect(Collectors.toList());
    }

    private void getclassFromSourceFilePath(List<String> sourcePatternList, Package currentPackage,
                                                                            Set<String> classFileList) {
        String sourceRoot = currentPackage.project().sourceRoot().toString();
        try {
            List<Path> allSourceFilePaths = getAllSourceFilePaths(sourceRoot);
            List<Path> validSourceFileList = extractValidSourceList(allSourceFilePaths, sourcePatternList, sourceRoot);

            for (Path sourceFilePath : validSourceFileList) {
                String sourceFile = sourceFilePath.toAbsolutePath().toString();
                sourceFile = sourceFile.replace(sourceRoot + PATH_SEPARATOR, "");

                String org = currentPackage.packageOrg().toString();
                String packageName = currentPackage.packageName().toString();
                String version = currentPackage.packageVersion().toString();

                if (sourceFile.contains((MODULES_ROOT)) || sourceFile.contains((GENERATED_MODULES_ROOT))) {
                    sourceFile = sourceFile.replace(MODULES_ROOT + PATH_SEPARATOR, "")
                            .replace(GENERATED_MODULES_ROOT + PATH_SEPARATOR, "");
                }

                if (sourceFile.split(Pattern.quote(PATH_SEPARATOR)).length == 2) {
                    String moduleName = sourceFile.split(Pattern.quote(PATH_SEPARATOR))[0];
                    String balFile = sourceFile.split(Pattern.quote(PATH_SEPARATOR))[1].replace(BLANG_SOURCE_EXT, "");
                    String className = getQualifiedClassName(org, packageName +
                                    FULLY_QULAIFIED_MODULENAME_SEPRATOR + moduleName, version, balFile);
                    classFileList.add(className);
                } else if (sourceFile.split(Pattern.quote(PATH_SEPARATOR)).length == 1) {
                    String balFile = sourceFile.split(Pattern.quote(PATH_SEPARATOR))[0].replace(BLANG_SOURCE_EXT, "");
                    String className = getQualifiedClassName(org, packageName, version, balFile);
                    classFileList.add(className);
                }
            }
        } catch (IOException e) {
            throw createLauncherException("unable to resolve classes for given source files.");
        }
    }

    private List<Path> extractValidSourceList(List<Path> allSourceFilePaths, List<String> sourcePatternList,
                                              String sourceRoot) {
        List<String> unMatchedPatterns = new ArrayList<>();
        Set<Path> validSourceFileSet = new HashSet<>();
        for (String sourcePattern : sourcePatternList) {
            String unModifiedSourcePattern = sourcePattern;
            boolean isIgnoringPattern = false;
            if (sourcePattern.startsWith(IGNORE_PATTERN)) {
                isIgnoringPattern = true;
                sourcePattern = sourcePattern.substring(1);
            }

            if (isWindows) {
                sourcePattern = sourcePattern.replace(UNIX_PATH_SEPARATOR, PATH_SEPARATOR);
            }

            // Replace the './' with 'sourceRoot/' to handle prefix properly.
            if (sourcePattern.startsWith(RELATIVE_PATH_PREFIX)) {
                sourcePattern = sourceRoot + PATH_SEPARATOR + sourcePattern.substring(2);
            } else if (sourcePattern.startsWith(PATH_SEPARATOR) && !sourcePattern.startsWith(sourceRoot +
                    PATH_SEPARATOR)) { // Replace the '/' with 'sourceRoot/' to handle prefix properly.
                sourcePattern = sourceRoot + PATH_SEPARATOR + sourcePattern.substring(1);
            } else if (!sourcePattern.startsWith(sourceRoot + PATH_SEPARATOR)) {
                // Add 'sourceRoot/' prefix if a directory/file is specified as 'foo'/'foo.bal'
                sourcePattern = WILDCARD + WILDCARD + PATH_SEPARATOR + sourcePattern;
            }


            // Convert 'foo/' or 'foo' as 'foo/**' and ignore 'foo*' or 'foo/*' or 'foo.bal'
            if (!sourcePattern.endsWith(WILDCARD) && !sourcePattern.endsWith(BLANG_SOURCE_EXT)) {
                if (!sourcePattern.endsWith(PATH_SEPARATOR)) {
                    sourcePattern = sourcePattern + PATH_SEPARATOR;
                }
                sourcePattern = sourcePattern + WILDCARD + WILDCARD;
            }

            if (isWindows) {
                sourcePattern = sourcePattern.replace(PATH_SEPARATOR, EXCLUDES_PATTERN_PATH_SEPARATOR);
            }

            if (!isIgnoringPattern) {
                List<Path> filteredPaths = filterPathStream(allSourceFilePaths.stream(), sourcePattern);
                if (filteredPaths.isEmpty()) {
                    unMatchedPatterns.add(unModifiedSourcePattern);
                    continue;
                }
                validSourceFileSet.addAll(filteredPaths);
                continue;
            }

            List<Path> filteredPaths = filterPathStream(validSourceFileSet.stream(), sourcePattern);
            if (filteredPaths.isEmpty()) {
                unMatchedPatterns.add(unModifiedSourcePattern);
                continue;
            }
            validSourceFileSet.removeAll(filteredPaths);
        }
        if (!unMatchedPatterns.isEmpty()) {
            out.println("WARNING: No matching sources found for " + String.join(", ", unMatchedPatterns));
        }

        return new ArrayList<>(validSourceFileSet);
    }

    private String getClassPath(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        List<Path> dependencies = new ArrayList<>();
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);

            // Skip getting file paths for execution if module doesnt contain a testable jar
            if (!module.testDocumentIds().isEmpty() || module.project().kind()
                    .equals(ProjectKind.SINGLE_FILE_PROJECT)) {
                for (JarLibrary jarLibs : jarResolver.getJarFilePathsRequiredForTestExecution(module.moduleName())) {
                    dependencies.add(jarLibs.path());
                }
            }
        }
        dependencies = dependencies.stream().distinct().collect(Collectors.toList());

        List<Path> jarList = getModuleJarPaths(jBallerinaBackend, currentPackage);
        dependencies.removeAll(jarList);

        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.stream().map(Path::toString).forEach(classPath::add);
        return classPath.toString();
    }

    private List<Path> getModuleJarPaths(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        List<Path> moduleJarPaths = new ArrayList<>();

        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);

            PlatformLibrary generatedJarLibrary = jBallerinaBackend.codeGeneratedLibrary(currentPackage.packageId(),
                    module.moduleName());
            moduleJarPaths.add(generatedJarLibrary.path());

            if (!module.testDocumentIds().isEmpty()) {
                PlatformLibrary codeGeneratedTestLibrary = jBallerinaBackend.codeGeneratedTestLibrary(
                        currentPackage.packageId(), module.moduleName());
                moduleJarPaths.add(codeGeneratedTestLibrary.path());
            }
        }

        for (ResolvedPackageDependency resolvedPackageDependency : currentPackage.getResolution().allDependencies()) {
            Package pkg = resolvedPackageDependency.packageInstance();
            for (ModuleId moduleId : pkg.moduleIds()) {
                Module module = pkg.module(moduleId);
                moduleJarPaths.add(
                        jBallerinaBackend.codeGeneratedLibrary(pkg.packageId(), module.moduleName()).path());
            }
        }

        return moduleJarPaths.stream().distinct().collect(Collectors.toList());
    }

    private List<URL> getModuleJarUrlList(JBallerinaBackend jBallerinaBackend, Package currentPackage)
            throws MalformedURLException {
        List<Path> moduleJarPaths = getModuleJarPaths(jBallerinaBackend, currentPackage);
        List<URL> moduleJarUrls = new ArrayList<>(moduleJarPaths.size());
        for (Path path : moduleJarPaths) {
            moduleJarUrls.add(path.toUri().toURL());
        }
        return moduleJarUrls;
    }

}
