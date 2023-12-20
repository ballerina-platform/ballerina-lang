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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.docs.utils.PathToJson;
import org.ballerinalang.docgen.generator.model.ApiDocsJson;
import org.ballerinalang.docgen.generator.model.CentralStdLibrary;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.ModuleLibrary;
import org.ballerinalang.docgen.generator.model.ModuleMetaData;
import org.ballerinalang.docgen.generator.model.search.ConstructSearchJson;
import org.ballerinalang.docgen.generator.model.search.ModuleSearchJson;
import org.ballerinalang.docgen.generator.model.search.SearchJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Main class to generate a ballerina documentation.
 */
public class BallerinaDocGenerator {

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static PrintStream out = System.out;

    public static final String API_DOCS_JSON = "api-docs.json";
    private static final String API_DOCS_JS = "api-docs.js";
    private static final String CENTRAL_STDLIB_INDEX_JSON = "stdlib-index.json";
    private static final String CENTRAL_STDLIB_SEARCH_JSON = "stdlib-search.json";
    private static final String BUILTIN_TYPES_DESCRIPTION_DIR = "builtin-types-descriptions";
    private static final String BUILTIN_KEYWORDS_DESCRIPTION_DIR = "keywords-descriptions";
    private static final String RELEASE_DESCRIPTION_MD = "/release-description.md";
    public static final String PROPERTIES_FILE = "/META-INF/properties";

    private static Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Path.class, new PathToJson())
            .excludeFieldsWithoutExposeAnnotation().create();

    /**
     * API to merge multiple api docs.
     *  @param apiDocsRoot api doc root
     */
    public static void mergeApiDocs(Path apiDocsRoot)  {
        out.println("docerina: API documentation generation for doc path - " + apiDocsRoot);
        File directory = apiDocsRoot.toFile();
        // get all the files from a directory
        File[] orgFileList = directory.listFiles();
        if (orgFileList == null) {
            log.error(String.format("docerina: API documentation generation failed. Could not find any packages"
                    + " in given path %s", apiDocsRoot));
            return;
        }
        Arrays.sort(orgFileList);
        ModuleLibrary moduleLib = new ModuleLibrary();
        CentralStdLibrary centralLib = new CentralStdLibrary();

        moduleLib.releaseVersion = getBallerinaShortVersion().replace("sl", "swan-lake-");
        centralLib.releaseVersion = getBallerinaShortVersion().replace("sl", "swan-lake-");
        centralLib.releaseShortVersion = getBallerinaShortVersion();
        centralLib.description = getReleaseDescription();

        for (File orgFile : orgFileList) {
            if (orgFile.isDirectory()) {
                File[] moduleFileList = orgFile.listFiles();
                Arrays.sort(moduleFileList);
                for (File moduleFile : moduleFileList) {
                    if (moduleFile.isDirectory() && moduleFile.listFiles().length > 0
                            && moduleFile.listFiles()[0].isDirectory()) {
                        File versionFile = moduleFile.listFiles()[0];
                        Path docJsonPath = Paths.get(versionFile.getAbsolutePath(), API_DOCS_JSON);
                        if (docJsonPath.toFile().exists()) {
                            try (BufferedReader br = Files.newBufferedReader(docJsonPath, StandardCharsets.UTF_8)) {
                                ApiDocsJson apiDocsJson = gson.fromJson(br, ApiDocsJson.class);
                                if (apiDocsJson.docsData.modules.isEmpty()) {
                                    log.warn("No packages found at: " + docJsonPath.toString());
                                    continue;
                                }
                                apiDocsJson.docsData.modules.forEach(mod -> {
                                    try {
                                        mod.resources
                                                .addAll(getResourcePaths(Paths.get(orgFile.getAbsolutePath())));
                                    } catch (IOException e) {
                                        log.error(String.format("API documentation generation failed. Cause: %s"
                                                , e.getMessage()), e);
                                        return;
                                    }
                                });
                                for (Module module : apiDocsJson.docsData.modules) {
                                    ModuleMetaData moduleMeta = new ModuleMetaData();
                                    moduleMeta.id = module.id;
                                    moduleMeta.summary = module.summary;
                                    moduleMeta.orgName = module.orgName;
                                    moduleMeta.version = module.version;
                                    moduleMeta.isDefaultModule = module.isDefaultModule;
                                    if (module.id.startsWith("lang.")) {
                                        centralLib.langLibs.add(moduleMeta);
                                        moduleLib.langLibs.add(module);
                                    } else {
                                        centralLib.modules.add(moduleMeta);
                                        moduleLib.modules.add(module);
                                    }

                                }
                            } catch (IOException e) {
                                log.error(String.format("API documentation generation failed. Cause: %s",
                                        e.getMessage()), e);
                                return;
                            }
                    }
                }
                }
            }
        }
        moduleLib.modules.sort((o1, o2) -> o1.id.compareToIgnoreCase(o2.id));
        centralLib.modules.sort((o1, o2) -> o1.id.compareToIgnoreCase(o2.id));
        writeAPIDocs(moduleLib, apiDocsRoot, true, false);

        // Create the central Ballerina library index JSON.
        String stdIndexJson = gson.toJson(centralLib);
        File stdIndexJsonFile = apiDocsRoot.resolve(CENTRAL_STDLIB_INDEX_JSON).toFile();
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(stdIndexJsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(stdIndexJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to create {} file.", CENTRAL_STDLIB_INDEX_JSON, e);
        }

        // Create the central Ballerina library search JSON.
        String stdSearchJson = gson.toJson(genSearchJson(moduleLib));
        File stdSearchJsonFile = apiDocsRoot.resolve(CENTRAL_STDLIB_SEARCH_JSON).toFile();
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(stdSearchJsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(stdSearchJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to create {} file.", CENTRAL_STDLIB_SEARCH_JSON, e);
        }
    }

    /**
     * API to generate API docs using a Project to a given folder.
     *  @param project Ballerina project
     *  @param output Output path as a string
     *  @param excludeUI Exclude UI elements being copied/generated
     */
    public static void generateAPIDocs(Project project, String output, boolean excludeUI)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap = generateModuleDocMap(project);
        ModuleLibrary moduleLib = new ModuleLibrary();
        moduleLib.modules = getDocsGenModel(moduleDocMap, project.currentPackage().packageOrg().toString(),
                project.currentPackage().packageVersion().toString());
        writeAPIDocs(moduleLib, Path.of(output), false, excludeUI);
    }

    private static void writeAPIDocs(ModuleLibrary moduleLib, Path output, boolean isMerge, boolean excludeUI) {
        if (moduleLib.modules.size() == 0) {
            log.error("No modules found to create docs.");
            return;
        }
        if (!isMerge && excludeUI) {
            // Doc gen for a bala.
            // Creates jsons for each modules
            for (Module module : moduleLib.modules) {
                ModuleLibrary tempLib = new ModuleLibrary();
                tempLib.modules.add(module);
                Path outputPath = output.resolve(module.orgName).resolve(module.id).resolve(module.version);
                genApiDocsJson(tempLib, outputPath, true);
                copyResources(module.resources, outputPath);
            }
            return;
        } else if (!isMerge) {
            // Doc generation via doc command
            output = output.resolve(moduleLib.modules.get(0).orgName).resolve(moduleLib.modules.get(0).id)
                    .resolve(moduleLib.modules.get(0).version);
        }
        genApiDocsJson(moduleLib, output, false);
        for (Module module: moduleLib.modules) {
            copyResources(module.resources, output);
        }
        copyDocerinaUI(output);
    }

    private static void copyDocerinaUI(Path output) {
        File source = Path.of(System.getProperty("ballerina.home"), "lib", "tools", "doc-ui").toFile();
        File dest;
        if (source.exists()) {
            dest = output.toFile();
            try {
                FileUtils.copyDirectory(source, dest);
            } catch (IOException e) {
                log.error("Failed to copy the doc ui.", e);
            }
        } else {
            dest = output.resolve("index.html").toFile();
            try {
                FileUtils.copyInputStreamToFile(BallerinaDocGenerator.class
                        .getResourceAsStream("/doc-ui/index.html"), dest);
            } catch (IOException e) {
                log.error("Failed to copy the doc ui.", e);
            }
        }
    }

    private static void copyResources(List<Path> resources, Path output) {
        if (!resources.isEmpty()) {
            File resourcesDirFile = output.resolve("resources").toFile();
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: copying project resources ");
            }
            for (Path resourcePath : resources) {
                try {
                    FileUtils.copyFileToDirectory(resourcePath.toFile(), resourcesDirFile);
                } catch (IOException e) {
                    log.error(String.format("docerina: failed to copy [resource] %s into [resources directory] "
                                    + "%s. Cause: %s", resourcePath.toString(), resourcesDirFile.toString(),
                            e.getMessage()), e);
                }
            }
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: successfully copied project resources into " + resourcesDirFile);
            }
        }
    }

    private static String getApiDocsVersion() {
        String apiDocsVersion = "";
        try (InputStream inputStream = BallerinaDocGenerator.class.getResourceAsStream("/META-INF/tool.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            apiDocsVersion = properties.getProperty("apiDocs.version").split("-")[0];

            return apiDocsVersion;
        } catch (IOException e) {
            return "NOT_FOUND";
        }
    }

    private static void genApiDocsJson(ModuleLibrary moduleLib, Path destination, boolean excludeUI) {
        try {
            Files.createDirectories(destination);
        } catch (IOException e) {
            log.error("API documentation generation failed when creating directory:", e);
        }

        ApiDocsJson apiDocsJson = new ApiDocsJson();
        apiDocsJson.apiDocsVersion = getApiDocsVersion();
        apiDocsJson.docsData = moduleLib;
        apiDocsJson.searchData = genSearchJson(moduleLib);

        File jsFile = destination.resolve(API_DOCS_JS).toFile();
        File jsonFile = destination.resolve(API_DOCS_JSON).toFile();

        if (jsFile.exists()) {
            if (!jsFile.delete()) {
                log.error("docerina: failed to delete {}", jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            if (!jsonFile.delete()) {
                log.error("docerina: failed to delete {}", jsonFile.toString());
            }
        }
        String json = gson.toJson(apiDocsJson);
        if (!excludeUI) {
            try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                    StandardCharsets.UTF_8)) {
                String js = "var apiDocsJson = " + json + ";";
                writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("Failed to create {} file.", API_DOCS_JS, e);
            }
        }

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to create {} file.", API_DOCS_JSON, e);
        }
    }

    private static SearchJson genSearchJson(ModuleLibrary moduleLib) {
        List<ModuleSearchJson> searchModules = new ArrayList<>();
        List<ConstructSearchJson> searchFunctions = new ArrayList<>();
        List<ConstructSearchJson> searchClasses = new ArrayList<>();
        List<ConstructSearchJson> searchRecords = new ArrayList<>();
        List<ConstructSearchJson> searchConstants = new ArrayList<>();
        List<ConstructSearchJson> searchErrors = new ArrayList<>();
        List<ConstructSearchJson> searchTypes = new ArrayList<>();
        List<ConstructSearchJson> searchClients = new ArrayList<>();
        List<ConstructSearchJson> searchListeners = new ArrayList<>();
        List<ConstructSearchJson> searchAnnotations = new ArrayList<>();
        List<ConstructSearchJson> searchObjectTypes = new ArrayList<>();
        List<ConstructSearchJson> searchEnums = new ArrayList<>();

        List<Module> allModules = new ArrayList<>();
        allModules.addAll(moduleLib.langLibs);
        allModules.addAll(moduleLib.modules);
        allModules.sort((o1, o2) -> o1.id.compareToIgnoreCase(o2.id));

        for (Module module: allModules) {
            if (module.summary != null) {
                searchModules.add(new ModuleSearchJson(module.id, module.orgName, module.version, module.summary,
                        module.isDefaultModule));
            }

            module.functions.forEach((function) ->
                    searchFunctions.add(new ConstructSearchJson(function.name, module.id, module.orgName,
                            module.version, function.description)));

            module.classes.forEach((bClass) ->
                    searchClasses.add(new ConstructSearchJson(bClass.name, module.id, module.orgName, module.version,
                            bClass.description)));

            module.objectTypes.forEach((absObj) ->
                    searchObjectTypes.add(new ConstructSearchJson(absObj.name, module.id, module.orgName,
                            module.version, absObj.description)));

            module.clients.forEach((client) ->
                    searchClients.add(new ConstructSearchJson(client.name, module.id, module.orgName, module.version,
                            client.description)));

            module.listeners.forEach((listener) ->
                    searchListeners.add(new ConstructSearchJson(listener.name, module.id, module.orgName,
                            module.version, listener.description)));

            module.records.forEach((record) ->
                    searchRecords.add(new ConstructSearchJson(record.name, module.id, module.orgName, module.version,
                            record.description)));

            module.constants.forEach((constant) ->
                    searchConstants.add(new ConstructSearchJson(constant.name, module.id, module.orgName,
                            module.version, constant.description)));

            module.errors.forEach((error) ->
                    searchErrors.add(new ConstructSearchJson(error.name, module.id, module.orgName, module.version,
                            error.description)));

            module.types.forEach((unionType) ->
                    searchTypes.add(new ConstructSearchJson(unionType.name, module.id, module.orgName, module.version,
                            unionType.description)));

            module.annotations.forEach((annotation) ->
                    searchAnnotations.add(new ConstructSearchJson(annotation.name, module.id, module.orgName,
                            module.version, annotation.description)));

            module.enums.forEach((benum) ->
                    searchEnums.add(new ConstructSearchJson(benum.name, module.id, module.orgName, module.version,
                            benum.description)));

        }

        return new SearchJson(searchModules, searchClasses, searchFunctions, searchRecords,
                searchConstants, searchErrors, searchTypes, searchClients, searchListeners, searchAnnotations,
                searchObjectTypes, searchEnums);
    }

    /**
     * Generates a map of module names and their ModuleDoc.
     *  @param project Ballerina project.
     *  @return a map of module names and their ModuleDoc.
     */
    public static Map<String, ModuleDoc> generateModuleDocMap(io.ballerina.projects.Project project)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap = new HashMap<>();
        for (io.ballerina.projects.Module module : project.currentPackage().modules()) {
            String moduleName;
            String moduleMdText = module.moduleMd().map(d -> d.content()).orElse("");
            Path modulePath;
            if (module.isDefaultModule()) {
                moduleName = module.moduleName().packageName().toString();
                modulePath = project.sourceRoot();
            } else {
                moduleName = module.moduleName().toString();
                modulePath = project.sourceRoot().resolve(ProjectConstants.MODULES_ROOT).resolve(module.moduleName()
                        .moduleNamePart());
            }
            // Skip modules that are not exported
            if (!project.currentPackage().manifest().exportedModules().contains(moduleName)) {
                continue;
            }
            // find the resources of the package
            List<Path> resources = getResourcePaths(modulePath);
            Map<String, SyntaxTree> syntaxTreeMap = new HashMap<>();
            module.documentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document.name(), document.syntaxTree());
            });
            // we cannot remove the module.getCompilation() here since the semantic model is accessed
            // after the code gen phase here. package.getCompilation() throws an IllegalStateException
            ModuleDoc moduleDoc = new ModuleDoc(moduleMdText, resources,
                    syntaxTreeMap, module.getCompilation().getSemanticModel(), module.isDefaultModule());
            moduleDocMap.put(moduleName, moduleDoc);
        }
        return moduleDocMap;
    }

    public static String getBallerinaShortVersion() {
        try (InputStream inputStream = BallerinaDocGenerator.class.getResourceAsStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(ProjectDirConstants.BALLERINA_SHORT_VERSION);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }

    public static String getReleaseDescription() {
        try (InputStream inputStream = BallerinaDocGenerator.class.getResourceAsStream(RELEASE_DESCRIPTION_MD)) {
            String desc = new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .collect(Collectors.joining("\n"));
            return desc;
        } catch (Throwable ignore) {
        }
        return "";
    }

    public static void setPrintStream(PrintStream out) {
        BallerinaDocGenerator.out = out;
    }

    /**
     * Generate docs generator model.
     *
     * @param docsMap moduleDocList modules list whose docs to be generated.
     * @param orgName organization name.
     * @param version project version.
     * @return docs generator model of the project.
     */
    public static List<Module> getDocsGenModel(Map<String, ModuleDoc> docsMap, String orgName, String version) {
        List<Module> moduleDocs = new ArrayList<>();
        List<ModuleMetaData> relatedModules = new ArrayList<>();
        for (Map.Entry<String, ModuleDoc> moduleDoc : docsMap.entrySet()) {
            SemanticModel model = moduleDoc.getValue().semanticModel;
            Module module = new Module();
            module.id = moduleDoc.getKey();
            module.orgName = orgName;
            String moduleVersion = version;
            // get version from system property if not found in bLangPackage
            module.version = moduleVersion.equals("") ?
                    System.getProperty(BallerinaDocConstants.VERSION) :
                    moduleVersion;
            module.summary = moduleDoc.getValue().summary;
            module.description = moduleDoc.getValue().description;
            module.isDefaultModule = moduleDoc.getValue().isDefault;

            // collect module's doc resources
            module.resources.addAll(moduleDoc.getValue().resources);

            boolean hasPublicConstructs = false;
            // Loop through bal files
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                boolean hasPublicConstructsTemp = Generator.setModuleFromSyntaxTree(module,
                        syntaxTreeMapEntry.getValue(), model);
                if (hasPublicConstructsTemp) {
                    hasPublicConstructs = true;
                }
            }
            if (hasPublicConstructs) {
                module.records.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.functions.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.classes.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.clients.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.listeners.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.objectTypes.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.enums.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.types.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.constants.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.annotations.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                module.errors.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
                moduleDocs.add(module);
                ModuleMetaData moduleMeta = new ModuleMetaData();
                moduleMeta.id = module.id;
                moduleMeta.orgName = module.orgName;
                moduleMeta.summary = module.summary;
                moduleMeta.version = module.version;
                moduleMeta.isDefaultModule = module.isDefaultModule;
                relatedModules.add(moduleMeta);
            }

        }
        moduleDocs.sort((module1, module2) -> module1.id.compareToIgnoreCase(module2.id));
        if (relatedModules.size() > 1) {
            relatedModules.sort((mod1, mod2) -> mod1.id.compareToIgnoreCase(mod2.id));
            moduleDocs.forEach(module -> module.relatedModules = relatedModules);
        }
        return moduleDocs;
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
}
