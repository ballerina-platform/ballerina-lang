/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.docgen.docs;

import org.ballerinalang.BLangASTBuilder;
import org.ballerinalang.docgen.docs.html.HtmlDocumentWriter;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main class to generate a ballerina documentation
 */
public class BallerinaDocGenerator {

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static final PrintStream out = System.out;

    private static final String BSOURCE_FILE_EXT = ".bal";

    /**
     * API to generate Ballerina API documentation.
     * 
     * @param output path to the output directory where the API documentation will be written to.
     * @param packageFilter comma separated list of package names to be filtered from the documentation.
     * @param sources either the path to the directories where Ballerina source files reside or a path to a Ballerina
     *            file which does not belong to a package.
     */
    public static void generateApiDocsWithFilter(String output, String packageFilter, String... sources) {
        generateApiDocs(output, packageFilter, false, sources);
    }
    
    /**
     * API to generate Ballerina API documentation for Native Source.
     * 
     * @param output path to the output directory where the API documentation will be written to.
     * @param packageFilter comma separated list of package names to be filtered from the documentation.
     * @param sources either the path to the directories where Ballerina source files reside or a path to a Ballerina
     *            file which does not belong to a package.
     */
    public static void generateNativeApiDocs(String output, String packageFilter, String... sources) {
        generateApiDocs(output, packageFilter, true, sources);
    }
    
    /**
     * API to generate Ballerina API documentation.
     * 
     * @param output path to the output directory where the API documentation will be written to.
     * @param packageFilter comma separated list of package names to be filtered from the documentation.
     * @param isNative whether the given packages are native or not.
     * @param sources either the path to the directories where Ballerina source files reside or a path to a Ballerina
     *            file which does not belong to a package.
     */
    public static void generateApiDocs(String output, String packageFilter, boolean isNative, String... sources) {
        HtmlDocumentWriter htmlDocumentWriter;
        if (output == null) {
            htmlDocumentWriter = new HtmlDocumentWriter();
        } else {
            htmlDocumentWriter = new HtmlDocumentWriter(output);
        }
        for (String source : sources) {
            try {
                Map<String, BLangPackage> docsMap = generatePackageDocsFromBallerina(source, packageFilter, isNative);
                htmlDocumentWriter.write(docsMap.values());
            } catch (Exception e) {
                out.println(String.format("docerina: API documentation generation failed for %s: %s", source,
                        e.getMessage()));
                log.error(String.format("API documentation generation failed for %s", source), e);
            }
        }
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link Package} objects. Key - Ballerina package name Value - {@link Package}
     */
    protected static Map<String, BLangPackage> generatePackageDocsFromBallerina(String packagePath) throws IOException {
        return generatePackageDocsFromBallerina(packagePath, null);
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @return a map of {@link Package} objects. Key - Ballerina package name Value - {@link Package}
     */
    protected static Map<String, BLangPackage> generatePackageDocsFromBallerina(String packagePath, 
            String packageFilter) throws IOException {
        return generatePackageDocsFromBallerina(packagePath, packageFilter, false);
    }
    
    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @param isNative whether the given packages are native or not.
     * @return a map of {@link Package} objects. Key - Ballerina package name Value - {@link Package}
     */
    protected static Map<String, BLangPackage> generatePackageDocsFromBallerina(String packagePath, 
            String packageFilter, boolean isNative) throws IOException {
        Path pkgPath = Paths.get(packagePath);
        final List<Path> packagePaths = new ArrayList<>();
        if (Files.isDirectory(pkgPath)) {
            BallerinaSubPackageVisitor subPackageVisitor = new BallerinaSubPackageVisitor(pkgPath, packagePaths);
            Files.walkFileTree(pkgPath, subPackageVisitor);
        } else {
            packagePaths.add(Paths.get(""));
        }

        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();
        if (!isNative) {
            // This is necessary to be true in order to Ballerina to work properly
            System.setProperty("skipNatives", "true");
        }

        for (Path path : packagePaths) {
            BLangProgram bLangProgram;
            if (isNative) {
                bLangProgram = new BLangASTBuilder().disableSemanticAnalyzer().build(pkgPath, path);
            } else {
                bLangProgram = new BLangASTBuilder().build(pkgPath, path);
            }
            if (bLangProgram == null) {
                out.println(String.format("docerina: invalid Ballerina package: %s", packagePath));
            } else {
                BLangPackage bLangPackage = bLangProgram.getEntryPackage();
                String packageName = bLangPackage.getPackagePath();
                if (isFilteredPackage(packageName, packageFilter)) {
                    if (BallerinaDocUtils.isDebugEnabled()) {
                        out.println("Package " + packageName + " excluded");
                    }
                    continue;
                }
                dataHolder.getPackageMap().put(packageName, bLangPackage);
            }
        }
        return dataHolder.getPackageMap();
    }

    private static boolean isFilteredPackage(String packageName, String packageFilter) {
        if ((packageFilter != null) && (packageFilter.trim().length() > 0)) {
            return Arrays.asList(packageFilter.split(",")).stream()
                    .filter(e -> packageName.startsWith(e.replace(".*", ""))).findAny().isPresent();
        }
        return false;
    }

    /**
     * Visits sub folders of a ballerina package.
     */
    static class BallerinaSubPackageVisitor extends SimpleFileVisitor<Path> {
        private Path source;
        private List<Path> subPackages;

        public BallerinaSubPackageVisitor(Path source, List<Path> aList) {
            this.source = source;
            this.subPackages = aList;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(BSOURCE_FILE_EXT)) {
                Path relativePath = source.relativize(file.getParent());
                if (!subPackages.contains(relativePath)) {
                    subPackages.add(relativePath);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getSubPackages() {
            return subPackages;
        }
    }
}
