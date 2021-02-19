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
import io.ballerina.projects.PackageMd;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.docs.utils.PathToJson;
import org.ballerinalang.docgen.generator.model.CentralStdLibrary;
import org.ballerinalang.docgen.generator.model.DocPackage;
import org.ballerinalang.docgen.generator.model.DocPackageMetadata;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.PackageLibrary;
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

    private static final String DOC_DATA_JSON = "doc_data.json";
    private static final String DOC_DATA_JS = "doc_data.js";
    private static final String CENTRAL_DOC_DATA_JSON = "central_doc_data.json";
    private static final String SEARCH_DATA_JSON = "search-data.json";
    private static final String SEARCH_DATA_JS = "search-data.js";
    private static final String SEARCH_DIR = "doc-search";
    private static final String DATA_DIR = "data";

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
        PackageLibrary packageLib = new PackageLibrary();
        PackageLibrary langLib = null;
        CentralStdLibrary centralLib = new CentralStdLibrary();
        centralLib.releaseName = System.getProperty("ballerina.version");

        // Get doc data json containing langlibs, which is generated when ballerina-lang is being built.
        Path langLibsJsonPath = Paths.get(directory.getAbsolutePath(), DATA_DIR, DOC_DATA_JSON);
        if (langLibsJsonPath.toFile().exists()) {
            try (BufferedReader br = Files.newBufferedReader(langLibsJsonPath, StandardCharsets.UTF_8)) {
                langLib = gson.fromJson(br, PackageLibrary.class);
                if (langLib.packages.isEmpty()) {
                    out.printf("No langlibs found at: %s%n", langLibsJsonPath.toString());
                    return;
                }
            } catch (IOException e) {
                String errorMsg = String.format("API documentation generation failed. Cause: %s",
                        e.getMessage());
                out.println(errorMsg);
                log.error(errorMsg, e);
                return;
            }
        }

        for (File file : fList) {
            if (file.isDirectory()) {
                Path docJsonPath = Paths.get(file.getAbsolutePath(), DATA_DIR, DOC_DATA_JSON);
                if (docJsonPath.toFile().exists()) {
                    try (BufferedReader br = Files.newBufferedReader(docJsonPath, StandardCharsets.UTF_8)) {
                        PackageLibrary jsonPackageLib = gson.fromJson(br, PackageLibrary.class);
                        if (jsonPackageLib.packages.isEmpty()) {
                            out.println("No packages found at: " + docJsonPath.toString());
                            continue;
                        }
                        jsonPackageLib.packages.forEach(docPackage -> {
                            try {
                                docPackage.resources.addAll(getResourcePaths(Paths.get(file.getAbsolutePath())));
                            } catch (IOException e) {
                                String errorMsg = String.format("API documentation generation failed. Cause: %s",
                                        e.getMessage());
                                out.println(errorMsg);
                                log.error(errorMsg, e);
                                return;
                            }
                        });
                        for (DocPackage docPackage : jsonPackageLib.packages) {
                            packageLib.packages.add(docPackage);

                            // Create json to show stdlibs through central
                            if (langLib != null) {
                                if (langLib.packages.stream().anyMatch(langLibPackage
                                        -> langLibPackage.name.equals(docPackage.name))) {
                                    centralLib.langLibs.add(docPackage);
                                } else {
                                    DocPackageMetadata pkgMeta = new DocPackageMetadata();
                                    pkgMeta.name = docPackage.name;
                                    pkgMeta.summary = docPackage.summary;
                                    pkgMeta.orgName = docPackage.orgName;
                                    pkgMeta.version = docPackage.version;
                                    centralLib.stdLibs.add(pkgMeta);
                                }
                            }
                        }
                        File newIndex = new File(file.getAbsolutePath() + File.separator + "index.html");
                        String htmlData = "<!DOCTYPE html>\n" +
                                "<html>\n" +
                                "<head>\n" +
                                "\t<meta http-equiv=\"refresh\" content=\"0; URL=../index.html#/" +
                                jsonPackageLib.packages.get(0).orgName + "/" +
                                jsonPackageLib.packages.get(0).name +
                                "/latest\" />\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "\t<h1>If you are not redirected please click this <a href=\"../index.html#/" +
                                jsonPackageLib.packages.get(0).orgName + "/" +
                                jsonPackageLib.packages.get(0).name + "/latest\">link</a> </h1>\n" +
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
        writeAPIDocs(packageLib, apiDocsRoot, true);

        // Create the central json
        String json = gson.toJson(centralLib);
        File jsonFile = new File(apiDocsRoot + File.separator + DATA_DIR + File.separator + CENTRAL_DOC_DATA_JSON);
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.printf("docerina: failed to create the %s. Cause: %s%n", CENTRAL_DOC_DATA_JSON, e.getMessage());
            log.error("Failed to create {} file.", CENTRAL_DOC_DATA_JSON, e);
        }
    }

    /**
     * API to generate API docs using a Project to a given folder.
     *  @param project Ballerina project
     *  @param output Output path as a string
     */
    public static void generateAPIDocs(Project project, String output)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap = generateModuleDocMap(project);
        DocPackage docPackage = getDocsGenModel(moduleDocMap, project.currentPackage().packageOrg().toString(),
                project.currentPackage().packageVersion().toString());
        docPackage.name = project.currentPackage().descriptor().name().toString();
        docPackage.orgName = project.currentPackage().packageOrg().toString();
        docPackage.version = project.currentPackage().packageVersion().toString();
        Optional<PackageMd> packageMdPath = project.currentPackage().packageMd();
        docPackage.description = packageMdPath
                .map(packageMd -> packageMd.content())
                .orElse("");
        if (!docPackage.description.equals("")) {
            docPackage.summary = BallerinaDocUtils.getSummary(docPackage.description);
        } else if (moduleDocMap.get(docPackage.name) != null) {
            // Use summary of the default module
            docPackage.summary = moduleDocMap.get(docPackage.name).summary;
        }
        if (!docPackage.modules.isEmpty()) {
            PackageLibrary packageLib = new PackageLibrary();
            packageLib.packages.add(docPackage);
            writeAPIDocs(packageLib, output, false);
        }
    }

    private static void writeAPIDocs(PackageLibrary packageLib, String output, boolean isMerge) {
        if (packageLib.packages.size() == 0) {
            return;
        }
        if (packageLib.packages.get(0).modules.size() != 0) {
            if (!isMerge) {
                output = packageLib.packages.get(0).name.equals("") ? output + File.separator +
                        packageLib.packages.get(0).modules.get(0).id
                        : output + File.separator + packageLib.packages.get(0).name;
            }
            String dataDir = output + File.separator + DATA_DIR;
            String searchDir = output + File.separator + SEARCH_DIR;
            try {
                Files.createDirectories(Paths.get(dataDir));
                Files.createDirectories(Paths.get(searchDir));
                genSearchJson(packageLib, searchDir);
                genProjectJson(packageLib, dataDir);
            } catch (IOException e) {
                out.printf("docerina: API documentation generation failed%n", e.getMessage());
                log.error("API documentation generation failed:", e);
            }

        }
        for (DocPackage docPackage: packageLib.packages) {
            if (!docPackage.resources.isEmpty()) {
                String resourcesDir = output + File.separator + "resources";
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: copying project resources ");
                }
                for (Path resourcePath : docPackage.resources) {
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

    private static void genProjectJson(PackageLibrary packageLib, String dataDir) {

        File jsFile = new File(dataDir + File.separator + DOC_DATA_JS);
        File jsonFile = new File(dataDir + File.separator + DOC_DATA_JSON);
        if (jsFile.exists()) {
            if (!jsFile.delete()) {
                out.printf("docerina: failed to delete %s%n", jsFile.toString());
                log.error("docerina: failed to delete {}", jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            if (!jsonFile.delete()) {
                out.printf("docerina: failed to delete %s%n", jsonFile.toString());
                log.error("docerina: failed to delete {}", jsonFile.toString());
            }
        }
        String json = gson.toJson(packageLib);
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                StandardCharsets.UTF_8)) {
            String js = "var docData = " + json + ";";
            writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.printf("docerina: failed to create the %s. Cause: %s%n", DOC_DATA_JS, e.getMessage());
            log.error("Failed to create {} file.", DOC_DATA_JS, e);
        }

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.printf("docerina: failed to create the %s. Cause: %s%n", DOC_DATA_JSON, e.getMessage());
            log.error("Failed to create {} file.", DOC_DATA_JSON, e);
        }
    }

    private static void genSearchJson(PackageLibrary packageLib, String searchDir) {
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

        for (DocPackage docPackage: packageLib.packages) {
            for (Module module : docPackage.modules) {
                if (module.summary == null && docPackage.summary != null) {
                    searchModules.add(new ModuleSearchJson(module.id, module.orgName, module.version,
                            getFirstLine(docPackage.summary)));
                } else if (module.summary != null) {
                    searchModules.add(new ModuleSearchJson(module.id, module.orgName, module.version,
                            getFirstLine(module.summary)));
                }
                module.functions.forEach((function) ->
                        searchFunctions.add(new ConstructSearchJson(function.name, module.id, module.orgName,
                                module.version, getFirstLine(function.description))));

                module.classes.forEach((bClass) ->
                        searchClasses.add(new ConstructSearchJson(bClass.name, module.id, module.orgName,
                                module.version, getFirstLine(bClass.description))));

                module.objectTypes.forEach((absObj) ->
                        searchObjectTypes.add(new ConstructSearchJson(absObj.name, module.id, module.orgName,
                                module.version, getFirstLine(absObj.description))));

                module.clients.forEach((client) ->
                        searchClients.add(new ConstructSearchJson(client.name, module.id, module.orgName,
                                module.version, getFirstLine(client.description))));

                module.listeners.forEach((listener) ->
                        searchListeners.add(new ConstructSearchJson(listener.name, module.id, module.orgName,
                                module.version, getFirstLine(listener.description))));

                module.records.forEach((record) ->
                        searchRecords.add(new ConstructSearchJson(record.name, module.id, module.orgName,
                                module.version, getFirstLine(record.description))));

                module.constants.forEach((constant) ->
                        searchConstants.add(new ConstructSearchJson(constant.name, module.id, module.orgName,
                                module.version, getFirstLine(constant.description))));

                module.errors.forEach((error) ->
                        searchErrors.add(new ConstructSearchJson(error.name, module.id, module.orgName, module.version,
                                getFirstLine(error.description))));

                module.types.forEach((unionType) ->
                        searchTypes.add(new ConstructSearchJson(unionType.name, module.id, module.orgName,
                                module.version, getFirstLine(unionType.description))));

                module.annotations.forEach((annotation) ->
                        searchAnnotations.add(new ConstructSearchJson(annotation.name, module.id, module.orgName,
                                module.version, getFirstLine(annotation.description))));

                module.enums.forEach((benum) ->
                        searchEnums.add(new ConstructSearchJson(benum.name, module.id, module.orgName,
                                module.version, getFirstLine(benum.description))));
            }
        }

        SearchJson searchJson = new SearchJson(searchModules, searchClasses, searchFunctions, searchRecords,
                searchConstants, searchErrors, searchTypes, searchClients, searchListeners, searchAnnotations,
                searchObjectTypes, searchEnums);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File jsonFile = new File(searchDir + File.separator + SEARCH_DATA_JSON);
        File jsFile = new File(searchDir + File.separator + SEARCH_DATA_JS);
        if (jsFile.exists()) {
            if (!jsFile.delete()) {
                out.printf("docerina: failed to delete %s%n", jsFile.toString());
                log.error("docerina: failed to delete {}", jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            if (!jsonFile.delete()) {
                out.printf("docerina: failed to delete %s%n", jsonFile.toString());
                log.error("docerina: failed to delete {}", jsonFile.toString());
            }
        }
        String json = gson.toJson(searchJson);

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.printf("docerina: failed to create the %s. Cause: %s%n", SEARCH_DATA_JSON, e.getMessage());
            log.error("Failed to create {} file.", SEARCH_DATA_JSON, e);
        }

        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                StandardCharsets.UTF_8)) {
            String js = "var searchData = " + json + ";";
            writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            out.printf("docerina: failed to create the %s. Cause: %s%n", SEARCH_DATA_JS, e.getMessage());
            log.error("Failed to create {} file.", SEARCH_DATA_JS, e);
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
            String moduleMdText = module.moduleMd().map(d -> d.content()).orElse("");
            Path modulePath;
            if (module.isDefaultModule()) {
                moduleName = module.moduleName().packageName().toString();
                modulePath = project.sourceRoot();
            } else {
                moduleName = module.moduleName().packageName() + "." + module.moduleName().moduleNamePart();
                modulePath = project.sourceRoot().resolve(ProjectConstants.MODULES_ROOT).resolve(module.moduleName()
                        .moduleNamePart());
            }
            // find the resources of the package
            List<Path> resources = getResourcePaths(modulePath);
            Map<String, SyntaxTree> syntaxTreeMap = new HashMap<>();
            module.documentIds().forEach(documentId -> {
                Document document = module.document(documentId);
                syntaxTreeMap.put(document.name(), document.syntaxTree());
            });
            ModuleDoc moduleDoc = new ModuleDoc(moduleMdText, resources,
                    syntaxTreeMap, module.getCompilation().getSemanticModel());
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
    public static DocPackage getDocsGenModel(Map<String, ModuleDoc> docsMap, String orgName, String version) {
        DocPackage docPackage = new DocPackage();
        docPackage.name = "";
        docPackage.description = "";

        List<Module> moduleDocs = new ArrayList<>();
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

            // collect module's doc resources
            docPackage.resources.addAll(moduleDoc.getValue().resources);

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
            }
        }
        moduleDocs.sort((module1, module2) -> module1.id.compareToIgnoreCase(module2.id));
        docPackage.modules = moduleDocs;
        return docPackage;
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
