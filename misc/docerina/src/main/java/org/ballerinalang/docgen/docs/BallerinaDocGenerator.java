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

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.Writer;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.PackageDoc;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.StaticCaption;
import org.ballerinalang.launcher.LauncherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Main class to generate a ballerina documentation.
 */
public class BallerinaDocGenerator {

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static final PrintStream out = System.out;

    private static final String BSOURCE_FILE_EXT = ".bal";
    private static final String PACKAGE_CONTENT_FILE = "Package.md";
    private static final Path BAL_BUILTIN = Paths.get("ballerina/builtin");
    private static final Path BAL_BUILTIN_CORE = Paths.get("ballerina/builtin/core");
    private static final String HTML = ".html";

    /**
     * API to generate Ballerina API documentation.
     *
     * @param output        path to the output directory where the API documentation will be written to.
     * @param packageFilter comma separated list of package names to be filtered from the documentation.
     * @param isNative      whether the given packages are native or not.
     * @param sources       either the path to the directories where Ballerina source files reside or a
     *                      path to a Ballerina file which does not belong to a package.
     */
    public static void generateApiDocs(String output, String packageFilter, boolean isNative, String... sources) {
        out.println("docerina: API documentation generation for sources - " + Arrays.toString(sources));
        for (String source : sources) {
            source = source.trim();
            try {
                Map<String, PackageDoc> docsMap;

                if (source.endsWith(".bal")) {
                    Path sourceFilePath = Paths.get(source);
                    Path parentDir = sourceFilePath.getParent();
                    Path fileName = sourceFilePath.getFileName();

                    if (fileName == null) {
                        log.warn("Skipping the source generation for invalid path: " + sourceFilePath);
                        continue;
                    }
                    if (parentDir == null) {
                        parentDir = Paths.get(".");
                    }
                    docsMap = generatePackageDocsFromBallerina(parentDir.toString(), fileName, packageFilter, isNative);
                } else {
                    Path dirPath = Paths.get(source);

                    //TODO fix this properly
                    //TODO Temporary fix that creates .ballerina to create project structure
                    Path projectFolder = dirPath.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
                    Files.createDirectory(projectFolder);

                    Path sourceRootPath = LauncherUtils.getSourceRootPath(dirPath.toString());
                    docsMap = generatePackageDocsFromBallerina(sourceRootPath.toString(), dirPath, packageFilter,
                            isNative);
                }
                if (docsMap.size() == 0) {
                    out.println("docerina: no package definitions found!");
                    return;
                }

                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("Generating HTML API documentation...");
                }

                String userDir = System.getProperty("user.dir");
                // If output directory is empty
                if (output == null) {
                    output = System.getProperty(BallerinaDocConstants.HTML_OUTPUT_PATH_KEY, userDir +
                            File.separator + "api-docs" + File.separator + "html");
                }

                // Create output directories
                Files.createDirectories(Paths.get(output));

                // Sort packages by package path
                List<PackageDoc> packageList = new ArrayList<>(docsMap.values());
                packageList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

                //Iterate over the packages to generate the pages
                List<String> packageNames = new ArrayList<>(docsMap.keySet());
                // Sort the package names
                Collections.sort(packageNames);

                List<Link> packageNameList = PackageName.convertList(packageNames);
                if (packageNames.contains("ballerina.builtin")) {
                    StaticCaption primitivesLinkName =
                                                    new StaticCaption(BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_NAME);
                    packageNameList.add(0, new Link(primitivesLinkName, BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF,
                                                                                                                false));
                }

                //Generate pages for the packages
                String packageTemplateName = System.getProperty(BallerinaDocConstants.PACKAGE_TEMPLATE_NAME_KEY,
                        "page");
                for (PackageDoc packageDoc: packageList) {
                    BLangPackage bLangPackage = packageDoc.bLangPackage;
                    String pkgDescription = packageDoc.description;

                    // Sort functions, connectors, structs, type mappers and annotationDefs
                    bLangPackage.getFunctions().sort(Comparator
                            .comparing(f ->(f.getReceiver() == null
                                            ? "" : f.getReceiver().getName()) + f.getName().getValue()));
                    bLangPackage.getObjects().sort(Comparator.comparing(c -> c.getName().getValue()));
                    bLangPackage.getStructs().sort(Comparator.comparing(s -> s.getName().getValue()));
                    bLangPackage.getAnnotations().sort(Comparator.comparing(a -> a.getName().getValue()));
                    bLangPackage.getEnums().sort(Comparator.comparing(a -> a.getName().getValue()));

                    // Sort connector actions
                    if ((bLangPackage.getConnectors() != null) && (bLangPackage.getConnectors().size() > 0)) {
                        bLangPackage.getConnectors().forEach(connector -> connector.getActions()
                                .sort(Comparator
                                    .comparing(a -> a.getName().getValue())));
                    }

                    String packagePath = refinePackagePath(bLangPackage);

                    Page page = Generator.generatePage(bLangPackage, packageNameList, pkgDescription);
                    String filePath = output + File.separator + packagePath + HTML;
                    Writer.writeHtmlDocument(page, packageTemplateName, filePath);

                    if ("ballerina.builtin".equals(packagePath)) {
                        Page primitivesPage = Generator.generatePageForPrimitives(bLangPackage, packageNameList);
                        String primitivesFilePath = output + File.separator + "primitive-types" + HTML;
                        Writer.writeHtmlDocument(primitivesPage, packageTemplateName, primitivesFilePath);
                    }
                }
                //Generate the index file with the list of all packages
                String indexTemplateName = System.getProperty(BallerinaDocConstants.PACKAGE_TEMPLATE_NAME_KEY,
                        "index");
                String indexFilePath = output + File.separator + "index" + HTML;
                Writer.writeHtmlDocument(packageNameList, indexTemplateName, indexFilePath);
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("Copying HTML theme...");
                }
                BallerinaDocUtils.copyResources("docerina-theme", output);
            } catch (IOException e) {
                out.println(String.format("docerina: API documentation generation failed for %s: %s", source,
                        e.getMessage()));
                log.error(String.format("API documentation generation failed for %s", source), e);
            }
        }
        try {
            String zipPath = System.getProperty(BallerinaDocConstants.OUTPUT_ZIP_PATH);
            if (zipPath != null) {
                BallerinaDocUtils.packageToZipFile(output, zipPath);
            }
        } catch (IOException e) {
            out.println(String.format("docerina: API documentation zip packaging failed for %s: %s", output,
                    e.getMessage()));
            log.error(String.format("API documentation zip packaging failed for %s", output), e);
        }
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot  points to the folder relative to which package path is given
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath)
            throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, packagePath, null);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath,
                                                                                String packageFilter)
            throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, packagePath, packageFilter, false);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(
            String sourceRoot, String packagePath, String packageFilter, boolean isNative) throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, Paths.get(packagePath), packageFilter, isNative);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   a {@link Path} object pointing either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @param isNative      whether the given packages are native or not.
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(
        String sourceRoot, Path packagePath, String packageFilter, boolean isNative) throws IOException {
        final List<Path> packagePaths = new ArrayList<>();
        final Map<String, Path> packageMdsMap = new HashMap<>();
        if (Files.isDirectory(packagePath)) {
            BallerinaSubPackageVisitor subPackageVisitor = new BallerinaSubPackageVisitor(packagePath, packagePaths,
                    packageMdsMap);
            Files.walkFileTree(packagePath, subPackageVisitor);
        } else {
            packagePaths.add(packagePath);
        }
        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();
        if (!isNative) {
            // This is necessary to be true in order to Ballerina to work properly
            System.setProperty("skipNatives", "true");
        }

        BLangPackage bLangPackage;
        int idx = 0;
        for (Path path : packagePaths) {
            CompilerContext context = new CompilerContext();
            CompilerOptions options = CompilerOptions.getInstance(context);
            options.put(CompilerOptionName.PROJECT_DIR, sourceRoot);
            options.put(CompilerOptionName.COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
            options.put(CompilerOptionName.PRESERVE_WHITESPACE, "false");

            Compiler compiler = Compiler.getInstance(context);

            // TODO: Remove this and the related constants once these are properly handled in the core
            if (BAL_BUILTIN.equals(path) || BAL_BUILTIN_CORE.equals(path)) {
                bLangPackage = loadBuiltInPackage(context);
            } else {
                // compile the given file
//                loadBuiltInPackage(context);
                bLangPackage = compiler.compile(getPackageNameFromPath(path));
            }

            if (bLangPackage == null) {
                out.println(String.format("docerina: invalid Ballerina package: %s", packagePath));
            } else {
                String packageName = bLangPackage.symbol.pkgID.name.value;
                if (isFilteredPackage(packageName, packageFilter)) {
                    if (BallerinaDocUtils.isDebugEnabled()) {
                        out.println("Package " + packageName + " excluded");
                    }
                    continue;
                }
                dataHolder.getPackageMap().put(packageName, new PackageDoc(packageMdsMap.containsKey(packageName) ?
                        packageMdsMap.get(packageName) : null, bLangPackage));
            }
            idx++;
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

    private static String getPackageNameFromPath(Path path) {
        StringJoiner sj = new StringJoiner(".");
        Iterator<Path> pathItr = path.iterator();
        while (pathItr.hasNext()) {
            sj.add(pathItr.next().toString());
        }
        return sj.toString();
    }

    private static BLangPackage loadBuiltInPackage(CompilerContext context) {
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        // Load built-in packages.
        BLangPackage builtInPkg = getBuiltInPackage(context);
        symbolTable.builtInPackageSymbol = builtInPkg.symbol;
        return builtInPkg;
    }

    private static BLangPackage getBuiltInPackage(CompilerContext context) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        return codeAnalyzer.analyze(semAnalyzer.analyze(pkgLoader.loadAndDefinePackage(Names.BUILTIN_ORG.getValue(),
                Names.BUILTIN_PACKAGE.getValue())));
    }

    private static String refinePackagePath(BLangPackage bLangPackage) {
        if (bLangPackage == null) {
            return "";
        }

        if (bLangPackage.getPosition().getSource().getPackageName().equals(".")) {
            return bLangPackage.getPosition().getSource().getCompilationUnitName();
        }
        return bLangPackage.symbol.pkgID.name.value;
    }

    /**
     * Visits sub folders of a ballerina package.
     */
    static class BallerinaSubPackageVisitor extends SimpleFileVisitor<Path> {
        private Path source;
        private List<Path> subPackages;
        private Map<String, Path> packageMdsMap;

        public BallerinaSubPackageVisitor(Path source, List<Path> aList, Map<String, Path> map) {
            this.source = source;
            this.subPackages = aList;
            this.packageMdsMap = map;
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
            } else if (file.toString().endsWith(PACKAGE_CONTENT_FILE)) {
                Path parent = file.getParent();
                if (parent != null) {
                    Path filePath = parent.getFileName();
                    if (filePath != null) {
                        packageMdsMap.putIfAbsent(filePath.toString(), file);
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getSubPackages() {
            return subPackages;
        }
    }
}
