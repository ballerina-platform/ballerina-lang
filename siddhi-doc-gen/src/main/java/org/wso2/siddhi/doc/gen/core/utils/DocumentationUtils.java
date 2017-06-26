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
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceMapper;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.stream.output.sink.SinkMapper;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.doc.gen.commons.ExampleMetaData;
import org.wso2.siddhi.doc.gen.commons.ExtensionMetaData;
import org.wso2.siddhi.doc.gen.commons.ExtensionType;
import org.wso2.siddhi.doc.gen.commons.NamespaceMetaData;
import org.wso2.siddhi.doc.gen.commons.ParameterMetaData;
import org.wso2.siddhi.doc.gen.commons.ReturnAttributeMetaData;
import org.wso2.siddhi.doc.gen.commons.SystemParameterMetaData;
import org.wso2.siddhi.doc.gen.core.freemarker.FormatDescriptionMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for getting the meta data for the extension processors in Siddhi
 */
public class DocumentationUtils {
    private static Map<ExtensionType, Class<?>> superClassMap;
    private static String[] templateFiles;

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

        // Populating the template files
        templateFiles = new String[]{"documentation.md.ftl"};
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
            ClassLoader urlClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

                }
            });
            // Getting extensions from all the class files in the classes directory
            addExtensionInDirectory(classesDirectory, classesDirectory.getAbsolutePath(), urlClassLoader,
                    namespaceMetaDataList, logger);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid classes directory: " + classesDirectory.getAbsolutePath(), e);
        }

        return namespaceMetaDataList;
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
                logger.warn("Discarding extension belonging to an unknown extension type: "
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
                parameter.setType(parameterAnnotation.type());
                parameter.setDescription(parameterAnnotation.description());
                parameter.setOptional(parameterAnnotation.optional());
                parameter.setDynamic(parameterAnnotation.dynamic());
                parameter.setDefaultValue(parameterAnnotation.defaultValue());
                parameters[i] = parameter;
            }
            extensionMetaData.setParameters(parameters);

            // Adding system parameters
            SystemParameterMetaData[] systemParameters =
                    new SystemParameterMetaData[extensionAnnotation.systemParameter().length];
            for (int i = 0; i < extensionAnnotation.systemParameter().length; i++) {
                SystemParameter systemParameterAnnotation = extensionAnnotation.systemParameter()[i];

                SystemParameterMetaData systemParameter = new SystemParameterMetaData();
                systemParameter.setName(systemParameterAnnotation.name());
                systemParameter.setDescription(systemParameterAnnotation.description());
                systemParameter.setDefaultValue(systemParameterAnnotation.defaultValue());
                systemParameter.setPossibleParameters(systemParameterAnnotation.possibleParameters());
                systemParameters[i] = systemParameter;
            }
            extensionMetaData.setSystemParameters(systemParameters);

            // Adding return attributes
            ReturnAttributeMetaData[] returnAttributes =
                    new ReturnAttributeMetaData[extensionAnnotation.returnAttributes().length];
            for (int i = 0; i < extensionAnnotation.returnAttributes().length; i++) {
                ReturnAttribute parameterAnnotation = extensionAnnotation.returnAttributes()[i];

                ReturnAttributeMetaData returnAttribute = new ReturnAttributeMetaData();
                returnAttribute.setName(parameterAnnotation.name());
                returnAttribute.setType(parameterAnnotation.type());
                returnAttribute.setDescription(parameterAnnotation.description());
                returnAttributes[i] = returnAttribute;
            }
            extensionMetaData.setReturnAttributes(returnAttributes);

            // Adding examples
            ExampleMetaData[] examples = new ExampleMetaData[extensionAnnotation.examples().length];
            for (int i = 0; i < extensionAnnotation.examples().length; i++) {
                Example exampleAnnotation = extensionAnnotation.examples()[i];

                ExampleMetaData exampleMetaData = new ExampleMetaData();
                exampleMetaData.setSyntax(exampleAnnotation.syntax());
                exampleMetaData.setDescription(exampleAnnotation.description());
                examples[i] = exampleMetaData;
            }
            extensionMetaData.setExamples(examples);

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
                namespace.setExtensionMap(new HashMap<String, List<ExtensionMetaData>>());
                namespaceList.add(namespace);
            }

            // Adding to the relevant extension metadata list in the namespace
            List<ExtensionMetaData> extensionMetaDataList = namespace.getExtensionMap().get(extensionType);
            if (extensionMetaDataList == null) {
                extensionMetaDataList = new ArrayList<>();
                namespace.getExtensionMap().put(extensionType, extensionMetaDataList);
            }

            extensionMetaDataList.add(extensionMetaData);
        }
    }

    /**
     * Generate documentation related files using metadata
     *
     * @param namespaceMetaDataList Metadata in this repository
     * @param outputDirectory       The path of the directory in which the documentation will be generated
     * @throws MojoFailureException if the Mojo fails to find template file or create new documentation file
     */
    public static void generateDocumentation(List<NamespaceMetaData> namespaceMetaDataList,
                                             String outputDirectory) throws MojoFailureException {
        // Creating the free marker configuration
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setClassForTemplateLoading(
                DocumentationUtils.class,
                File.separator + Constants.TEMPLATES_DIRECTORY
        );

        // Generating data model
        Map<String, Object> rootDataModel = new HashMap<>();
        rootDataModel.put("metaData", namespaceMetaDataList);
        rootDataModel.put("formatDescription", new FormatDescriptionMethod());

        for (String templateFile : templateFiles) {
            try {
                // Fetching the template
                Template template = cfg.getTemplate(templateFile);

                // Finding output file name by removing .ftl extension
                String fileName = templateFile.substring(0,
                        templateFile.length() - Constants.FREEMARKER_TEMPLATE_FILE_EXTENSION.length());

                // Generating empty documentation files
                File file = new File(outputDirectory + File.separator + fileName);
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
                        template.process(rootDataModel, writer);
                    }
                } catch (TemplateException e) {
                    throw new MojoFailureException("Invalid Free Marker template found in " + templateFile, e);
                }
            } catch (IOException e) {
                throw new MojoFailureException("Unable to find template file " + templateFile, e);
            }
        }
    }
}
