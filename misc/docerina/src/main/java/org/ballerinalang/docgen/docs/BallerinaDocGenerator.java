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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
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
import org.ballerinalang.docgen.model.search.ConstructSearchJson;
import org.ballerinalang.docgen.model.search.ModuleSearchJson;
import org.ballerinalang.docgen.model.search.SearchJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
import java.util.Set;
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
    private static final String JSON = ".json";
    private static final String MODULE_SEARCH = "search";
    private static final String SEARCH_DATA = "search-data.js";
    private static final String SEARCH_DIR = "doc-search";
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * API to merge multiple api docs.
     *  @param apiDocsRoot api doc root
     */
    public static void mergeApiDocs(String apiDocsRoot)  {
        out.println("docerina: API documentation generation for doc path - " + apiDocsRoot);
        File directory = new File(apiDocsRoot);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList == null) {
            String errorMsg = String.format("docerina: API documentation generation failed. Could not find any module" +
                                                    " in given path %s", apiDocsRoot);
            out.println(errorMsg);
            log.error(errorMsg);
            return;
        }
        Arrays.sort(fList);
        List<Module> moduleList = new ArrayList<>(fList.length);
        for (File file : fList) {
            if (file.isDirectory()) {
                Path moduleJsonPath = Paths.get(file.getAbsolutePath(), file.getName() + JSON);
                if (moduleJsonPath.toFile().exists()) {
                    try (BufferedReader br = Files.newBufferedReader(moduleJsonPath, StandardCharsets.UTF_8)) {
                        Module module = gson.fromJson(br, Module.class);
                        moduleList.add(module);
                    } catch (IOException e) {
                        String errorMsg = String.format("API documentation generation failed. Cause: %s",
                                                        e.getMessage());
                        out.println(errorMsg);
                        log.error(errorMsg, e);
                        return;
                    }
                }
            }
        }
        mergeSearchJsons(apiDocsRoot);
        Project project = new Project();
        project.modules = moduleList;
        String projectTemplateName = System.getProperty(BallerinaDocConstants.PROJECT_TEMPLATE_NAME_KEY, "index");
        String indexHtmlPath = apiDocsRoot + File.separator  + projectTemplateName + HTML;
        ProjectPageContext projectPageContext = new ProjectPageContext(project, "API Documentation", "");
        // Generate index.html for the project
        try {
            Writer.writeHtmlDocument(projectPageContext, projectTemplateName, indexHtmlPath);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the index.html. Cause: %s", e.getMessage()));
            log.error("Failed to create the index.html file.", e);
        }
    }

    public static void writeAPIDocsForModules(Map<String, ModuleDoc> docsMap, String output) {
        // Sort modules by module path
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        // Module level doc resources
        Map<String, List<Path>> resources = new HashMap<>();

        // Generate project model
        Project project = getDocsGenModel(moduleDocList, resources);

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
        if (project.modules == null) {
            String errMessage =
                    "docerina: API documentation generation failed. Couldn't create the [output directory] " + output;
            out.println(errMessage);
            log.error(errMessage);
            return;
        }
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
                                : module.orgName + "/" + module.id));
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
                // Create module json
                genModuleJson(module, modDir + File.separator + module.id + JSON);
                // Create search json
                genSearchJson(module, modDir + File.separator + MODULE_SEARCH + JSON);

                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: generated docs for module: " + module.id);
                }
            } catch (IOException e) {
                out.println(String.format("docerina: API documentation generation failed for module %s: %s",
                        module.id, e.getMessage()));
                log.error(String.format("API documentation generation failed for %s", module.id), e);
            }
        }

        // Generate index.html for the project
        genIndexHtml(output, project);
        // Merge search JSONS of modules
        mergeSearchJsons(output);
        // Copy template resources to output dir
        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("docerina: copying HTML theme into " + output);
        }
        try {
            BallerinaDocUtils.copyResources("html-template-resources", output);
            BallerinaDocUtils.copyResources("syntax-highlighter", output);
            BallerinaDocUtils.copyResources("doc-search", output);
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

    private static void genIndexHtml(String output, Project project) {
        String projectTemplateName = System.getProperty(BallerinaDocConstants.PROJECT_TEMPLATE_NAME_KEY, "index");
        String indexHtmlPath = output + File.separator  + projectTemplateName + HTML;
        ProjectPageContext projectPageContext = new ProjectPageContext(project, "API Documentation", "");
        // Generate index.html for the project
        try {
            Writer.writeHtmlDocument(projectPageContext, projectTemplateName, indexHtmlPath);
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the index.html. Cause: %s", e.getMessage()));
            log.error("Failed to create the index.html file.", e);
        }
    }

    private static void genModuleJson(Module module, String moduleJsonPath) {
        File jsonFile = new File(moduleJsonPath);
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            String json = gson.toJson(module);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the module.json. Cause: %s", e.getMessage()));
            log.error("Failed to create module.json file.", e);
        }
    }

    private static void genSearchJson(Module module, String jsonPath) {
        List<ModuleSearchJson> searchModules = new ArrayList<>();
        List<ConstructSearchJson> searchFunctions = new ArrayList<>();
        List<ConstructSearchJson> searchObjects = new ArrayList<>();
        List<ConstructSearchJson> searchRecords = new ArrayList<>();
        List<ConstructSearchJson> searchConstants = new ArrayList<>();
        List<ConstructSearchJson> searchErrors = new ArrayList<>();
        List<ConstructSearchJson> searchTypes = new ArrayList<>();

        if (module.summary != null) {
            searchModules.add(new ModuleSearchJson(module.id, getFirstLine(module.summary)));
        }
        module.functions.forEach((function) ->
                searchFunctions.add(new ConstructSearchJson(function.name, module.id,
                        getFirstLine(function.description))));

        module.objects.forEach((object) ->
                searchObjects.add(new ConstructSearchJson(object.name, module.id, getFirstLine(object.description))));

        module.records.forEach((record) ->
                searchRecords.add(new ConstructSearchJson(record.name, module.id, getFirstLine(record.description))));

        module.constants.forEach((constant) ->
                searchConstants.add(new ConstructSearchJson(constant.name, module.id,
                        getFirstLine(constant.description))));

        module.errors.forEach((error) ->
                searchErrors.add(new ConstructSearchJson(error.name, module.id, getFirstLine(error.description))));

        module.unionTypes.forEach((unionType) ->
                searchTypes.add(new ConstructSearchJson(unionType.name, module.id,
                        getFirstLine(unionType.description))));


        SearchJson searchJson = new SearchJson(searchModules, searchObjects, searchFunctions, searchRecords,
                searchConstants, searchErrors, searchTypes);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File jsonFile = new File(jsonPath);
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            String json = gson.toJson(searchJson);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the search.json. Cause: %s", e.getMessage()));
            log.error("Failed to create search.json file.", e);
        }

    }

    private static String getFirstLine(String description) {
        String[] splits = description.split("\\.", 2);
        if (splits.length < 2) {
            return splits[0];
        } else {
            if (splits[0].contains("<p>")) {
                return splits[0] + ".</p>";
            }
            return splits[0] + ".";
        }
    }

    private static void mergeSearchJsons(String docRoot) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File directory = new File(docRoot);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList != null) {
            Arrays.sort(fList);

            SearchJson searchJson = new SearchJson();
            for (File file : fList) {
                if (file.isDirectory()) {
                    Path moduleJsonPath = Paths.get(file.getAbsolutePath(), MODULE_SEARCH + JSON);
                    if (moduleJsonPath.toFile().exists()) {
                        try (BufferedReader br = Files.newBufferedReader(moduleJsonPath, StandardCharsets.UTF_8)) {
                            SearchJson modSearchJson = gson.fromJson(br, SearchJson.class);
                            searchJson.getModules().addAll(modSearchJson.getModules());
                            searchJson.getFunctions().addAll(modSearchJson.getFunctions());
                            searchJson.getObjects().addAll(modSearchJson.getObjects());
                            searchJson.getRecords().addAll(modSearchJson.getRecords());
                            searchJson.getConstants().addAll(modSearchJson.getConstants());
                            searchJson.getErrors().addAll(modSearchJson.getErrors());
                            searchJson.getTypes().addAll(modSearchJson.getTypes());
                        } catch (IOException e) {
                            String errorMsg = String.format("API documentation generation failed. Cause: %s",
                                    e.getMessage());
                            out.println(errorMsg);
                            log.error(errorMsg, e);
                            return;
                        }
                    }
                }
            }
            File docSearchDir = new File(docRoot + File.separator + SEARCH_DIR);
            boolean docSearchDirExists = docSearchDir.exists() || docSearchDir.mkdir();
            if (!docSearchDirExists) {
                out.println("docerina: failed to create " + SEARCH_DIR + " directory");
                log.error("Failed to create " + SEARCH_DIR + " directory.");
            }
            File jsonFile = new File(docRoot + File.separator + SEARCH_DIR + File.separator + SEARCH_DATA);
            try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                    StandardCharsets.UTF_8)) {
                String json = gson.toJson(searchJson);
                String js = "var searchData = " + json + ";";
                writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                out.println(String.format("docerina: failed to create the " + SEARCH_DATA + ". Cause: %s",
                        e.getMessage()));
                log.error("Failed to create " + SEARCH_DATA + " file.", e);
            }
        }
    }

    public static Map<String, ModuleDoc> generateModuleDocs(String sourceRoot,
                                                            List<BLangPackage> modules) throws IOException {
        Map<String, ModuleDoc> moduleDocMap = new HashMap<>();
        for (BLangPackage bLangPackage : modules) {
            moduleDocMap.put(bLangPackage.packageID.name.toString(), generateModuleDoc(sourceRoot, bLangPackage));
        }
        return moduleDocMap;
    }

    public static Map<String, ModuleDoc> generateModuleDocs(String sourceRoot, List<BLangPackage> modules,
                                                            Set<String> moduleFilter) throws IOException {
        Map<String, ModuleDoc> moduleDocMap = new HashMap<>();
        for (BLangPackage bLangPackage : modules) {
            String moduleName = bLangPackage.packageID.name.toString();
            if (moduleFilter.contains(moduleName)) {
                continue;
            }
            moduleDocMap.put(moduleName, generateModuleDoc(sourceRoot, bLangPackage));
        }
        return moduleDocMap;
    }

    public static ModuleDoc generateModuleDoc(String sourceRoot, BLangPackage bLangPackage) throws IOException {
        String moduleName = bLangPackage.packageID.name.toString();
        Path absolutePkgPath = getAbsoluteModulePath(sourceRoot, Paths.get(moduleName));
        // find the Module.md file
        Path packageMd = getModuleDocPath(absolutePkgPath);
        // find the resources of the package
        List<Path> resources = getResourcePaths(absolutePkgPath);
        return new ModuleDoc(packageMd == null ? null : packageMd.toAbsolutePath(), resources, bLangPackage);
    }

    public static void setPrintStream(PrintStream out) {
        BallerinaDocGenerator.out = out;
    }

    /**
     * Generate docs generator model.
     *
     * @param moduleDocList moduleDocList modules list whose docs to be generated
     * @param resources     module level doc resources
     * @return docs generator model of the project
     */
    public static Project getDocsGenModel(List<ModuleDoc> moduleDocList, Map<String, List<Path>> resources) {
        Project project = new Project();
        project.isSingleFile =
                moduleDocList.size() == 1 && moduleDocList.get(0).bLangPackage.packageID.name.value.equals(".");
        if (project.isSingleFile) {
            project.sourceFileName = moduleDocList.get(0).bLangPackage.packageID.sourceFileName.value;
        }
        project.name = "";
        project.description = "";

        List<Module> moduleDocs = new ArrayList<>();
        for (ModuleDoc moduleDoc : moduleDocList) {
            // Generate module models
            Module module = new Module();
            module.id = moduleDoc.bLangPackage.packageID.name.toString();
            module.orgName = moduleDoc.bLangPackage.packageID.orgName.toString();
            String moduleVersion = moduleDoc.bLangPackage.packageID.version.toString();
            // get version from system property if not found in bLangPackage
            module.version = moduleVersion.equals("") ?
                    System.getProperty(BallerinaDocConstants.VERSION) :
                    moduleVersion;
            module.summary = moduleDoc.summary;
            module.description = moduleDoc.description;

            // populate module constructs
            sortModuleConstructs(moduleDoc.bLangPackage);
            Generator.generateModuleConstructs(module, moduleDoc.bLangPackage);

            // collect module's doc resources
            resources.put(module.id, moduleDoc.resources);

            moduleDocs.add(module);
        }
        project.modules = moduleDocs;
        return project;
    }

    private static void sortModuleConstructs(BLangPackage bLangPackage) {
        bLangPackage.getFunctions().sort(Comparator.comparing(f -> (f.getReceiver() == null ? "" : f
                .getReceiver().getName()) + f.getName().getValue()));
        bLangPackage.getAnnotations().sort(Comparator.comparing(a -> a.getName().getValue()));
        bLangPackage.getTypeDefinitions()
                .sort(Comparator.comparing(a -> a.getName() == null ? "" : a.getName().getValue()));
        bLangPackage.getGlobalVariables().sort(Comparator.comparing(a -> a.getName().getValue()));
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
}
