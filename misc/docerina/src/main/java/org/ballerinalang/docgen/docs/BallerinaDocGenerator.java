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
import org.ballerinalang.docgen.generator.model.ApiDocsJson;
import org.ballerinalang.docgen.generator.model.BuiltInType;
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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        PackageLibrary packageLib = new PackageLibrary();
        CentralStdLibrary centralLib = new CentralStdLibrary();

        packageLib.releaseVersion = getBallerinaShortVersion().replace("sl", "swan-lake-");;
        packageLib.releaseShortVersion = getBallerinaShortVersion();
        packageLib.description = getReleaseDescription();

        centralLib.releaseVersion = getBallerinaShortVersion().replace("sl", "swan-lake-");
        centralLib.releaseShortVersion = getBallerinaShortVersion();
        centralLib.description = getReleaseDescription();

        for (File orgFile : orgFileList) {
            if (orgFile.isDirectory()) {
                File[] pkgFileList = orgFile.listFiles();
                Arrays.sort(pkgFileList);
                for (File pkgFile : pkgFileList) {
                    if (pkgFile.isDirectory() && pkgFile.listFiles().length > 0
                            && pkgFile.listFiles()[0].isDirectory()) {
                        File versionFile = pkgFile.listFiles()[0];
                        Path docJsonPath = Paths.get(versionFile.getAbsolutePath(), API_DOCS_JSON);
                        if (docJsonPath.toFile().exists()) {
                            try (BufferedReader br = Files.newBufferedReader(docJsonPath, StandardCharsets.UTF_8)) {
                                ApiDocsJson apiDocsJson = gson.fromJson(br, ApiDocsJson.class);
                                if (apiDocsJson.docsData.packages.isEmpty()) {
                                    log.warn("No packages found at: " + docJsonPath.toString());
                                    continue;
                                }
                                apiDocsJson.docsData.packages.forEach(docPackage -> {
                                    try {
                                        docPackage.resources
                                                .addAll(getResourcePaths(Paths.get(orgFile.getAbsolutePath())));
                                    } catch (IOException e) {
                                        log.error(String.format("API documentation generation failed. Cause: %s"
                                                , e.getMessage()), e);
                                        return;
                                    }
                                });
                                for (DocPackage docPackage : apiDocsJson.docsData.packages) {
                                    packageLib.packages.add(docPackage);
                                    DocPackageMetadata pkgMeta = new DocPackageMetadata();
                                    pkgMeta.name = docPackage.name;
                                    pkgMeta.summary = docPackage.summary;
                                    pkgMeta.orgName = docPackage.orgName;
                                    pkgMeta.version = docPackage.version;
                                    centralLib.packages.add(pkgMeta);
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
        List<BuiltInType> builtInTypes = getBuiltInTypesOrKeywords(packageLib.packages, BUILTIN_TYPES_DESCRIPTION_DIR);
        List<BuiltInType> keywords = getBuiltInTypesOrKeywords(packageLib.packages, BUILTIN_KEYWORDS_DESCRIPTION_DIR);

        packageLib.builtinTypes = builtInTypes;
        packageLib.keywords = keywords;
        centralLib.builtinTypes = builtInTypes;
        centralLib.keywords = keywords;
        packageLib.packages.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        centralLib.packages.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        writeAPIDocs(packageLib, apiDocsRoot, true, false);

        // Create the central standard library index json
        String stdIndexJson = gson.toJson(centralLib);
        File stdIndexJsonFile = apiDocsRoot.resolve(CENTRAL_STDLIB_INDEX_JSON).toFile();
        try (java.io.Writer writer = new OutputStreamWriter(new FileOutputStream(stdIndexJsonFile),
                StandardCharsets.UTF_8)) {
            writer.write(new String(stdIndexJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to create {} file.", CENTRAL_STDLIB_INDEX_JSON, e);
        }

        // Create the central standard library search json
        String stdSearchJson = gson.toJson(genSearchJson(packageLib));
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
            writeAPIDocs(packageLib, Path.of(output), false, excludeUI);
        }
    }

    private static void writeAPIDocs(PackageLibrary packageLib, Path output, boolean isMerge, boolean excludeUI) {
        if (packageLib.packages.size() == 0) {
            return;
        }
        if (!isMerge && packageLib.packages.get(0).modules.size() != 0) {
            output = packageLib.packages.get(0).name.equals("")
                    ? output.resolve(packageLib.packages.get(0).modules.get(0).orgName)
                    .resolve(packageLib.packages.get(0).modules.get(0).id).resolve(packageLib.packages.get(0).version)
                    : output.resolve(packageLib.packages.get(0).orgName).resolve(packageLib.packages.get(0).name)
                    .resolve(packageLib.packages.get(0).version);
        }
        try {
            Files.createDirectories(output);
            Files.createDirectories(output);
            genApiDocsJson(packageLib, output, excludeUI);
        } catch (IOException e) {
            log.error("API documentation generation failed:", e);
        }

        for (DocPackage docPackage: packageLib.packages) {
            if (!docPackage.resources.isEmpty()) {
                File resourcesDirFile = output.resolve("resources").toFile();
                if (BallerinaDocUtils.isDebugEnabled()) {
                    out.println("docerina: copying project resources ");
                }
                for (Path resourcePath : docPackage.resources) {
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
        // Copy docerina ui
        if (!excludeUI) {
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
                    //BallerinaDocUtils.unzipResources(, dest);
                } catch (IOException e) {
                    log.error("Failed to copy the doc ui.", e);
                }
            }
        }
    }

    private static void genApiDocsJson(PackageLibrary packageLib, Path destination, boolean excludeUI) {

        ApiDocsJson apiDocsJson = new ApiDocsJson();
        apiDocsJson.docsData = packageLib;
        apiDocsJson.searchData = genSearchJson(packageLib);

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

    private static SearchJson genSearchJson(PackageLibrary packageLib) {
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

        return new SearchJson(searchModules, searchClasses, searchFunctions, searchRecords,
                searchConstants, searchErrors, searchTypes, searchClients, searchListeners, searchAnnotations,
                searchObjectTypes, searchEnums);
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

    private static List<BuiltInType> getBuiltInTypesOrKeywords(List<DocPackage> packages, String dir) {

        List<BuiltInType> builtInTypesOrKeywords = new ArrayList<>();
        // Iterate through JAR resources and add builtin types and descriptions
        CodeSource src = BallerinaDocGenerator.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            try {
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null) {
                        break;
                    }
                    String name = e.getName();
                    if (name.startsWith(dir + "/")
                            && !name.equals(dir + "/")) {
                        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                        if (in != null) {
                            String builtinTypename = name
                                    .replace(dir + "/", "")
                                    .replace(".md", "");
                            String desc = new BufferedReader(new InputStreamReader(in)).lines()
                                    .collect(Collectors.joining("\n"));
                            Optional<DocPackage> relevantLangLib = packages.stream().filter(docPackage ->
                                    docPackage.name.equals("lang." + builtinTypename)).findFirst();
                            BuiltInType builtInType = new BuiltInType(builtinTypename, desc);
                            if (relevantLangLib.isPresent()) {
                                DocPackageMetadata relevantLangLibMetaData = new DocPackageMetadata();
                                relevantLangLibMetaData.name = relevantLangLib.get().name;
                                relevantLangLibMetaData.orgName = relevantLangLib.get().orgName;
                                relevantLangLibMetaData.version = relevantLangLib.get().version;
                                builtInType.langlib = relevantLangLibMetaData;
                            }
                            builtInTypesOrKeywords.add(builtInType);
                        }
                    }
                }
            } catch (IOException exp) {
                out.printf("docerina: API documentation generation failed at getting builtin type mds %n",
                        exp.getMessage());
                log.error("API documentation generation failed at getting builtin type mds:", exp);
            }
        }
        builtInTypesOrKeywords.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        return builtInTypesOrKeywords;
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
