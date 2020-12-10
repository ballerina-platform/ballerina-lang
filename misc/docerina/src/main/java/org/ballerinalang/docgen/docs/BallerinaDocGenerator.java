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
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.docs.utils.PathToJson;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.docgen.generator.model.search.ConstructSearchJson;
import org.ballerinalang.docgen.generator.model.search.ModuleSearchJson;
import org.ballerinalang.docgen.generator.model.search.SearchJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String DOC_JSON = "api-doc-data.json";
    private static final String JSON = ".json";
    private static final String MODULE_SEARCH = "search";
    private static final String SEARCH_DATA = "search-data.js";
    private static final String SEARCH_DIR = "doc-search";
    private static Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Path.class, new PathToJson())
            .excludeFieldsWithoutExposeAnnotation().create();

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
        Project project = new Project();
        for (File file : fList) {
            if (file.isDirectory()) {
                Path docJsonPath = Paths.get(file.getAbsolutePath(), "data", "doc_data" + JSON);
                if (docJsonPath.toFile().exists()) {
                    try (BufferedReader br = Files.newBufferedReader(docJsonPath, StandardCharsets.UTF_8)) {
                        Project jsonProject = gson.fromJson(br, Project.class);
                        project.resources.addAll(getResourcePaths(Paths.get(file.getAbsolutePath())));
                        project.modules.addAll(jsonProject.modules);

                        File newIndex = new File(file.getAbsolutePath() + File.separator + "index.html");
                        String htmlData = "<!DOCTYPE html>\n" +
                                "<html>\n" +
                                "<head>\n" +
                                "\t<meta http-equiv=\"refresh\" content=\"0; URL=../index.html#/" + jsonProject.name +
                                "\" />\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "\t<h1>If you are not redirected please click this <a href=\"../index.html#/" +
                                jsonProject.name + "\">link</a> </h1>\n" +
                                "</body>\n" +
                                "</html>";
                        FileUtils.write(newIndex, htmlData, StandardCharsets.UTF_8, false);
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
        writeAPIDocs(project, apiDocsRoot, true);
    }

    /**
     * API to generate API docs using a Project to a given folder.
     *  @param project Ballerina project
     *  @param output Output path as a string
     */
    public static void generateAPIDocs(io.ballerina.projects.Project project, String output)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap = generateModuleDocMap(project);
        Project docerinaProject = getDocsGenModel(moduleDocMap, project.currentPackage().packageOrg().toString(),
                project.currentPackage().packageVersion().toString());
        docerinaProject.name = project.currentPackage().descriptor().name().toString();
        Path packageMdPath = project.sourceRoot().resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
        if (packageMdPath.toFile().exists()) {
            String mdContent = new String(Files.readAllBytes(packageMdPath), "UTF-8");
            docerinaProject.description = BallerinaDocUtils.mdToHtml(mdContent, true);
        }
        if (!docerinaProject.modules.isEmpty()) {
            writeAPIDocs(docerinaProject, output, false);
        }
    }

    public static void writeAPIDocs(Project project, String output, boolean isMerge) {
        if (project.modules.size() != 0) {
            if (!isMerge) {
                output = project.name.equals("") ? output + File.separator + project.modules.get(0).id
                        : output + File.separator + project.name;
            }
            String dataDir = output + File.separator + "data";
            String searchDir = output + File.separator + SEARCH_DIR;
            try {
                Files.createDirectories(Paths.get(dataDir));
                Files.createDirectories(Paths.get(searchDir));
                genSearchJson(project, searchDir);
                genProjectJson(project, dataDir);
            } catch (IOException e) {
                out.printf("docerina: API documentation generation failed%n", e.getMessage());
                log.error("API documentation generation failed:", e);
            }

        }
        if (!project.resources.isEmpty()) {
            String resourcesDir = output + File.separator + "resources";
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: copying project resources ");
            }
            for (Path resourcePath : project.resources) {
                File resourcesDirFile = new File(resourcesDir);
                try {
                    FileUtils.copyFileToDirectory(resourcePath.toFile(), resourcesDirFile);
                } catch (IOException e) {
                    out.println(String.format("docerina: failed to copy [resource] %s into " +
                            "[resources directory] %s. Cause: %s", resourcePath.toString(),
                            resourcesDirFile.toString(), e.getMessage()));
                    log.error(String.format("docerina: failed to copy [resource] %s into [resources directory] "
                            + "%s. Cause: %s", resourcePath.toString(), resourcesDirFile.toString(),
                            e.getMessage()), e);
                }
            }
            if (BallerinaDocUtils.isDebugEnabled()) {
                out.println("docerina: successfully copied project resources into " + resourcesDir);
            }
        }

        // Copy docerina ui
        File source = new File(System.getProperty("ballerina.home") + File.separator + "lib" + File.separator +
                "tools" + File.separator + "doc-ui");
        File dest;
        if (source.exists()) {
            dest = new File(output);
            try {
                FileUtils.copyDirectory(source, dest);
            } catch (IOException e) {
                out.println(String.format("docerina: failed to copy doc ui. Cause: %s", e.getMessage
                        ()));
                log.error("Failed to copy the doc ui.", e);
            }
        } else {
            dest = new File(output, "index.html");
            try {
                FileUtils.copyInputStreamToFile(BallerinaDocGenerator.class
                        .getResourceAsStream("/doc-ui/index.html"), dest);
                //BallerinaDocUtils.unzipResources(, dest);
            } catch (IOException e) {
                out.println(String.format("docerina: failed to copy doc ui. Cause: %s", e.getMessage
                        ()));
                log.error("Failed to copy the doc ui.", e);
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

    private static void genProjectJson(Project project, String dataDir) {

        File jsFile = new File(dataDir + File.separator + "doc_data.js");
        File jsonFile = new File(dataDir + File.separator + "doc_data.json");
        if (jsFile.exists()) {
            boolean deleted = jsFile.delete();
            if (!deleted) {
                out.println("docerina: failed to delete " + jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            boolean deleted = jsonFile.delete();
            if (!deleted) {
                out.println("docerina: failed to delete " + jsonFile.toString());
            }
        }
        String json = gson.toJson(project);
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                StandardCharsets.UTF_8)) {
            String js = "var docData = " + json + ";";
            writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the " + "doc_data.js" + ". Cause: %s",
                    e.getMessage()));
            log.error("Failed to create " + "doc_data.js" + " file.", e);
        }

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the " + "doc_data.json" + ". Cause: %s",
                    e.getMessage()));
            log.error("Failed to create " + "doc_data.json" + " file.", e);
        }
    }

    private static void genSearchJson(Project project, String searchDir) {
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
        List<ConstructSearchJson> searchAbstractObjects = new ArrayList<>();

        for (Module module: project.modules) {
            if (module.summary != null) {
                searchModules.add(new ModuleSearchJson(module.id, getFirstLine(module.summary)));
            }
            module.functions.forEach((function) ->
                    searchFunctions.add(new ConstructSearchJson(function.name, module.id,
                            getFirstLine(function.description))));

            module.classes.forEach((bClass) ->
                    searchClasses.add(new ConstructSearchJson(bClass.name, module.id, getFirstLine(bClass
                            .description))));

            module.abstractObjects.forEach((absObj) ->
                    searchAbstractObjects.add(new ConstructSearchJson(absObj.name, module.id,
                            getFirstLine(absObj.description))));

            module.clients.forEach((client) ->
                    searchClients.add(new ConstructSearchJson(client.name, module.id, getFirstLine(client
                            .description))));

            module.listeners.forEach((listener) ->
                    searchListeners.add(new ConstructSearchJson(listener.name, module.id,
                            getFirstLine(listener.description))));

            module.records.forEach((record) ->
                    searchRecords.add(new ConstructSearchJson(record.name, module.id, getFirstLine(record.
                            description))));

            module.constants.forEach((constant) ->
                    searchConstants.add(new ConstructSearchJson(constant.name, module.id,
                            getFirstLine(constant.description))));

            module.errors.forEach((error) ->
                    searchErrors.add(new ConstructSearchJson(error.name, module.id, getFirstLine(error
                            .description))));

            module.types.forEach((unionType) ->
                    searchTypes.add(new ConstructSearchJson(unionType.name, module.id,
                            getFirstLine(unionType.description))));

            module.annotations.forEach((annotation) ->
                    searchAnnotations.add(new ConstructSearchJson(annotation.name, module.id,
                            getFirstLine(annotation.description))));
        }

        SearchJson searchJson = new SearchJson(searchModules, searchClasses, searchFunctions, searchRecords,
                searchConstants, searchErrors, searchTypes, searchClients, searchListeners, searchAnnotations,
                searchAbstractObjects);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File jsonFile = new File(searchDir + File.separator + "search-data.json");
        File jsFile = new File(searchDir + File.separator + "search-data.js");
        if (jsFile.exists()) {
            boolean deleted = jsFile.delete();
            if (!deleted) {
                out.println("docerina: failed to delete " + jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            boolean deleted = jsonFile.delete();
            if (!deleted) {
                out.println("docerina: failed to delete " + jsonFile.toString());
            }
        }
        String json = gson.toJson(searchJson);

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the search-data.json. Cause: %s", e.getMessage()));
            log.error("Failed to create search-data.json file.", e);
        }

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                StandardCharsets.UTF_8)) {
            String js = "var searchData = " + json + ";";
            writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.println(String.format("docerina: failed to create the " + "search-data.js" + ". Cause: %s",
                    e.getMessage()));
            log.error("Failed to create " + "search-data.js" + " file.", e);
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
            Path modulePath;
            if (module.isDefaultModule()) {
                moduleName = module.moduleName().packageName().toString();
                modulePath = project.sourceRoot();
            } else {
                moduleName = module.moduleName().moduleNamePart();
                modulePath = project.sourceRoot().resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
            }
            // find the Module.md file
            Path moduleMd = getModuleDocPath(modulePath, ProjectConstants.MODULE_MD_FILE_NAME);
            if (moduleMd == null && module.isDefaultModule()) {
                moduleMd = getModuleDocPath(modulePath, ProjectConstants.PACKAGE_MD_FILE_NAME);
            }
            // find the resources of the package
            List<Path> resources = getResourcePaths(modulePath);
            Map<String, SyntaxTree> syntaxTreeMap = new HashMap<>();
            module.documentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document.name(), document.syntaxTree());
            });
            ModuleDoc moduleDoc = new ModuleDoc(moduleMd == null ? null : moduleMd.toAbsolutePath(), resources,
                    syntaxTreeMap, (BallerinaSemanticModel) module.getCompilation().getSemanticModel());
            moduleDocMap.put(moduleName, moduleDoc);
        }
        return moduleDocMap;
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
    public static Project getDocsGenModel(Map<String, ModuleDoc> docsMap, String orgName, String version) {
        Project project = new Project();
        project.name = "";
        project.description = "";

        List<Module> moduleDocs = new ArrayList<>();
        for (Map.Entry<String, ModuleDoc> moduleDoc : docsMap.entrySet()) {
            BallerinaSemanticModel model = moduleDoc.getValue().semanticModel;
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

            // collect module's doc resources
            project.resources.addAll(moduleDoc.getValue().resources);

            boolean hasPublicConstructs = false;
            // Loop through bal files
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                hasPublicConstructs = Generator.setModuleFromSyntaxTree(module, syntaxTreeMapEntry.getValue(), model,
                        syntaxTreeMapEntry.getKey());
            }
            if (hasPublicConstructs) {
                moduleDocs.add(module);
            }
        }
        project.modules = moduleDocs;
        return project;
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

    private static Path getModuleDocPath(Path absolutePkgPath, String mdFileName) throws IOException {
        Path packageMd;
        Optional<Path> o = Files.find(absolutePkgPath, 1, (path, attr) -> {
            Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString().equals(mdFileName);
            }
            return false;
        }).findFirst();

        packageMd = o.isPresent() ? o.get() : null;
        return packageMd;
    }
}
