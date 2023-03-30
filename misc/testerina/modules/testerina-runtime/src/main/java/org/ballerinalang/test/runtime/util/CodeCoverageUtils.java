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
package org.ballerinalang.test.runtime.util;

import io.ballerina.identifier.Utils;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.test.runtime.entity.NormalizedCoverageClass;
import org.ballerinalang.test.runtime.entity.PartialCoverageModifiedLine;
import org.ballerinalang.test.runtime.entity.PartialCoverageModifiedSourceFile;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_XML_FILE;

/**
 * Class containing utility methods required to generate the coverage report.
 *
 * @since 1.2.0
 */
public class CodeCoverageUtils {

    private static final PrintStream errStream = System.err;

    private CodeCoverageUtils() {}

    /**
     * Checks if a given code coverage report format was requested by user.
     *
     * @param coverageReportFormat
     * @param format
     * @return
     */
    public static boolean isRequestedReportFormat(String coverageReportFormat, String format) {
        boolean isRequested = false;
        if (coverageReportFormat != null) {
            isRequested = coverageReportFormat.equals(format);
        }
        return isRequested;
    }

    /**
     * Util method to extract required class files for code coverage analysis.
     *
     * @param source      path of testable jar
     * @param destination path to extract the classes
     * @param orgName     org name of the project being executed
     * @param moduleName  name of the module being executed
     * @throws NoSuchFileException if source file doesnt exist
     */
    public static void unzipCompiledSource(Path source, Path destination, String orgName,
                                           String moduleName, boolean enableIncludesFilter,
                                           String includesInCoverage) throws NoSuchFileException {
        String destJarDir = destination.toString();
        try (JarFile jarFile = new JarFile(source.toFile())) {
            Enumeration<JarEntry> enu = jarFile.entries();
            while (enu.hasMoreElements()) {
                JarEntry entry = enu.nextElement();
                File file = new File(destJarDir, entry.getName());
                if (isRequiredFile(entry.getName(), orgName, enableIncludesFilter,
                        includesInCoverage)) {
                    if (!file.exists()) {
                        Files.createDirectories(file.getParentFile().toPath());
                    }
                    if (entry.isDirectory()) {
                        continue;
                    }
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

            }
        } catch (NoSuchFileException e) {
            String msg = "Unable to generate code coverage for the module " + moduleName + ". Source file does not " +
                    "exist";
            errStream.println(msg);
            throw new NoSuchFileException(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isRequiredFile(String path, String orgName, boolean enableIncludesFilter,
                                          String includesInCoverage) {
        return !(path.contains("$_init") || path.contains("META-INF") || path.contains("/tests/")
                || (path.contains("$frame$") && (path.contains("module") || path.contains(orgName)))
                || path.contains("module-info.class")
                || (enableIncludesFilter && !isIncluded(path, includesInCoverage)));
    }

    private static String normalizeRegexPattern(String pattern) {
        if (pattern.contains(TesterinaConstants.WILDCARD)) {
            pattern = pattern.replace(TesterinaConstants.WILDCARD,
                    TesterinaConstants.DOT + TesterinaConstants.WILDCARD);
        }
        return pattern;
    }

    private static boolean isIncluded(String path, String includesInCoverage) {
        boolean isIncluded = false;
        if (includesInCoverage != null) {
            List<String> includedPackages = Arrays.asList(includesInCoverage.split(":"));
            for (String packageName : includedPackages) {
                packageName = packageName.replace(".", "/");
                Pattern pattern = Pattern.compile(normalizeRegexPattern(packageName));
                Matcher matcherStr = pattern.matcher(path);
                if (matcherStr.find()) {
                    isIncluded = true;
                    break;
                }
            }
        }
        return isIncluded;
    }

    /**
     * Deletes a provided directory.
     *
     * @param dir directory to delete
     * @throws IOException if deletion fails
     */
    public static void deleteDirectory(File dir) throws IOException {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDirectory(f);
                }
            }
        }
        Files.deleteIfExists(dir.toPath());
    }

    /**
     * Extracts the Testerina report zip from resources to a given destination.
     *
     * @param source zip stream
     * @param target target directory
     * @throws IOException if extraction failed
     */
    public static void unzipReportResources(InputStream source, File target) throws IOException {
        final ZipInputStream zipStream = new ZipInputStream(source);
        ZipEntry nextEntry;
        while ((nextEntry = zipStream.getNextEntry()) != null) {
            final String name = nextEntry.getName();
            // only extract files
            if (!name.endsWith("/")) {
                final File nextFile = new File(target, name);

                // create directories
                final File parent = nextFile.getParentFile();
                if (parent != null) {
                    Files.createDirectories(parent.toPath());
                }

                // write file
                try (OutputStream targetStream = new FileOutputStream(nextFile)) {
                    final int bufferSize = 4 * 1024;
                    final byte[] buffer = new byte[bufferSize];

                    int nextCount;
                    while ((nextCount = zipStream.read(buffer)) >= 0) {
                        targetStream.write(buffer, 0, nextCount);
                    }
                }
            }
        }
    }

    /**
     * Modify Classes in CoverageBuilder to reflect ballerina source root.
     *
     * @param classesList Collection of class coverage
     * @return Collection of class coverage
     */
    private static  Collection<IClassCoverage> modifyClasses(Collection<IClassCoverage> classesList,
                                                             Package packageInstance) {
        Collection<IClassCoverage> modifiedClasses = new ArrayList<>();
        for (IClassCoverage classCoverage : classesList) {
            if (classCoverage.getSourceFileName() != null &&
                    classCoverage.getSourceFileName().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                if (classCoverage.getSourceFileName().startsWith("tests/")) {
                    continue;
                } else {
                    //Normalize package name and class name for classes generated for bal files
                    IClassCoverage modifiedClassCoverage = new NormalizedCoverageClass(classCoverage,
                            normalizeFileName(classCoverage.getPackageName(), packageInstance),
                            normalizeFileName(classCoverage.getName(), packageInstance));
                    modifiedClasses.add(modifiedClassCoverage);
                }
            } else {
                modifiedClasses.add(classCoverage);
            }
        }
        return modifiedClasses;
    }

    /**
     * Modify source file for ballerina package.
     *
     * @param sourceFiles Collection of source files
     * @param packageInstance Package
     * @return Collection of source file coverage
     */
    private static Collection<ISourceFileCoverage> modifySourceFiles(Collection<ISourceFileCoverage> sourceFiles,
                                                                     Package packageInstance) {
        Collection<ISourceFileCoverage> modifiedSourceFiles = new ArrayList<>();
        for (ISourceFileCoverage sourcefile : sourceFiles) {
            ISourceFileCoverage modifiedSourceFile;
            List<ILine> modifiedLines;
            if (sourcefile.getName().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                if (sourcefile.getName().startsWith("tests/")) {
                    continue;
                } else {
                    modifiedLines = modifyLines(sourcefile);
                    //Normalize source file package name
                    modifiedSourceFile = new PartialCoverageModifiedSourceFile(sourcefile,
                            modifiedLines, normalizeFileName(sourcefile.getPackageName(), packageInstance));
                    modifiedSourceFiles.add(modifiedSourceFile);
                }
            } else {
                modifiedSourceFiles.add(sourcefile);
            }
        }
        return modifiedSourceFiles;
    }

    /**
     * Normalize file name for ballerina file.
     *
     * @param fileName String
     * @param packageInstance Package
     * @return
     */
    private static String normalizeFileName(String fileName, Package packageInstance) {
        String orgName = Utils.encodeNonFunctionIdentifier(
                packageInstance.packageOrg().toString());
        //Get package instance and traverse through all the modules
        for (Module module : packageInstance.modules()) {
            String packageName = Utils.encodeNonFunctionIdentifier(
                    module.moduleName().toString());
            String sourceRoot = module.project().sourceRoot().getFileName().toString();
            if (!module.isDefaultModule()) {
                sourceRoot = sourceRoot + "/" + ProjectConstants.MODULES_ROOT + "/" +
                        module.moduleName().moduleNamePart();
            }
            if (fileName.contains(orgName + "/" + packageName + "/")) {
                //Escape special characters before using in regex
                orgName = Pattern.quote(orgName);
                packageName = Pattern.quote(packageName);
                // Capture file paths with the format "orgName/packageName/xxxx/file-name" and replace with
                // "<source-root>/file-name"
                String normalizedFileName = fileName.replaceAll("^" + orgName + "/" +
                        packageName + "/.*/", sourceRoot + "/");
                // Capture remaining file paths with the format "orgName/packageName/file-name" and replace
                // with "<source-root>"
                normalizedFileName = normalizedFileName.replaceAll("^" + orgName + "/" +
                        packageName + "/.*", sourceRoot);
                return normalizedFileName;
            }
        }
        return fileName;
    }

    /**
     * Modify source file coverage to update partial coverage information.
     *
     * @param sourcefile ISourceFileCoverage
     * @return List of modified lines
     */
    private static List<ILine> modifyLines(ISourceFileCoverage sourcefile) {
        List<ILine> modifiedLines = new ArrayList<>();
        for (int i = sourcefile.getFirstLine(); i <= sourcefile.getLastLine(); i++) {
            ILine line = sourcefile.getLine(i);
            ILine modifiedLine = new PartialCoverageModifiedLine(line.getInstructionCounter(), line.getBranchCounter());
            modifiedLines.add(modifiedLine);
        }
        return modifiedLines;
    }

    /**
     * Modify bundle coverage to update partial coverage information.
     *
     * @param title String
     * @param packageInstance Package
     * @param packageNativeClassCoverageList IClassCoverage list for native classes
     * @param packageBalClassCoverageList IClassCoverage list for ballerina classes
     * @param packageSourceCovList ISourceFileCoverage list for ballerina source files
     * @return
     */
    private static IBundleCoverage getPartialCoverageModifiedBundle(String title, Package packageInstance,
                                                                    List<IClassCoverage> packageNativeClassCoverageList,
                                                                    List<IClassCoverage> packageBalClassCoverageList,
                                                                    List<ISourceFileCoverage> packageSourceCovList) {
        CoverageBuilder packageCoverageBuilder = new CoverageBuilder();
        for (IClassCoverage classCov : packageNativeClassCoverageList) {
            try {
                // Add non package specific classes to coverage builder.
                packageCoverageBuilder.visitCoverage(classCov);
            } catch (IllegalStateException exception) {
                continue;
            }
        }
        // Add both native and bal class coverages to the class list
        packageBalClassCoverageList.addAll(packageCoverageBuilder.getClasses());
        // Add both native and bal source file coverages to a single list
        packageSourceCovList.addAll(packageCoverageBuilder.getSourceFiles());
        return new BundleCoverageImpl(title, modifyClasses(packageBalClassCoverageList, packageInstance),
                modifySourceFiles(packageSourceCovList, packageInstance));
    }

    /**
     * Create jacoco XML report for package.
     *
     * @param project Project
     * @param packageExecData List of ExecutionData
     * @param packageNativeClassCovList  List of native class coverages for this package
     * @param packageBalClassCovList List of ballerina class coverages for this package
     * @param packageSourceCovList List of ballerina source coverages for this package
     * @param packageSessionInfo List of SessionInfo
     * @throws IOException
     */
    public static void createXMLReport(Project project,
                                       List<ExecutionData> packageExecData,
                                       List<IClassCoverage> packageNativeClassCovList,
                                       List<IClassCoverage> packageBalClassCovList,
                                       List<ISourceFileCoverage> packageSourceCovList,
                                       List<SessionInfo> packageSessionInfo) throws IOException {
        Target target = new Target(project.targetDir());
        String title = target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR).toFile().getName();
        XMLFormatter xmlFormatter = new XMLFormatter();
        File reportFile = new File(target.getReportPath().resolve(
                project.currentPackage().packageName().value()).resolve(REPORT_XML_FILE).toString());
        reportFile.getParentFile().mkdirs();
        try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
            IReportVisitor visitor = xmlFormatter.createVisitor(fileOutputStream);
            visitor.visitInfo(packageSessionInfo, packageExecData);
            visitor.visitBundle(getPartialCoverageModifiedBundle(title, project.currentPackage(),
                    packageNativeClassCovList, packageBalClassCovList, packageSourceCovList),
                    null);
            visitor.visitEnd();
        }
    }
}
