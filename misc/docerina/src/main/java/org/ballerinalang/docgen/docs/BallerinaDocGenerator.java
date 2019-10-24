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
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.Writer;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.generator.model.AnnotationsPageContext;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.ClientPageContext;
import org.ballerinalang.docgen.generator.model.ConstantsPageContext;
import org.ballerinalang.docgen.generator.model.ErrorsPageContext;
import org.ballerinalang.docgen.generator.model.FunctionsPageContext;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.ListenerPageContext;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModulePageContext;
import org.ballerinalang.docgen.generator.model.Object;
import org.ballerinalang.docgen.generator.model.ObjectPageContext;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.docgen.generator.model.ProjectPageContext;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.RecordPageContext;
import org.ballerinalang.docgen.generator.model.TypesPageContext;
import org.ballerinalang.docgen.model.ModuleDoc;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main class to generate a ballerina documentation.
 */
public class BallerinaDocGenerator {

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static PrintStream out = System.out;

    private static final String MODULE_CONTENT_FILE = "Module.md";
    private static final Path BAL_BUILTIN = Paths.get("ballerina", "builtin");
    private static final String HTML = ".html";

    /**
     * API to generate Ballerina API documentation.
     *  @param sourceRoot    project root
     * @param output        path to the output directory where the API documentation will be written to.
     * @param moduleFilter comma separated list of module names to be filtered from the documentation.
     * @param isNative      whether the given modules are native or not.
     * @param offline       is offline generation
     * @param sources       either the path to the directories where Ballerina source files reside or a
     */
    public static void generateApiDocs(String sourceRoot, String output, String moduleFilter, boolean isNative,
                                       boolean offline, String... sources) {
        out.println("docerina: API documentation generation for sources - " + Arrays.toString(sources));

        // generate module docs
        Map<String, ModuleDoc> docsMap = generateModuleDocsMap(sourceRoot, moduleFilter, isNative, sources, offline);

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

        writeAPIDocsForModules(docsMap, output);

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: documentation generation is done.");
        }
    }

    public static void writeAPIDocsForModules(Map<String, ModuleDoc> docsMap, String output) {
        // Sort modules by module path
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        // Module level doc resources
        Map<String, List<Path>> resources = new HashMap<>();

        // Generate project model
        Project project = new Project();
        project.isSingleFile = moduleDocList.size() == 1 &&
                moduleDocList.get(0).bLangPackage.packageID.name.value.equals(".");
        if (project.isSingleFile) {
            project.sourceFileName = moduleDocList.get(0).bLangPackage.packageID.sourceFileName.value;
        }
        project.name = "";
        project.description = "";
        project.version = BallerinaDocDataHolder.getInstance().getVersion();
        project.organization = BallerinaDocDataHolder.getInstance().getOrgName();
        project.modules = moduleDocList.stream().map(moduleDoc -> {

            // Generate module models
            Module module = new Module();
            module.id = moduleDoc.bLangPackage.packageID.name.toString();
            module.summary = moduleDoc.summary;
            module.description = moduleDoc.description;

            // populate module constructs
            sortModuleConstructs(moduleDoc.bLangPackage);
            Generator.generateModuleConstructs(module, moduleDoc.bLangPackage);

            // collect module's doc resources
            resources.put(module.id, moduleDoc.resources);

            return module;
        }).collect(Collectors.toList());

        // Generate index.html for the project
        String projectTemplateName = System.getProperty(BallerinaDocConstants.PROJECT_TEMPLATE_NAME_KEY, "index");
        String indexFilePath = output + File.separator + "index" + HTML;
        ProjectPageContext projectPageContext = new ProjectPageContext(project, "API Documentation", "");
        try {
            Writer.writeHtmlDocument(projectPageContext, projectTemplateName, indexFilePath);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the index.html. Cause: %s", e.getMessage()));
            log.error("Failed to create the index.html file.", e);
        }

        String moduleTemplateName = System.getProperty(BallerinaDocConstants.MODULE_TEMPLATE_NAME_KEY, "module");
        String recordTemplateName = System.getProperty(BallerinaDocConstants.RECORD_TEMPLATE_NAME_KEY, "record");
        String objectTemplateName = System.getProperty(BallerinaDocConstants.OBJECT_TEMPLATE_NAME_KEY, "object");
        String clientTemplateName = System.getProperty(BallerinaDocConstants.CLIENT_TEMPLATE_NAME_KEY, "client");
        String listenerTemplateName = System.getProperty(BallerinaDocConstants.LISTENER_TEMPLATE_NAME_KEY,
                "listener");
        String functionsTemplateName = System.getProperty(BallerinaDocConstants.FUNCTIONS_TEMPLATE_NAME_KEY,
                "functions");
        String constantsTemplateName = System.getProperty(BallerinaDocConstants.CONSTANTS_TEMPLATE_NAME_KEY,
                "constants");
        String typesTemplateName = System.getProperty(BallerinaDocConstants.TYPES_TEMPLATE_NAME_KEY, "types");
        String annotationsTemplateName = System.getProperty(BallerinaDocConstants.ANNOTATIONS_TEMPLATE_NAME_KEY,
                "annotations");
        String errorsTemplateName = System.getProperty(BallerinaDocConstants.ERRORS_TEMPLATE_NAME_KEY, "errors");

        String rootPathModuleLevel = project.isSingleFile ? "./" : "../";
        String rootPathConstructLevel = project.isSingleFile ? "../" : "../../";
        // Generate module pages
        for (Module module : project.modules) {
            try {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: starting to generate docs for module: " + module.id);
                }

                // Create module directory
                String modDir = output + File.separator + module.id;
                Files.createDirectories(Paths.get(modDir));

                // Create module index page
                ModulePageContext modulePageContext = new ModulePageContext(module, project,
                        rootPathModuleLevel,
                        "API Docs - " + (project.isSingleFile ? project.sourceFileName
                                : project.organization + "/" + module.id));
                String modIndexPath = modDir + File.separator + "index" + HTML;
                Writer.writeHtmlDocument(modulePageContext, moduleTemplateName, modIndexPath);

                // Create pages for records
                if (!module.records.isEmpty()) {
                    String recordsDir = modDir + File.separator + "records";
                    Files.createDirectories(Paths.get(recordsDir));
                    for (Record record : module.records) {
                        RecordPageContext recordPageContext = new RecordPageContext(record, module, project,
                                rootPathConstructLevel, "API Docs - Record : " + record.name);
                        String recordFilePath = recordsDir + File.separator + record.name + HTML;
                        Writer.writeHtmlDocument(recordPageContext, recordTemplateName, recordFilePath);
                    }
                }

                // Create pages for objects
                if (!module.objects.isEmpty()) {
                    String objectsDir = modDir + File.separator + "objects";
                    Files.createDirectories(Paths.get(objectsDir));
                    for (Object object : module.objects) {
                        ObjectPageContext objectPageContext = new ObjectPageContext(object, module, project,
                                rootPathConstructLevel, "API Docs - Object : " + object.name);
                        String objectFilePath = objectsDir + File.separator + object.name + HTML;
                        Writer.writeHtmlDocument(objectPageContext, objectTemplateName, objectFilePath);
                    }
                }

                // Create pages for clients
                if (!module.clients.isEmpty()) {
                    String clientsDir = modDir + File.separator + "clients";
                    Files.createDirectories(Paths.get(clientsDir));
                    for (Client client : module.clients) {
                        ClientPageContext clientPageContext = new ClientPageContext(client, module, project,
                                rootPathConstructLevel, "API Docs - Client : " + client.name);
                        String clientFilePath = clientsDir + File.separator + client.name + HTML;
                        Writer.writeHtmlDocument(clientPageContext, clientTemplateName, clientFilePath);
                    }
                }

                // Create pages for listeners
                if (!module.listeners.isEmpty()) {
                    String listenersDir = modDir + File.separator + "listeners";
                    Files.createDirectories(Paths.get(listenersDir));
                    for (Listener listener : module.listeners) {
                        ListenerPageContext listenerPageContext = new ListenerPageContext(listener, module, project,
                                rootPathConstructLevel, "API Docs - Listener : " + listener.name);
                        String listenerFilePath = listenersDir + File.separator + listener.name + HTML;
                        Writer.writeHtmlDocument(listenerPageContext, listenerTemplateName, listenerFilePath);
                    }
                }

                // Create pages for functions
                if (!module.functions.isEmpty()) {
                    String functionsFile = modDir + File.separator + "functions" + HTML;
                    FunctionsPageContext functionsPageContext = new FunctionsPageContext(module.functions,
                            module, project, rootPathModuleLevel, "API Docs - Functions : " + module.id);
                    Writer.writeHtmlDocument(functionsPageContext, functionsTemplateName, functionsFile);
                }

                // Create pages for constants
                if (!module.constants.isEmpty()) {
                    String constantsFile = modDir + File.separator + "constants" + HTML;
                    ConstantsPageContext constantsPageContext = new ConstantsPageContext(module.constants,
                            module, project, rootPathModuleLevel, "API Docs - Constants : " + module.id);
                    Writer.writeHtmlDocument(constantsPageContext, constantsTemplateName, constantsFile);
                }

                // Create pages for types
                if (!(module.unionTypes.isEmpty() && module.finiteTypes.isEmpty())) {
                    String typesFile = modDir + File.separator + "types" + HTML;
                    TypesPageContext typesPageContext = new TypesPageContext(module.unionTypes, module, project,
                            rootPathModuleLevel, "API Docs - Types : " + module.id);
                    Writer.writeHtmlDocument(typesPageContext, typesTemplateName, typesFile);
                }

                // Create pages for annotations
                if (!module.annotations.isEmpty()) {
                    String annotationsFile = modDir + File.separator + "annotations" + HTML;
                    AnnotationsPageContext annotationsPageContext = new AnnotationsPageContext(module.annotations,
                            module, project, rootPathModuleLevel, "API Docs - Annotations : " + module.id);
                    Writer.writeHtmlDocument(annotationsPageContext, annotationsTemplateName, annotationsFile);
                }

                // Create pages for errors
                if (!module.errors.isEmpty()) {
                    String errorsFile = modDir + File.separator + "errors" + HTML;
                    ErrorsPageContext errorsPageContext = new ErrorsPageContext(module.errors, module, project,
                            rootPathModuleLevel, "API Docs - Errors : " + module.id);
                    Writer.writeHtmlDocument(errorsPageContext, errorsTemplateName, errorsFile);
                }

                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: generated docs for module: " + module.id);
                }
            } catch (IOException e) {
                out.println(String.format("docerina: API documentation generation failed for module %s: %s",
                        module.id, e.getMessage()));
                log.error(String.format("API documentation generation failed for %s", module.id), e);
            }
        }

        // Copy template resources to output dir
        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: copying HTML theme into " + output);
        }
        try {
            BallerinaDocUtils.copyResources("html-template-resources", output);
            BallerinaDocUtils.copyResources("syntax-highlighter", output);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to copy the docerina-theme resource. Cause: %s", e.getMessage
                    ()));
            log.error("Failed to coxpy the docerina-theme resource.", e);
        }
        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: successfully copied HTML theme into " + output);
        }

        if (!resources.isEmpty()) {
            String resourcesDir = output + File.separator + "resources";
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: copying project resources ");
            }
            for (Map.Entry<String, List<Path>> resourceSet : resources.entrySet()) {
                File resourcesDirFile = new File(output + File.separator + resourceSet.getKey()
                        + File.separator + "resources");
                resourceSet.getValue().forEach(resource -> {
                    try {
                        FileUtils.copyFileToDirectory(resource.toFile(), resourcesDirFile);
                    } catch (IOException e) {
                        out.println(String.format("docerina: failed to copy [resource] %s into [resources directory] " +
                                "%s. Cause: %s", resource.toString(), resourcesDirFile.toString(), e.getMessage()));
                        log.error(String.format("docerina: failed to copy [resource] %s into [resources directory] " +
                                "%s. Cause: %s", resource.toString(), resourcesDirFile.toString(), e.getMessage()), e);
                    }
                });
            }
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: successfully copied project resources into " + resourcesDir);
            }
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
    }

    public static Map<String, ModuleDoc> generateModuleDocsFromBLangPackages(String sourceRoot,
                                                                     List<BLangPackage> modules) throws IOException {
        Map<String, ModuleDoc> moduleDocMap = new HashMap<>();
        for (BLangPackage bLangPackage : modules) {
            String moduleName = bLangPackage.packageID.name.toString();
            Path absolutePkgPath = getAbsoluteModulePath(sourceRoot, Paths.get(moduleName));

            // find the Module.md file
            Path packageMd = getModuleDocPath(absolutePkgPath);

            // find the resources of the package
            List<Path> resources = getResourcePaths(absolutePkgPath);

            moduleDocMap.put(moduleName,
                    new ModuleDoc(packageMd == null ? null : packageMd.toAbsolutePath(), resources, bLangPackage));
        }
        return moduleDocMap;
    }

    public static void setPrintStream(PrintStream out) {
        BallerinaDocGenerator.out = out;
    }

    private static void sortModuleConstructs(BLangPackage bLangPackage) {
        bLangPackage.getFunctions().sort(Comparator.comparing(f -> (f.getReceiver() == null ? "" : f
                .getReceiver().getName()) + f.getName().getValue()));
        bLangPackage.getAnnotations().sort(Comparator.comparing(a -> a.getName().getValue()));
        bLangPackage.getTypeDefinitions().sort(Comparator.comparing(a -> a.getName().getValue()));
        bLangPackage.getGlobalVariables().sort(Comparator.comparing(a -> a.getName().getValue()));
    }

    private static Map<String, ModuleDoc> generateModuleDocsMap(String sourceRoot, String packageFilter, boolean
            isNative, String[] sources, boolean offline) {
        for (String source : sources) {
            source = source.trim();
            try {
                generatePackageDocsFromBallerina(sourceRoot, source, packageFilter, isNative, offline);

            } catch (Exception e) {
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
    protected static Map<String, ModuleDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath)
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
    protected static Map<String, ModuleDoc> generatePackageDocsFromBallerina(String sourceRoot, String packagePath,
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
    protected static Map<String, ModuleDoc> generatePackageDocsFromBallerina(
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
    protected static Map<String, ModuleDoc> generatePackageDocsFromBallerina(
            String sourceRoot, Path packagePath, String packageFilter, boolean isNative, boolean offline)
            throws IOException {

        Path absolutePkgPath = getAbsoluteModulePath(sourceRoot, packagePath);

        // find the Module.md file
        Path packageMd = getModuleDocPath(absolutePkgPath);

        // find the resources of the package
        List<Path> resources = getResourcePaths(absolutePkgPath);

        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();
        if (!isNative) {
            // This is necessary to be true in order to Ballerina to work properly
            System.setProperty("skipNatives", "true");
        }

        BLangPackage bLangPackage = null;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(CompilerOptionName.PROJECT_DIR, sourceRoot);
        options.put(CompilerOptionName.COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());
        options.put(CompilerOptionName.PRESERVE_WHITESPACE, "false");
        options.put(CompilerOptionName.OFFLINE, Boolean.valueOf(offline).toString());
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        context.put(SourceDirectory.class, new FileSystemProjectDirectory(Paths.get(sourceRoot)));

        Compiler compiler = Compiler.getInstance(context);

        // TODO: Remove this and the related constants once these are properly handled in the core
        if (!absolutePkgPath.endsWith(BAL_BUILTIN.toString())) {
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
                dataHolder.getPackageMap().put(packageName, new ModuleDoc(packageMd == null ? null : packageMd
                        .toAbsolutePath(), resources, bLangPackage));
            }
        }
        return dataHolder.getPackageMap();
    }

    private static List<Path> getResourcePaths(Path absolutePkgPath) throws IOException {
        Path resourcesDirPath = absolutePkgPath.resolve("resources");
        List<Path> resources = new ArrayList<>();
        if (resourcesDirPath.toFile().exists()) {
            resources = Files.walk(resourcesDirPath).filter(path -> !path.equals(resourcesDirPath)).collect(Collectors
                    .toList());
        }
        return resources;
    }

    private static Path getModuleDocPath(Path absolutePkgPath) throws IOException {
        Path packageMd;
        Optional<Path> o = Files.find(absolutePkgPath, 1, (path, attr) -> {
            Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString().equals(MODULE_CONTENT_FILE);
            }
            return false;
        }).findFirst();

        packageMd = o.isPresent() ? o.get() : null;
        return packageMd;
    }

    private static Path getAbsoluteModulePath(String sourceRoot, Path modulePath) {
        return Paths.get(sourceRoot).resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                               .resolve(modulePath);
    }

    private static String packageNameToString(PackageID pkgId) {
        String pkgName = pkgId.getName().getValue();
        return ".".equals(pkgName) ? pkgId.sourceFileName.getValue() : pkgName;
    }

    private static boolean isFilteredPackage(String packageName, String packageFilter) {
        if ((packageFilter != null) && (packageFilter.trim().length() > 0)) {
            return Arrays.stream(packageFilter.split(","))
                    .anyMatch(e -> packageName.startsWith(e.replace(".*", "")));
        }
        return false;
    }
}
