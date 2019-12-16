/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.BallerinaOpenApi;
import org.ballerinalang.openapi.model.GenSrcFile;
import org.ballerinalang.openapi.typemodel.OpenApiType;
import org.ballerinalang.openapi.utils.CodegenUtils;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.openapi.utils.GeneratorConstants.GenType;
import org.ballerinalang.openapi.utils.TypeMatchingUtil;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.openapi.model.GenSrcFile.GenFileType;
import static org.ballerinalang.openapi.utils.GeneratorConstants.GenType.GEN_CLIENT;
import static org.ballerinalang.openapi.utils.GeneratorConstants.MODULE_MD;

/**
 * This class generates Ballerina Services/Clients for a provided OAS definition.
 */
public class CodeGenerator {
    private String srcPackage;
    private String modelPackage;

    private static final PrintStream outStream = System.err;

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>client</li>
     *                       </ul>
     * @param executionPath  Command execution path
     * @param definitionPath Input Open Api Definition file path
     * @param serviceName    Output Service Name
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    private void generate(GenType type, String executionPath, String definitionPath,
                          String reldefinitionPath , String serviceName, String outPath)
            throws IOException, BallerinaOpenApiException {

        if (!CodegenUtils.isBallerinaProject(Paths.get(outPath))) {
            throw new BallerinaOpenApiException(OpenApiMesseges.GEN_CLIENT_PROJECT_ROOT);
        }

        //Check if the selected path is a ballerina root for service generation
        //TODO check with team for root check
        Path projectRoot = ProjectDirs.findProjectRoot(Paths.get(executionPath));
        if (type.equals(GenType.GEN_SERVICE) && projectRoot == null) {
            throw LauncherUtils.createUsageExceptionWithHelp(OpenApiMesseges.GEN_SERVICE_PROJECT_ROOT);
        }

        Path srcPath = CodegenUtils.getSourcePath(srcPackage, outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);

        if (type.equals(GEN_CLIENT)) {
            srcPath = srcPath.resolve("client");
            implPath = implPath.resolve("client");

            if (Files.notExists(srcPath)) {
                Files.createDirectory(srcPath);
            }

            if (Files.notExists(implPath)) {
                Files.createDirectory(implPath);
            }

        }

        List<GenSrcFile> genFiles = generateBalSource(type, definitionPath, reldefinitionPath, serviceName);
        writeGeneratedSources(genFiles, srcPath, implPath, type);
    }


    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * Method can be user for generating Ballerina clients.
     *
     * @param executionPath  Command execution path
     * @param definitionPath Input Open Api Definition file path
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateClient(String executionPath, String definitionPath, String outPath)
            throws IOException, BallerinaOpenApiException {
        generate(GenType.GEN_CLIENT, executionPath, definitionPath, null, null, outPath);
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * Method can be user for generating Ballerina clients.
     *
     * @param executionPath  Command execution path
     * @param definitionPath Input Open Api Definition file path
     * @param reldefinitionPath Relative definition path to be used in the generated ballerina code
     * @param serviceName    service name for the generated service
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateService(String executionPath, String definitionPath,
                                String reldefinitionPath, String serviceName, String outPath)
            throws IOException, BallerinaOpenApiException {
        generate(GenType.GEN_SERVICE, executionPath, definitionPath, reldefinitionPath, serviceName, outPath);
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated code will be returned as a list of source files
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>client</li>
     *                       </ul>
     * @param serviceName    Out put service name
     * @param definitionPath Input Open Api Definition file path
     * @param reldefinitionPath Relative OpenApi File
     * @return a list of generated source files wrapped as {@link GenSrcFile}
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when open api context building fail
     */
    public List<GenSrcFile> generateBalSource(GenType type, String definitionPath,
                                              String reldefinitionPath, String serviceName)
            throws IOException, BallerinaOpenApiException {
        OpenAPI api = new OpenAPIV3Parser().read(definitionPath);

        if (api == null) {
            throw new BallerinaOpenApiException("Couldn't read the definition from file: " + definitionPath);
        }

        if (serviceName != null) {
            api.getInfo().setTitle(serviceName);
        } else if (api.getInfo() == null || StringUtils.isEmpty(api.getInfo().getTitle())) {
            api.getInfo().setTitle(GeneratorConstants.UNTITLED_SERVICE);
        }

        final OpenApiType openApi = TypeMatchingUtil.traveseOpenApiTypes(api);
        openApi.setServiceName(serviceName);
        openApi.setModuleName(srcPackage);
        openApi.setServers(api);
        openApi.setTags(api.getTags());
        
        if (reldefinitionPath == null) {
            openApi.setDefinitionPath(definitionPath.replaceAll(Pattern.quote("\\"),
                    Matcher.quoteReplacement("\\\\")));
        } else {
            openApi.setDefinitionPath(reldefinitionPath.replaceAll(Pattern.quote("\\"),
                    Matcher.quoteReplacement("\\\\")));
        }

        List<GenSrcFile> sourceFiles;

        switch (type) {
            case GEN_CLIENT:
                // modelPackage is not in use at the moment. All models will be written into same package
                // as other src files.
                // Therefore value set to modelPackage is ignored here
                BallerinaOpenApi definitionContext = new BallerinaOpenApi().buildContext(api).srcPackage(srcPackage)
                        .modelPackage(srcPackage);
                definitionContext.setDefinitionPath(reldefinitionPath);

                sourceFiles = generateClient(definitionContext);
                break;
            case GEN_SERVICE:
                sourceFiles = generateBallerinaService(openApi);
                break;
            default:
                return null;
        }

        return sourceFiles;
    }

    /**
     * Write ballerina definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws IOException when file operations fail
     * @deprecated This method is now deprecated.
     * Use {@link #generateBalSource(GeneratorConstants.GenType, String, String, String) generate}
     * and implement a file write functionality your self, if you need to customize file writing steps.
     * Otherwise use {@link #generate(GeneratorConstants.GenType, String, String, String, String, String) generate}
     * to directly write generated source to a ballerina module.
     */
    @Deprecated
    public void writeBallerina(Object object, String templateDir, String templateName, String outPath)
            throws IOException {
        PrintWriter writer = null;

        try {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(
                    MapValueResolver.INSTANCE,
                    JavaBeanValueResolver.INSTANCE,
                    FieldValueResolver.INSTANCE).build();
            writer = new PrintWriter(outPath, "UTF-8");
            writer.println(template.apply(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private Template compileTemplate(String defaultTemplateDir, String templateName) throws IOException {
        defaultTemplateDir = defaultTemplateDir.replaceAll("\\\\", "/");
        String templatesDirPath = System.getProperty(GeneratorConstants.TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(GeneratorConstants.TEMPLATES_SUFFIX);
        fileTemplateLoader.setSuffix(GeneratorConstants.TEMPLATES_SUFFIX);

        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader);
        handlebars.registerHelpers(StringHelpers.class);
        handlebars.registerHelper("equals", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);

            if (param0 == null) {
                throw new IllegalArgumentException("found 'null', expected 'string'");
            }
            if (object != null) {
                if (object.toString().equals(param0.toString())) {
                    result = options.fn(options.context);
                } else {
                    result = options.inverse();
                }
            } else {
                result = null;
            }

            return result;
        });

        return handlebars.compile(templateName);
    }

    private void writeGeneratedSources(List<GenSrcFile> sources, Path srcPath, Path implPath, GenType type)
            throws IOException {
        // Remove old generated files - if any - before regenerate
        // if srcPackage was not provided and source was written to main package nothing will be deleted.
        if (srcPackage != null && !srcPackage.isEmpty() && Files.exists(srcPath)) {
            final File[] listFiles = new File(String.valueOf(srcPath)).listFiles();
            if (listFiles != null) {
                Arrays.stream(listFiles).forEach(file -> {
                    boolean deleteStatus = true;
                    if (!file.isDirectory() && !file.getName().equals(MODULE_MD)) {
                        deleteStatus = file.delete();
                    }

                    //Capture return value of file.delete() since if
                    //unable to delete returns false from file.delete() without an exception.
                    if (!deleteStatus) {
                        outStream.println("Unable to clean module directory.");
                    }
                });
            }

        }

        for (GenSrcFile file : sources) {
            Path filePath;

            // We only overwrite files of overwritable type.
            // So non overwritable files will be written to disk only once.
            if (!file.getType().isOverwritable()) {
                filePath = implPath.resolve(file.getFileName());
                if (Files.notExists(filePath)) {
                    CodegenUtils.writeFile(filePath, file.getContent());
                }
            } else {
                filePath = srcPath.resolve(file.getFileName());
                CodegenUtils.writeFile(filePath, file.getContent());
            }
        }

        //This will print the generated files to the console
        if (type.equals(GenType.GEN_SERVICE)) {
            outStream.println("Service generated successfully and the OpenApi contract is copied to " + srcPackage
                    + "/resources. this location will be referenced throughout the ballerina project.");
        } else if (type.equals(GEN_CLIENT)) {
            outStream.println("Client generated successfully.");
        }
        outStream.println("Following files were created. \n" +
                "src/ \n- " + srcPackage);
        Iterator<GenSrcFile> iterator = sources.iterator();
        while (iterator.hasNext()) {
            outStream.println("-- " + iterator.next().getFileName());
        }
    }

    /**
     * Generate code for ballerina client.
     *
     * @param context model context to be used by the templates
     * @return generated source files as a list of {@link GenSrcFile}
     * @throws IOException when code generation with specified templates fails
     */
    private List<GenSrcFile> generateClient(BallerinaOpenApi context) throws IOException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = GeneratorConstants.DEFAULT_CLIENT_PKG;
        }

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String srcFile = context.getInfo().getTitle().toLowerCase(Locale.ENGLISH)
                .replaceAll(" ", "_") + ".bal";

        // Generate ballerina service and resources.
        String mainContent = getContent(context, GeneratorConstants.DEFAULT_CLIENT_DIR,
                GeneratorConstants.CLIENT_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        // Generate ballerina records to represent schemas.
        String schemaContent = getContent(context, GeneratorConstants.DEFAULT_MODEL_DIR,
                GeneratorConstants.SCHEMA_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.MODEL_SRC, srcPackage, GeneratorConstants.SCHEMA_FILE_NAME,
                schemaContent));

        return sourceFiles;
    }

    private List<GenSrcFile> generateBallerinaService(OpenApiType api) throws IOException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = GeneratorConstants.DEFAULT_MOCK_PKG;
        }

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String concatTitle = api.getServiceName().toLowerCase(Locale.ENGLISH).replaceAll(" ", "_");
        String srcFile = concatTitle + ".bal";

        String mainContent = getContent(api, GeneratorConstants.DEFAULT_TEMPLATE_DIR + "/service",
                "serviceMock");
        sourceFiles.add(new GenSrcFile(GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        String schemaContent = getContent(api, GeneratorConstants.DEFAULT_TEMPLATE_DIR + "/service",
                "schema");
        sourceFiles.add(new GenSrcFile(GenFileType.GEN_SRC, srcPackage, GeneratorConstants.SCHEMA_FILE_NAME,
                schemaContent));

        return sourceFiles;
    }

    /**
     * Retrieve generated source content as a String value.
     *
     * @param object       context to be used by template engine
     * @param templateDir  templates directory
     * @param templateName name of the template to be used for this code generation
     * @return String with populated template
     * @throws IOException when template population fails
     */
    private String getContent(OpenApiType object, String templateDir, String templateName) throws IOException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object)
                .resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
        return template.apply(context);
    }

    /**
     * Retrieve generated source content as a String value.
     *
     * @param object       context to be used by template engine
     * @param templateDir  templates directory
     * @param templateName name of the template to be used for this code generation
     * @return String with populated template
     * @throws IOException when template population fails
     */
    private String getContent(BallerinaOpenApi object, String templateDir, String templateName) throws IOException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object)
                .resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
        return template.apply(context);
    }

    public String getSrcPackage() {
        return srcPackage;
    }

    public void setSrcPackage(String srcPackage) {
        this.srcPackage = srcPackage;
    }

    public String getModelPackage() {
        return modelPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }
}
