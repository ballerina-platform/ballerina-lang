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

package org.wso2.ballerina.docgen.docs;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.util.program.BLangPrograms;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.docgen.docs.html.HtmlDocumentWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main class to generate a ballerina documentation
 */
public class BallerinaDocGeneratorMain {

    private static final PrintStream out = System.out;

    public static void main(String[] args) {

        if ((args == null) || (args.length < 1 || args.length > 2)) {
            out.println("Usage: docerina [ballerina-package-path] [package-filter]");
            return;
        }

        try {
            Map<String, BLangPackage> docsMap =
                    generatePackageDocsFromBallerina(args[0], (args.length == 2) ? args[1] : null);
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(docsMap.values());
        } catch (IOException e) {
            out.println("Docerina: Could not read ballerina file(s): " + e.getMessage());
        }
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link Package} objects. Key - Ballerina package name Value - {@link Package}
     */
    public static Map<String, BLangPackage> generatePackageDocsFromBallerina(String packagePath) throws IOException {
        return generatePackageDocsFromBallerina(packagePath, null);
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter the name of the package or pattern to be excluded
     * @return a map of {@link Package} objects. Key - Ballerina package name Value - {@link Package}
     */
    public static Map<String, BLangPackage> generatePackageDocsFromBallerina(String packagePath, String packageFilter)
            throws IOException {

        Path pkgPath = Paths.get(packagePath);
        List<Path> packagePaths = new ArrayList<>();
        if (Files.isDirectory(pkgPath)) {
            BallerinaSubPackageVisitor subPackageVisitor = new BallerinaSubPackageVisitor(pkgPath, packagePaths);
            Files.walkFileTree(pkgPath, subPackageVisitor);
        } else {
            packagePaths.add(Paths.get(""));
        }

        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();

        for (Path path : packagePaths) {
            BLangProgram bLangProgram = new BLangProgramLoader().loadLibrary(pkgPath, path);
            if (bLangProgram == null) {
                out.println(String.format("Docerina: Invalid Ballerina Package: %s", packagePath));
            } else {
                for (BLangPackage bLangPackage : bLangProgram.getLibraryPackages()) {
                    String packageName = bLangPackage.getPackagePath();
                    if ((packageFilter != null) && (packageFilter.trim().length() > 0)
                            && (packageName.startsWith(packageFilter.replace(".*", "")))) {
                        out.println("Package " + packageName + " excluded");
                        continue;
                    }

                    dataHolder.getPackageMap().put(packageName, bLangPackage);
                }
            }
        }

        return dataHolder.getPackageMap();
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
            if (file.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
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
