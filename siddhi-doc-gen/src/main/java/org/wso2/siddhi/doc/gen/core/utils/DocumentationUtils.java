/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.doc.gen.core.utils;

import com.google.common.io.Files;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.function.Script;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceMapper;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.stream.output.sink.SinkMapper;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.doc.gen.commons.metadata.ExampleMetaData;
import org.wso2.siddhi.doc.gen.commons.metadata.ExtensionMetaData;
import org.wso2.siddhi.doc.gen.commons.metadata.ExtensionType;
import org.wso2.siddhi.doc.gen.commons.metadata.NamespaceMetaData;
import org.wso2.siddhi.doc.gen.commons.metadata.ParameterMetaData;
import org.wso2.siddhi.doc.gen.commons.metadata.ReturnAttributeMetaData;
import org.wso2.siddhi.doc.gen.commons.metadata.SystemParameterMetaData;
import org.wso2.siddhi.doc.gen.core.freemarker.FormatDescriptionMethod;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for getting the meta data for the extension processors in Siddhi
 */
public class DocumentationUtils {
    private static Map<ExtensionType, Class<?>> superClassMap;

    static {
        // Populating the processor super class map
        superClassMap = new HashMap<>();
        superClassMap.put(ExtensionType.FUNCTION, FunctionExecutor.class);
        superClassMap.put(ExtensionType.ATTRIBUTE_AGGREGATOR, AttributeAggregator.class);
        superClassMap.put(ExtensionType.WINDOW, WindowProcessor.class);
        superClassMap.put(ExtensionType.STREAM_FUNCTION, StreamFunctionProcessor.class);
        superClassMap.put(ExtensionType.STREAM_PROCESSOR, StreamProcessor.class);
        superClassMap.put(ExtensionType.SOURCE, Source.class);
        superClassMap.put(ExtensionType.SINK, Sink.class);
        superClassMap.put(ExtensionType.SOURCE_MAPPER, SourceMapper.class);
        superClassMap.put(ExtensionType.SINK_MAPPER, SinkMapper.class);
        superClassMap.put(ExtensionType.STORE, Table.class);
        superClassMap.put(ExtensionType.SCRIPT, Script.class);
    }

    private DocumentationUtils() {   // To prevent instantiating utils class
    }

    /**
     * Returns the extension extension meta data
     * Gets the meta data from the siddhi manager
     *
     * @param targetDirectoryPath The path of the target directory of the maven module containing extensions
     * @param logger              The maven plugin logger
     * @return NamespaceMetaData namespace meta data list
     */
    public static List<NamespaceMetaData> getExtensionMetaData(String targetDirectoryPath,
                                                               List<String> runtimeClasspathElements,
                                                               Log logger)
            throws MojoFailureException, MojoExecutionException {
        List<NamespaceMetaData> namespaceMetaDataList = new ArrayList<>();
        int urlCount = runtimeClasspathElements.size() + 1;     // +1 to include the module's target/classes folder

        // Creating a list of URLs with all project dependencies
        URL[] urls = new URL[urlCount];
        for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            try {
                urls[i] = new File(runtimeClasspathElements.get(i)).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new MojoFailureException("Unable to access project dependency: "
                        + runtimeClasspathElements.get(i), e);
            }
        }

        File classesDirectory = new File(targetDirectoryPath + File.separator + Constants.CLASSES_DIRECTORY);
        try {
            // Adding the generated classes to the class loader
            urls[urlCount - 1] = classesDirectory.toURI().toURL();
            ClassLoader urlClassLoader = AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>) () -> new URLClassLoader(
                            urls, Thread.currentThread().getContextClassLoader()
                    )
            );
            // Getting extensions from all the class files in the classes directory
            addExtensionInDirectory(classesDirectory, classesDirectory.getAbsolutePath(), urlClassLoader,
                    namespaceMetaDataList, logger);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid classes directory: " + classesDirectory.getAbsolutePath(), e);
        }

        return namespaceMetaDataList;
    }


    /**
     * Generate documentation related files using metadata
     *
     * @param namespaceMetaDataList      Metadata in this repository
     * @param documentationBaseDirectory The path of the directory in which the documentation will be generated
     * @param documentationVersion       The version of the documentation being generated
     * @throws MojoFailureException if the Mojo fails to find template file or create new documentation file
     */
    public static void generateDocumentation(List<NamespaceMetaData> namespaceMetaDataList,
                                             String documentationBaseDirectory, String documentationVersion)
            throws MojoFailureException {
        // Generating data model
        Map<String, Object> rootDataModel = new HashMap<>();
        rootDataModel.put("metaData", namespaceMetaDataList);
        rootDataModel.put("formatDescription", new FormatDescriptionMethod());

        String outputFileRelativePath = Constants.API_SUB_DIRECTORY + File.separator + documentationVersion
                + Constants.MARKDOWN_FILE_EXTENSION;

        generateFileFromTemplate(
                Constants.MARKDOWN_DOCUMENTATION_TEMPLATE + Constants.MARKDOWN_FILE_EXTENSION
                        + Constants.FREEMARKER_TEMPLATE_FILE_EXTENSION,
                rootDataModel, documentationBaseDirectory, outputFileRelativePath
        );
    }

    /**
     * Update the documentation home page
     *
     * @param readMeFile                 The path to the read me file
     * @param documentationBaseDirectory The path of the base directory in which the documentation will be generated
     * @param homePageFileName           The name of the documentation file that will be generated
     * @param mkdocsConfigFile           The name of the mkdocs file
     * @param logger                     The maven plugin logger
     * @throws MojoFailureException if the Mojo fails to find template file or create new documentation file
     */
    public static void updateHomePage(File readMeFile, String documentationBaseDirectory,
                                      String homePageFileName, File mkdocsConfigFile, Log logger)
            throws MojoFailureException {
        // Retrieving the content of the README.md file
        List<String> readMeFileLines = new ArrayList<>();
        try {
            readMeFileLines = Files.readLines(readMeFile, Constants.DEFAULT_CHARSET);
        } catch (IOException ignored) {
        }

        // Retrieving the documentation file names
        File documentationDirectory = new File(documentationBaseDirectory
                + File.separator + Constants.API_SUB_DIRECTORY);
        String[] documentationFiles = documentationDirectory.list();

        // Getting only the markdown files
        List<String> documentationFilesList = new ArrayList<>();
        if (documentationFiles != null) {
            for (String documentationFile : documentationFiles) {
                if (documentationFile.endsWith(Constants.MARKDOWN_FILE_EXTENSION)) {
                    documentationFilesList.add(documentationFile);
                }
            }
        }

        // Generating data model
        Map<String, Object> rootDataModel = new HashMap<>();
        rootDataModel.put("readMeFileLines", readMeFileLines);
        rootDataModel.put("documentationFiles", documentationFilesList);

        generateFileFromTemplate(
                Constants.MARKDOWN_HOME_PAGE_TEMPLATE + Constants.MARKDOWN_FILE_EXTENSION
                        + Constants.FREEMARKER_TEMPLATE_FILE_EXTENSION,
                rootDataModel, documentationBaseDirectory,
                homePageFileName + Constants.MARKDOWN_FILE_EXTENSION
        );

        // Adding the links in the home page to the mkdocs config
        try {
            updateAPIPagesInMkdocsConfig(mkdocsConfigFile, documentationFilesList);
        } catch (FileNotFoundException e) {
            logger.warn("Unable to find mkdocs configuration file: " + mkdocsConfigFile.getAbsolutePath()
                    + ". Mkdocs configuration file not updated.");
        }
    }

    /**
     * This add a new page to the list of pages in the mkdocs configuration
     *
     * @param mkdocsConfigFile    The mkdocs configuration file
     * @param apiDirectoryContent The contents of the api directory
     */
    private static void updateAPIPagesInMkdocsConfig(File mkdocsConfigFile, List<String> apiDirectoryContent)
            throws MojoFailureException, FileNotFoundException {
        // Creating yaml parser
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);

        // Reading the mkdocs configuration
        Map<String, Object> yamlConfig;
        yamlConfig = (Map<String, Object>) yaml.load(new InputStreamReader(
                new FileInputStream(mkdocsConfigFile), Constants.DEFAULT_CHARSET)
        );

        // Getting the pages list
        List<Map<String, Object>> yamlConfigPagesList =
                (List<Map<String, Object>>) yamlConfig.get(Constants.MKDOCS_CONFIG_PAGES_KEY);

        // Creating the new api pages list
        List<Map<String, Object>> apiPagesList = new ArrayList<>();
        for (String apiFile : apiDirectoryContent) {
            String pageName = apiFile.substring(0, apiFile.length() - Constants.MARKDOWN_FILE_EXTENSION.length());

            Map<String, Object> newPage = new HashMap<>();
            newPage.put(pageName, Constants.API_SUB_DIRECTORY + Constants.MKDOCS_FILE_SEPARATOR + apiFile);
            apiPagesList.add(newPage);
        }

        // Setting the new api pages
        Map<String, Object> yamlConfigAPIPage = null;
        for (Map<String, Object> yamlConfigPage : yamlConfigPagesList) {
            if (yamlConfigPage.get(Constants.MKDOCS_CONFIG_PAGES_API_KEY) != null) {
                yamlConfigAPIPage = yamlConfigPage;
                break;
            }
        }
        if (yamlConfigAPIPage == null) {
            yamlConfigAPIPage = new HashMap<>();
            yamlConfigPagesList.add(yamlConfigAPIPage);
        }
        yamlConfigAPIPage.put(Constants.MKDOCS_CONFIG_PAGES_API_KEY, apiPagesList);

        // Saving the updated configuration
        yaml.dump(yamlConfig, new OutputStreamWriter(
                new FileOutputStream(mkdocsConfigFile), Constants.DEFAULT_CHARSET)
        );
    }

    /**
     * Generate a extension index file from the template file
     *
     * @param extensionRepositories      The list of extension repository names
     * @param extensionRepositoryOwner   The extension repository owner's name
     * @param documentationBaseDirectory The output directory path in which the extension index will be generated
     * @param extensionsIndexFileName    The name of the index file that will be generated
     */
    public static void createExtensionsIndex(List<String> extensionRepositories, String extensionRepositoryOwner,
                                             String documentationBaseDirectory, String extensionsIndexFileName)
            throws MojoFailureException {
        // Separating Apache and GPL extensions based on siddhi repository prefix conventions
        List<String> gplExtensionRepositories = new ArrayList<>();
        List<String> apacheExtensionRepositories = new ArrayList<>();
        for (String extensionRepository : extensionRepositories) {
            if (extensionRepository.startsWith(Constants.GITHUB_GPL_EXTENSION_REPOSITORY_PREFIX)) {
                gplExtensionRepositories.add(extensionRepository);
            } else if (extensionRepository.startsWith(Constants.GITHUB_APACHE_EXTENSION_REPOSITORY_PREFIX)) {
                apacheExtensionRepositories.add(extensionRepository);
            }
        }

        // Generating data model
        Map<String, Object> rootDataModel = new HashMap<>();
        rootDataModel.put("extensionsOwner", extensionRepositoryOwner);
        rootDataModel.put("gplExtensionRepositories", gplExtensionRepositories);
        rootDataModel.put("apacheExtensionRepositories", apacheExtensionRepositories);

        generateFileFromTemplate(
                Constants.MARKDOWN_EXTENSIONS_INDEX_TEMPLATE + Constants.MARKDOWN_FILE_EXTENSION
                        + Constants.FREEMARKER_TEMPLATE_FILE_EXTENSION,
                rootDataModel, documentationBaseDirectory,
                extensionsIndexFileName + Constants.MARKDOWN_FILE_EXTENSION
        );
    }

    /**
     * Search for class files in the directory and add extensions from them
     * This method recursively searches the sub directories
     *
     * @param directory             The directory from which the extension metadata will be loaded
     * @param classesDirectoryPath  The absolute path to the classes directory in the target folder in the module
     * @param urlClassLoader        The url class loader which should be used for loading the classes
     * @param namespaceMetaDataList List of namespace meta data
     * @param logger                The maven logger
     * @throws MojoExecutionException If failed to
     */
    private static void addExtensionInDirectory(File directory, String classesDirectoryPath,
                                                ClassLoader urlClassLoader,
                                                List<NamespaceMetaData> namespaceMetaDataList, Log logger)
            throws MojoExecutionException {
        File[] innerFiles = directory.listFiles();
        if (innerFiles != null) {
            for (File innerFile : innerFiles) {
                if (innerFile.isDirectory()) {
                    addExtensionInDirectory(innerFile, classesDirectoryPath, urlClassLoader, namespaceMetaDataList,
                            logger);
                } else {
                    addExtensionInFile(innerFile, classesDirectoryPath, urlClassLoader, namespaceMetaDataList,
                            logger);
                }
            }
        }
    }

    /**
     * Add an extension annotation in a file
     * This first checks if this is a class file and loads the class and adds the annotation if present
     *
     * @param file                  The file from which the extension metadata will be loaded
     * @param classesDirectoryPath  The absolute path to the classes directory in the target folder in the module
     * @param urlClassLoader        The url class loader which should be used for loading the classes
     * @param namespaceMetaDataList List of namespace meta data
     * @param logger                The maven logger
     * @throws MojoExecutionException If failed to load class
     */
    private static void addExtensionInFile(File file, String classesDirectoryPath, ClassLoader urlClassLoader,
                                           List<NamespaceMetaData> namespaceMetaDataList, Log logger)
            throws MojoExecutionException {
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(Constants.CLASS_FILE_EXTENSION) &&
                filePath.length() > classesDirectoryPath.length()) {
            String relativePathToClass = filePath.substring((classesDirectoryPath + File.separator).length());

            try {
                // Loading class
                Class<?> extensionClass = Class.forName(
                        relativePathToClass
                                .substring(0, relativePathToClass.length() - Constants.CLASS_FILE_EXTENSION.length())
                                .replace(File.separator, "."),
                        false, urlClassLoader
                );

                // Generating metadata and adding the it to the list of relevant extensions
                addExtensionMetaDataIntoNamespaceList(namespaceMetaDataList, extensionClass, logger);
            } catch (ClassNotFoundException ignored) {
                logger.warn("Ignoring the failed class loading from " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Generate extension meta data from the annotated data in the class
     *
     * @param namespaceList  The list of namespaces to which the new extension will be added
     * @param extensionClass Class from which meta data should be extracted from
     * @param logger         The maven plugin logger
     */
    private static void addExtensionMetaDataIntoNamespaceList(List<NamespaceMetaData> namespaceList,
                                                              Class<?> extensionClass,
                                                              Log logger)
            throws MojoExecutionException {
        Extension extensionAnnotation = extensionClass.getAnnotation(Extension.class);

        if (extensionAnnotation != null) {      // Discarding extension classes without annotation
            ExtensionMetaData extensionMetaData = new ExtensionMetaData();

            // Finding extension type
            String extensionType = null;
            for (Map.Entry<ExtensionType, Class<?>> entry : superClassMap.entrySet()) {
                Class<?> superClass = entry.getValue();
                if (superClass.isAssignableFrom(extensionClass) && superClass != extensionClass) {
                    extensionType = entry.getKey().getValue();
                    break;
                }
            }

            // Discarding the extension if it belongs to an unknown type
            if (extensionType == null) {
                logger.warn("Discarding extension (belonging to an unknown extension type): "
                        + extensionClass.getCanonicalName());
                return;
            }

            extensionMetaData.setName(extensionAnnotation.name());
            extensionMetaData.setDescription(extensionAnnotation.description());

            // Adding query parameters
            ParameterMetaData[] parameters = new ParameterMetaData[extensionAnnotation.parameters().length];
            for (int i = 0; i < extensionAnnotation.parameters().length; i++) {
                Parameter parameterAnnotation = extensionAnnotation.parameters()[i];

                ParameterMetaData parameter = new ParameterMetaData();
                parameter.setName(parameterAnnotation.name());
                parameter.setType(Arrays.asList(parameterAnnotation.type()));
                parameter.setDescription(parameterAnnotation.description());
                parameter.setOptional(parameterAnnotation.optional());
                parameter.setDynamic(parameterAnnotation.dynamic());
                parameter.setDefaultValue(parameterAnnotation.defaultValue());
                parameters[i] = parameter;
            }
            extensionMetaData.setParameters(Arrays.asList(parameters));

            // Adding system parameters
            SystemParameterMetaData[] systemParameters =
                    new SystemParameterMetaData[extensionAnnotation.systemParameter().length];
            for (int i = 0; i < extensionAnnotation.systemParameter().length; i++) {
                SystemParameter systemParameterAnnotation = extensionAnnotation.systemParameter()[i];

                SystemParameterMetaData systemParameter = new SystemParameterMetaData();
                systemParameter.setName(systemParameterAnnotation.name());
                systemParameter.setDescription(systemParameterAnnotation.description());
                systemParameter.setDefaultValue(systemParameterAnnotation.defaultValue());
                systemParameter.setPossibleParameters(Arrays.asList(systemParameterAnnotation.possibleParameters()));
                systemParameters[i] = systemParameter;
            }
            extensionMetaData.setSystemParameters(Arrays.asList(systemParameters));

            // Adding return attributes
            ReturnAttributeMetaData[] returnAttributes =
                    new ReturnAttributeMetaData[extensionAnnotation.returnAttributes().length];
            for (int i = 0; i < extensionAnnotation.returnAttributes().length; i++) {
                ReturnAttribute parameterAnnotation = extensionAnnotation.returnAttributes()[i];

                ReturnAttributeMetaData returnAttribute = new ReturnAttributeMetaData();
                returnAttribute.setName(parameterAnnotation.name());
                returnAttribute.setType(Arrays.asList(parameterAnnotation.type()));
                returnAttribute.setDescription(parameterAnnotation.description());
                returnAttributes[i] = returnAttribute;
            }
            extensionMetaData.setReturnAttributes(Arrays.asList(returnAttributes));

            // Adding examples
            ExampleMetaData[] examples = new ExampleMetaData[extensionAnnotation.examples().length];
            for (int i = 0; i < extensionAnnotation.examples().length; i++) {
                Example exampleAnnotation = extensionAnnotation.examples()[i];

                ExampleMetaData exampleMetaData = new ExampleMetaData();
                exampleMetaData.setSyntax(exampleAnnotation.syntax());
                exampleMetaData.setDescription(exampleAnnotation.description());
                examples[i] = exampleMetaData;
            }
            extensionMetaData.setExamples(Arrays.asList(examples));

            // Finding the namespace
            String namespaceName = extensionAnnotation.namespace();
            if (Objects.equals(namespaceName, "")) {
                namespaceName = Constants.CORE_NAMESPACE;
            }

            // Finding the relevant namespace in the namespace list
            NamespaceMetaData namespace = null;
            for (NamespaceMetaData existingNamespace : namespaceList) {
                if (Objects.equals(existingNamespace.getName(), namespaceName)) {
                    namespace = existingNamespace;
                    break;
                }
            }
            // Creating namespace if it doesn't exist
            if (namespace == null) {
                namespace = new NamespaceMetaData();
                namespace.setName(namespaceName);
                namespace.setExtensionMap(new HashMap<>());
                namespaceList.add(namespace);
            }

            // Adding to the relevant extension metadata list in the namespace
            List<ExtensionMetaData> extensionMetaDataList = namespace.getExtensionMap()
                    .computeIfAbsent(extensionType, k -> new ArrayList<>());

            extensionMetaDataList.add(extensionMetaData);
        }
    }

    /**
     * Generate a file from a template
     *
     * @param templateFile    The template file name
     * @param dataModel       The data model to be used for generating the output files from template files
     * @param outputDirectory The output directory in which the file will be generated
     * @param outputFileName  The name of the file that will be generated
     * @throws MojoFailureException if the Mojo fails to find template file or create new documentation file
     */
    private static void generateFileFromTemplate(String templateFile, Object dataModel,
                                                 String outputDirectory, String outputFileName)
            throws MojoFailureException {
        // Creating the free marker configuration
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setClassForTemplateLoading(
                DocumentationUtils.class,
                File.separator + Constants.TEMPLATES_DIRECTORY
        );

        try {
            // Fetching the template
            Template template = cfg.getTemplate(templateFile);

            // Generating empty documentation files
            File file = new File(outputDirectory + File.separator + outputFileName);
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new MojoFailureException("Unable to create directory " + file.getParentFile());
                }
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new MojoFailureException("Unable to create file " + file.getAbsolutePath());
                }
            }

            // Writing to the documentation file
            try (OutputStream outputStream = new FileOutputStream(file)) {
                try (Writer writer = new OutputStreamWriter(outputStream, Charset.defaultCharset())) {
                    template.process(dataModel, writer);
                }
            } catch (TemplateException e) {
                throw new MojoFailureException("Invalid Free Marker template found in " + templateFile, e);
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unable to find template file " + templateFile, e);
        }
    }
}
