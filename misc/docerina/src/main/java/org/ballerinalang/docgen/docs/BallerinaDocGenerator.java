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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Main class to generate a ballerina documentation.
 */
public final class BallerinaDocGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static PrintStream out = System.out;

    public static final String API_DOCS_JSON = "api-docs.json";
    private static final String API_DOCS_JS = "api-docs.js";
    private static final String CENTRAL_STDLIB_INDEX_JSON = "stdlib-index.json";
    private static final String CENTRAL_STDLIB_SEARCH_JSON = "stdlib-search.json";
    private static final String BALLERINA_DOC_UI_ZIP_FILE_NAME = "ballerina-doc-ui.zip";
    private static final String CONTENT_TYPE = "application/json";
    private static final String DOCS_FOLDER_NAME = "docs";
    private static final String ICON_NAME = "icon.png";
    private static final String JSON_KEY_HASH_VALUE = "hashValue";
    private static final String JSON_KEY_FILE_URL = "fileURL";
    private static final String RELEASE_DESCRIPTION_MD = "/release-description.md";
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String SHA256_HASH_FILE_NAME = "ballerina-doc-ui-hash.sha256";
    private static final String PROPERTIES_FILE = "/META-INF/properties";
    private static final String CENTRAL_REGISTRY_PATH = "/registry";
    private static final String CENTRAL_DOC_UI_PATH = "/docs/doc-ui";

    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Path.class, new PathToJson())
            .excludeFieldsWithoutExposeAnnotation().create();

    private BallerinaDocGenerator() {
    }

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
            LOG.error(String.format("docerina: API documentation generation failed. Could not find any packages"
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
                        Path docJsonPath = Path.of(versionFile.getAbsolutePath(), API_DOCS_JSON);
                        if (docJsonPath.toFile().exists()) {
                            try (BufferedReader br = Files.newBufferedReader(docJsonPath, StandardCharsets.UTF_8)) {
                                ApiDocsJson apiDocsJson = GSON.fromJson(br, ApiDocsJson.class);
                                if (apiDocsJson.docsData.modules.isEmpty()) {
                                    LOG.warn("No packages found at: " + docJsonPath.toString());
                                    continue;
                                }
                                apiDocsJson.docsData.modules.forEach(mod -> {
                                    try {
                                        mod.resources
                                                .addAll(getResourcePaths(Path.of(orgFile.getAbsolutePath())));
                                    } catch (IOException e) {
                                        LOG.error(String.format("API documentation generation failed. Cause: %s"
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
                                LOG.error(String.format("API documentation generation failed. Cause: %s",
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
        String stdIndexJson = GSON.toJson(centralLib);
        File stdIndexJsonFile = apiDocsRoot.resolve(CENTRAL_STDLIB_INDEX_JSON).toFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(stdIndexJsonFile), StandardCharsets.UTF_8)) {
            writer.write(new String(stdIndexJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("Failed to create {} file.", CENTRAL_STDLIB_INDEX_JSON, e);
        }

        // Create the central Ballerina library search JSON.
        String stdSearchJson = GSON.toJson(genSearchJson(moduleLib));
        File stdSearchJsonFile = apiDocsRoot.resolve(CENTRAL_STDLIB_SEARCH_JSON).toFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(stdSearchJsonFile), StandardCharsets.UTF_8)) {
            writer.write(new String(stdSearchJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("Failed to create {} file.", CENTRAL_STDLIB_SEARCH_JSON, e);
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
        copyIcon(project, Path.of(output), moduleLib);
    }

    public static void copyIcon(Project project, Path output, ModuleLibrary moduleLib) {
        String sourceLocation = project.currentPackage().manifest().icon();
        if (!sourceLocation.isEmpty()) {
            output = output.resolve(moduleLib.modules.get(0).orgName).resolve(moduleLib.modules.get(0).id)
                    .resolve(moduleLib.modules.get(0).version).resolve(ICON_NAME);
            Path iconPath = Path.of(sourceLocation);
            try {
                byte[] iconByteArray = Files.readAllBytes(iconPath);
                Files.write(output, iconByteArray);
            } catch (IOException e) {
                LOG.error("Failed to copy icon to the API docs.", e);
            }
        }
    }

    private static void writeAPIDocs(ModuleLibrary moduleLib, Path output, boolean isMerge, boolean excludeUI) {
        if (moduleLib.modules.isEmpty()) {
            LOG.error("No modules found to create docs.");
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
        String source = RepoUtils.getRemoteRepoURL();
        source = source.replace(CENTRAL_REGISTRY_PATH, CENTRAL_DOC_UI_PATH);
        Path docsDirPath = ProjectUtils.createAndGetHomeReposPath().resolve(DOCS_FOLDER_NAME);
        Path sha256FilePath = docsDirPath.resolve(SHA256_HASH_FILE_NAME);
        Path zipFilePath = docsDirPath.resolve(BALLERINA_DOC_UI_ZIP_FILE_NAME);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(source)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != HTTP_OK || response.body() == null ||
                    !Objects.equals(response.header("content-type"), CONTENT_TYPE)) {
                throw new IOException("Response failed with status code: " + response.code());
            }
            ResponseBody responseBody = response.body();
            JsonObject jsonResponse = JsonParser.parseReader(responseBody.charStream()).getAsJsonObject();
            String sha256HashValue = jsonResponse.get(JSON_KEY_HASH_VALUE).getAsString();
            String zipFileURL = jsonResponse.get(JSON_KEY_FILE_URL).getAsString();
            if (!Files.exists(sha256FilePath) || !Files.exists(zipFilePath)) {
                if (!docsDirPath.toFile().exists()) {
                    Files.createDirectories(docsDirPath);
                }
                writeFileInCache(zipFileURL, sha256HashValue, zipFilePath, sha256FilePath);
            } else {
                String hashValueInCache = Files.readString(sha256FilePath).trim();
                if (!sha256HashValue.equals(hashValueInCache)) {
                    writeFileInCache(zipFileURL, sha256HashValue, zipFilePath, sha256FilePath);
                }
            }
            copyDocUIToProjectDir(output, zipFilePath);
        } catch (IOException e) {
            if (Files.exists(zipFilePath)) {
                String warning = """
                        WARNING: Unable to fetch the latest UI from the central.
                        This document is built using an existing version of the UI.
                        """;
                out.println(warning);
                copyDocUIToProjectDir(output, zipFilePath);
            } else {
                File sourceDir = Path.of(System.getProperty("ballerina.home"), "lib", "tools", "doc-ui").toFile();
                if (sourceDir.exists()) {
                    try {
                        FileUtils.copyDirectory(sourceDir, output.toFile());
                    } catch (IOException ex) {
                        LOG.error("Failed to copy the API doc UI", ex);
                    }
                } else {
                    try {
                        FileUtils.copyInputStreamToFile(BallerinaDocGenerator.class
                                .getResourceAsStream("/doc-ui/index.html"), output.resolve("index.html").toFile());
                    } catch (IOException ex) {
                        LOG.error("Failed to copy the API doc UI", ex);
                    }
                }
            }
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }

    private static void writeFileInCache(String fileURL, String hashValue, Path zipFilePath, Path hashFilePath)
            throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url(fileURL).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == HTTP_OK && response.body() != null) {
                ResponseBody responseBody = response.body();
                byte[] contentInBytes = responseBody.bytes();
                byte[] hash = BallerinaDocUtils.getHash(contentInBytes, SHA256_ALGORITHM);
                String checksum = BallerinaDocUtils.bytesToHex(hash);
                if (checksum.equals(hashValue)) {
                    Files.write(zipFilePath, contentInBytes);
                    Files.write(hashFilePath, hashValue.getBytes());
                } else {
                    throw new IOException("Failed to fetch API docs UI. UI Components may have been corrupted.");
                }
            } else {
                throw new IOException("Failed to fetch API docs UI. Request failed.");
            }
        }
    }

    private static void copyDocUIToProjectDir(Path output, Path zipFilePath) {
        try (InputStream inputStream = new FileInputStream(zipFilePath.toFile())) {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path docUIFilePath = output.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(docUIFilePath);
                } else {
                    try (OutputStream outputStream = new BufferedOutputStream
                            (new FileOutputStream(docUIFilePath.toFile()))) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    } catch (IOException e) {
                        LOG.error("Unable to write to the file" , e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Error occurred when unzipping file", e);
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
                    LOG.error(String.format("docerina: failed to copy [resource] %s into [resources directory] "
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
            LOG.error("API documentation generation failed when creating directory:", e);
        }

        ApiDocsJson apiDocsJson = new ApiDocsJson();
        apiDocsJson.apiDocsVersion = getApiDocsVersion();
        apiDocsJson.docsData = moduleLib;
        apiDocsJson.searchData = genSearchJson(moduleLib);

        File jsFile = destination.resolve(API_DOCS_JS).toFile();
        File jsonFile = destination.resolve(API_DOCS_JSON).toFile();

        if (jsFile.exists()) {
            if (!jsFile.delete()) {
                LOG.error("docerina: failed to delete {}", jsFile.toString());
            }
        }
        if (jsonFile.exists()) {
            if (!jsonFile.delete()) {
                LOG.error("docerina: failed to delete {}", jsonFile.toString());
            }
        }
        String json = GSON.toJson(apiDocsJson);
        if (!excludeUI) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsFile),
                    StandardCharsets.UTF_8)) {
                String js = "var apiDocsJson = " + json + ";";
                writer.write(new String(js.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOG.error("Failed to create {} file.", API_DOCS_JS, e);
            }
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("Failed to create {} file.", API_DOCS_JSON, e);
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
    public static Map<String, ModuleDoc> generateModuleDocMap(Project project)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap = new HashMap<>();
        Map<String, PackageManifest.Module> moduleMap = new HashMap<>();
        for (PackageManifest.Module module : project.currentPackage().manifest().modules()) {
            moduleMap.put(module.name(), module);
        }

        for (io.ballerina.projects.Module module : project.currentPackage().modules()) {
            String moduleName;
            String moduleMdText;
            Path modulePath;
            String summary;

            if (module.isDefaultModule()) {
                moduleName = module.moduleName().packageName().toString();
                if (project.currentPackage().manifest().readme() == null) {
                    moduleMdText = "";
                } else {
                    Path readmePath = Paths.get(project.currentPackage().manifest().readme());
                    if (!readmePath.isAbsolute()) {
                        readmePath = project.sourceRoot().resolve(readmePath);
                    }
                    moduleMdText = Files.readString(readmePath);
                }
                modulePath = project.sourceRoot();
                summary = project.currentPackage().manifest().description();
            } else {
                moduleName = module.moduleName().toString();
                if (moduleMap.containsKey(moduleName)) {
                    if (moduleMap.get(moduleName).readme() == null || moduleMap.get(moduleName).readme().isEmpty()) {
                        moduleMdText = "";
                    } else {
                        moduleMdText = Files.readString(
                                project.sourceRoot().resolve(moduleMap.get(moduleName).readme()));
                    }
                } else {
                    moduleMdText = "";
                }
                modulePath = project.sourceRoot().resolve(ProjectConstants.MODULES_ROOT).resolve(module.moduleName()
                        .moduleNamePart());
                summary = moduleMap.get(moduleName).description();
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
            ModuleDoc moduleDoc = new ModuleDoc(moduleMdText, summary, resources,
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
            module.version = moduleVersion.isEmpty() ?
                    System.getProperty(BallerinaDocConstants.VERSION) :
                    moduleVersion;
            module.summary = moduleDoc.getValue().summary;
            module.description = moduleDoc.getValue().description;
            module.isDefaultModule = moduleDoc.getValue().isDefault;

            // collect module's doc resources
            module.resources.addAll(moduleDoc.getValue().resources);

            // Loop through bal files
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                Generator.setModuleFromSyntaxTree(module,
                        syntaxTreeMapEntry.getValue(), model);
            }
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
            try (Stream<Path> paths = Files.walk(resourcesDirPath)) {
                resources = paths.filter(path -> !path.equals(resourcesDirPath)).toList();
            }
        }
        return resources;
    }
}
