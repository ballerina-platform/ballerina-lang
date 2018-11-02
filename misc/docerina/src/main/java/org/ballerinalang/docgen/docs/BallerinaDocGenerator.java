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

import org.apache.commons.io.FileUtils;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.Writer;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.model.Caption;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.PackageDoc;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.SourceDirectory;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main class to generate a ballerina documentation.
 */
public class BallerinaDocGenerator {

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static final PrintStream out = System.out;

    private static final String BSOURCE_FILE_EXT = ".bal";
    private static final String MODULE_CONTENT_FILE = "Module.md";
    private static final Path BAL_BUILTIN = Paths.get("ballerina", "builtin");
    private static final String HTML = ".html";

    /**
     * API to generate Ballerina API documentation.
     *  @param sourceRoot    project root
     * @param output        path to the output directory where the API documentation will be written to.
     * @param packageFilter comma separated list of package names to be filtered from the documentation.
     * @param isNative      whether the given packages are native or not.
     * @param offline       is offline generation
     * @param sources       either the path to the directories where Ballerina source files reside or a
     */
    public static void generateApiDocs(String sourceRoot, String output, String packageFilter, boolean isNative,
                                       boolean offline, String... sources) {
        out.println("docerina: API documentation generation for sources - " + Arrays.toString(sources));
        List<Link> primitives = primitives();

        // generate package docs
        Map<String, PackageDoc> docsMap = generatePackageDocsMap(sourceRoot, packageFilter, isNative, sources, offline);

        if (docsMap.size() == 0) {
            out.println("docerina: no module definitions found!");
            return;
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: generating HTML API documentation...");
        }

        // validate output path
        String userDir = System.getProperty("user.dir");
        // If output directory is empty
        if (output == null) {
            output = System.getProperty(BallerinaDocConstants.HTML_OUTPUT_PATH_KEY, userDir + File.separator +
                    ProjectDirConstants.TARGET_DIR_NAME + File.separator + "api-docs");
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: creating output directory: " + output);
        }

        try {
            // Create output directory
            Files.createDirectories(Paths.get(output));
        } catch (IOException e) {
            out.println(String.format("docerina: API documentation generation failed. Couldn't create the [output " +
                    "directory] %s. Cause: %s", output, e.getMessage()));
            log.error(String.format("API documentation generation failed. Couldn't create the [output directory] %s. " +
                    "" + "" + "Cause: %s", output, e.getMessage()), e);
            return;
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: successfully created the output directory: " + output);
        }

        // Sort packages by package path
        List<PackageDoc> packageList = new ArrayList<>(docsMap.values());
        packageList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        // Sort the package names
        List<String> packageNames = new ArrayList<>(docsMap.keySet());
        Collections.sort(packageNames);

        List<Link> packageNameList = PackageName.convertList(packageNames);

        String packageTemplateName = System.getProperty(BallerinaDocConstants.MODULE_TEMPLATE_NAME_KEY, "page");
        String packageToCTemplateName = System.getProperty(BallerinaDocConstants.MODULE_TOC_TEMPLATE_NAME_KEY, "toc");

        List<Path> resources = new ArrayList<>();

        //Iterate over the packages to generate the pages
        for (PackageDoc packageDoc : packageList) {

            try {
                BLangPackage bLangPackage = packageDoc.bLangPackage;
                String pkgDescription = packageDoc.description;

                // Sort functions, connectors, structs, type mappers and annotationDefs
                sortPackageConstructs(bLangPackage);

                String packagePath = refinePackagePath(bLangPackage);
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: starting to generate docs for module: " + packagePath);
                }

                // other normal packages
                Page page = Generator.generatePage(bLangPackage, packageNameList, pkgDescription, primitives);
                String filePath = output + File.separator + packagePath + HTML;
                Writer.writeHtmlDocument(page, packageTemplateName, filePath);

                if (ConfigRegistry.getInstance().getAsBoolean(BallerinaDocConstants.GENERATE_TOC)) {
                    // generates ToC into a separate HTML - requirement of Central
                    out.println("docerina: generating toc: " + output + File.separator + packagePath + "-toc" + HTML);
                    String tocFilePath = output + File.separator + packagePath + "-toc" + HTML;
                    Writer.writeHtmlDocument(page, packageToCTemplateName, tocFilePath);
                }

                if (Names.BUILTIN_PACKAGE.getValue().equals(packagePath)) {
                    // primitives are in builtin package
                    Page primitivesPage = Generator.generatePageForPrimitives(bLangPackage, packageNameList,
                            primitives);
                    String primitivesFilePath = output + File.separator + "primitive-types" + HTML;
                    Writer.writeHtmlDocument(primitivesPage, packageTemplateName, primitivesFilePath);
                }

                // collect package resources
                resources.addAll(packageDoc.resources);

                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: generated docs for module: " + packagePath);
                }
            } catch (IOException e) {
                out.println(String.format("docerina: API documentation generation failed for module %s: %s",
                        packageDoc.bLangPackage.packageID.toString(), e.getMessage()));
                log.error(String.format("API documentation generation failed for %s", packageDoc.bLangPackage
                        .packageID.toString()), e);
            }
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: copying HTML theme into " + output);
        }
        try {
            BallerinaDocUtils.copyResources("docerina-theme", output);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to copy the docerina-theme resource. Cause: %s", e.getMessage
                    ()));
            log.error("Failed to copy the docerina-theme resource.", e);
        }
        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: successfully copied HTML theme into " + output);
        }

        if (!resources.isEmpty()) {
            String resourcesDir = output + File.separator + "resources";
            File resourcesDirFile = new File(resourcesDir);
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: copying project resources into " + resourcesDir);
            }
            resources.parallelStream().forEach(path -> {
                try {
                    FileUtils.copyFileToDirectory(path.toFile(), resourcesDirFile);
                } catch (IOException e) {
                    out.println(String.format("docerina: failed to copy [resource] %s into [resources directory] " +
                            "%s. Cause: %s", path.toString(), resourcesDir, e.getMessage()));
                    log.error(String.format("docerina: failed to copy [resource] %s into [resources directory] " +
                            "%s. Cause: %s", path.toString(), resourcesDir, e.getMessage()), e);
                }
            });
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: successfully copied project resources into " + resourcesDir);
            }
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: generating the index HTML file.");
        }

        try {
            //Generate the index file with the list of all modules
            String indexTemplateName = System.getProperty(BallerinaDocConstants.MODULE_TEMPLATE_NAME_KEY, "index");
            String indexFilePath = output + File.separator + "index" + HTML;
            Writer.writeHtmlDocument(packageNameList, indexTemplateName, indexFilePath);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the index.html. Cause: %s", e.getMessage()));
            log.error("Failed to create the index.html file.", e);
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: successfully generated the index HTML file.");
            out.println("docerina: generating the module-list HTML file.");
        }

        try {
            // Generate module-list.html file which prints the list of processed packages
            String pkgListTemplateName = System.getProperty(BallerinaDocConstants.MODULE_LIST_TEMPLATE_NAME_KEY,
                    "module-list");

            String pkgListFilePath = output + File.separator + "module-list" + HTML;
            Writer.writeHtmlDocument(packageNameList, pkgListTemplateName, pkgListFilePath);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the module-list.html. Cause: %s", e.getMessage()));
            log.error("Failed to create the module-list.html file.", e);
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: successfully generated the module-list HTML file.");
        }

        try {
            String zipPath = System.getProperty(BallerinaDocConstants.OUTPUT_ZIP_PATH);
            if (zipPath != null) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: generating the documentation zip file.");
                }
                BallerinaDocUtils.packageToZipFile(output, zipPath);
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: successfully generated the documentation zip file.");
                }
            }
        } catch (IOException e) {
            out.println(String.format("docerina: API documentation zip packaging failed for %s: %s", output, e
                    .getMessage()));
            log.error(String.format("API documentation zip packaging failed for %s", output), e);
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: documentation generation is done.");
        }
    }

    private static void sortPackageConstructs(BLangPackage bLangPackage) {
        bLangPackage.getFunctions().sort(Comparator.comparing(f -> (f.getReceiver() == null ? "" : f
                .getReceiver().getName()) + f.getName().getValue()));
        bLangPackage.getAnnotations().sort(Comparator.comparing(a -> a.getName().getValue()));
        bLangPackage.getTypeDefinitions().sort(Comparator.comparing(a -> a.getName().getValue()));
        bLangPackage.getGlobalVariables().sort(Comparator.comparing(a -> a.getName().getValue()));
    }

    private static Map<String, PackageDoc> generatePackageDocsMap(String sourceRoot, String packageFilter, boolean
            isNative, String[] sources, boolean offline) {
        for (String source : sources) {
            source = source.trim();
            try {
                generatePackageDocsFromBallerina(sourceRoot, source, packageFilter, isNative, offline);

            } catch (IOException e) {
                out.println(String.format("docerina: API documentation generation failed for %s: %s", source, e
                        .getMessage()));
                log.error(String.format("API documentation generation failed for %s", source), e);
                // we continue, as there may be other valid packages.
                continue;
            }
        }
        return BallerinaDocDataHolder.getInstance().getPackageMap();
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot  points to the folder relative to which package path is given
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     * @throws IOException on error.
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath)
            throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, packagePath, null, true);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @param offline is offline generation
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     * @throws IOException on error.
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath,
                                                                              String packageFilter, boolean offline)
            throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, packagePath, packageFilter, false, offline);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @param isNative      whether this is a native package or not.
     * @param offline is offline generation
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     * @throws IOException on error.
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(
            String sourceRoot, String packagePath, String packageFilter, boolean isNative, boolean offline)
            throws IOException {
        return generatePackageDocsFromBallerina(sourceRoot, Paths.get(packagePath), packageFilter, isNative, offline);
    }

    /**
     * Generates {@link BLangPackage} objects for each Ballerina package from the given ballerina files.
     *
     * @param sourceRoot    points to the folder relative to which package path is given
     * @param packagePath   a {@link Path} object pointing either to a ballerina file or a folder with ballerina files.
     * @param packageFilter comma separated list of package names/patterns to be filtered from the documentation.
     * @param isNative      whether the given packages are native or not.
     * @param offline is offline generation
     * @return a map of {@link BLangPackage} objects. Key - Ballerina package name Value - {@link BLangPackage}
     * @throws IOException on error.
     */
    protected static Map<String, PackageDoc> generatePackageDocsFromBallerina(
            String sourceRoot, Path packagePath, String packageFilter, boolean isNative, boolean offline)
            throws IOException {

        // find the Module.md file
        Path packageMd;
        Path absolutePkgPath = Paths.get(sourceRoot).resolve(packagePath);
        Optional<Path> o = Files.find(absolutePkgPath, 1, (path, attr) -> {
            Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString().equals(MODULE_CONTENT_FILE);
            }
            return false;
        }).findFirst();

        packageMd = o.isPresent() ? o.get() : null;

        // find the resources of the package
        Path resourcesDirPath = absolutePkgPath.resolve("resources");
        List<Path> resources = new ArrayList<>();
        if (resourcesDirPath.toFile().exists()) {
            resources = Files.walk(resourcesDirPath).filter(path -> !path.equals(resourcesDirPath)).collect(Collectors
                    .toList());
        }

        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();
        if (!isNative) {
            // This is necessary to be true in order to Ballerina to work properly
            System.setProperty("skipNatives", "true");
        }

        BLangPackage bLangPackage;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(CompilerOptionName.PROJECT_DIR, sourceRoot);
        options.put(CompilerOptionName.COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());
        options.put(CompilerOptionName.PRESERVE_WHITESPACE, "false");
        options.put(CompilerOptionName.OFFLINE, Boolean.valueOf(offline).toString());
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(Paths.get(sourceRoot)));

        Compiler compiler = Compiler.getInstance(context);

        // TODO: Remove this and the related constants once these are properly handled in the core
        if (absolutePkgPath.endsWith(BAL_BUILTIN.toString())) {
            bLangPackage = loadBuiltInPackage(context);
        } else {
            // compile the given package
            Path fileOrPackageName = packagePath.getFileName();
            bLangPackage = compiler.compile(fileOrPackageName == null ? packagePath.toString() : fileOrPackageName
                    .toString());
        }

        if (bLangPackage == null) {
            out.println(String.format("docerina: invalid Ballerina module: %s", packagePath));
        } else {
            String packageName = packageNameToString(bLangPackage.packageID);
            if (isFilteredPackage(packageName, packageFilter)) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: module " + packageName + " excluded");
                }
            } else {
                dataHolder.getPackageMap().put(packageName, new PackageDoc(packageMd == null ? null : packageMd
                        .toAbsolutePath(), resources, bLangPackage));
            }
        }
        return dataHolder.getPackageMap();
    }

    private static String packageNameToString(PackageID pkgId) {
        String pkgName = pkgId.getName().getValue();
        return ".".equals(pkgName) ? pkgId.sourceFileName.getValue() : pkgName;
    }

    private static boolean isFilteredPackage(String packageName, String packageFilter) {
        if ((packageFilter != null) && (packageFilter.trim().length() > 0)) {
            return Arrays.asList(packageFilter.split(",")).stream()
                    .filter(e -> packageName.startsWith(e.replace(".*", ""))).findAny().isPresent();
        }
        return false;
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
                Names.BUILTIN_PACKAGE.getValue(), Names.EMPTY.getValue())));
    }

    private static List<Link> primitives() {
        List<String> primitives = BallerinaDocUtils.loadPrimitivesDescriptions(true);
        List<Link> primitiveLinks = new ArrayList<>();
        for (String type : primitives) {
            primitiveLinks.add(new Link(new Caption(type), BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF.concat(""
                    + ".html#" + type), true));
        }
        return primitiveLinks;
    }

    private static String refinePackagePath(BLangPackage bLangPackage) {
        if (bLangPackage == null) {
            return "";
        }

        if (bLangPackage.getPosition().getSource().getPackageName().equals(".")) {
            return bLangPackage.getPosition().getSource().pkgID.sourceFileName.getValue();
        }
        return bLangPackage.packageID.getName().getValue();
    }

}
