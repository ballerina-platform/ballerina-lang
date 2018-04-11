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

package org.ballerinalang.swagger;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.swagger.oas.models.OpenAPI;
import io.swagger.parser.v3.OpenAPIV3Parser;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.model.BallerinaOpenApi;
import org.ballerinalang.swagger.model.GenSrcFile;
import org.ballerinalang.swagger.utils.CodegenUtils;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.ballerinalang.swagger.utils.GeneratorConstants.GenType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.ballerinalang.swagger.model.GenSrcFile.GenFileType;

/**
 * This class generates Ballerina Services/Connectors for a provided OAS definition.
 */
public class CodeGenerator {
    private String srcPackage;
    private String modelPackage;

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina package at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and connectors</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>connector</li>
     *                       </ul>
     * @param definitionPath Input Open Api Definition file path
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generate(GenType type, String definitionPath, String outPath)
            throws IOException, BallerinaOpenApiException {
        if (!CodegenUtils.isBallerinaProject(Paths.get(outPath))) {
            throw new BallerinaOpenApiException("Output path is not a valid ballerina project directory. Use "
                    + "`ballerina init` to generate a new project");
        }

        Path srcPath = CodegenUtils.getSourcePath(srcPackage, outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generate(type, definitionPath);
        writeGeneratedSources(genFiles, srcPath, implPath);
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated code will be returned as a list of source files
     * <p>Method can be user for generating Ballerina mock services and connectors</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>connector</li>
     *                       </ul>
     * @param definitionPath Input Open Api Definition file path
     * @return a list of generated source files wrapped as {@link GenSrcFile}
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when open api context building fail
     */
    public List<GenSrcFile> generate(GenType type, String definitionPath)
            throws IOException, BallerinaOpenApiException {
        OpenAPI api = new OpenAPIV3Parser().read(definitionPath);

        // modelPackage is not in use at the moment. All models will be written into same package as other src files.
        // Therefore value set to modelPackage is ignored here
        BallerinaOpenApi definitionContext = new BallerinaOpenApi().buildContext(api).srcPackage(srcPackage)
                .modelPackage(srcPackage);
        List<GenSrcFile> sourceFiles;

        switch (type) {
            case CONNECTOR:
                sourceFiles = generateConnector(definitionContext);
                break;
            case MOCK:
                sourceFiles = generateMock(definitionContext);
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
     * @deprecated This method is now deprecated. Use {@link #generate(GenType, String)} and implement a
     * file write functionality your self, if you need to customize file writing steps.
     * Otherwise use {{@link #generate(GenType, String, String)}} to directly write generated sources
     * to a ballerina package.
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
            if (object != null && object.toString().equals(param0.toString())) {
                result = options.fn(options.context);
            } else {
                result = null;
            }

            return result;
        });

        return handlebars.compile(templateName);
    }

    private void writeGeneratedSources(List<GenSrcFile> sources, Path srcPath, Path implPath) throws IOException {
        // Remove old generated files - if any - before regenerate
        // if srcPackage was not provided and source was written to main package nothing will be deleted.
        if (srcPackage != null && !srcPackage.isEmpty() && Files.exists(srcPath)) {
            FileUtils.deleteDirectory(srcPath.toFile());
        }

        Files.createDirectories(srcPath);
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
    }

    /**
     * Generate code for ballerina connector.
     *
     * @param context model context to be used by the templates
     * @return generated source files as a list of {@link GenSrcFile}
     * @throws IOException when code generation with specified templates fails
     */
    private List<GenSrcFile> generateConnector(BallerinaOpenApi context) throws IOException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String srcFile = context.getInfo().getTitle().toLowerCase(Locale.ENGLISH).replaceAll(" ", "_") + ".bal";

        String mainContent = getContent(context, GeneratorConstants.DEFAULT_CONNECTOR_DIR,
                GeneratorConstants.CONNECTOR_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        // Generate ballerina structs
        String schemaContent = getContent(context, GeneratorConstants.DEFAULT_MODEL_DIR,
                GeneratorConstants.SCHEMA_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.MODEL_SRC, srcPackage, GeneratorConstants.SCHEMA_FILE_NAME,
                schemaContent));

        return sourceFiles;
    }

    /**
     * Generate code for mock ballerina service.
     *
     * @param context model context to be used by the templates
     * @return generated source files as a list of {@link GenSrcFile}
     * @throws IOException when code generation with specified templates fails
     */
    private List<GenSrcFile> generateMock(BallerinaOpenApi context) throws IOException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String concatTitle = context.getInfo().getTitle().toLowerCase(Locale.ENGLISH).replaceAll(" ", "_");
        String srcFile = concatTitle + ".bal";
        String implFile = concatTitle + "_impl.bal";

        String mainContent = getContent(context, GeneratorConstants.DEFAULT_MOCK_DIR,
                GeneratorConstants.MOCK_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        // Generate ballerina structs
        String schemaContent = getContent(context, GeneratorConstants.DEFAULT_MODEL_DIR,
                GeneratorConstants.SCHEMA_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.MODEL_SRC, srcPackage, GeneratorConstants.SCHEMA_FILE_NAME,
                schemaContent));

        // Generate resource implementation source
        String implContent = getContent(context, GeneratorConstants.DEFAULT_MOCK_DIR,
                GeneratorConstants.IMPL_TEMPLATE_NAME);
        sourceFiles.add(new GenSrcFile(GenFileType.IMPL_SRC, srcPackage, implFile, implContent));


        return sourceFiles;
    }

    /**
     * Retrieve generated source content as a String value.
     *
     * @param object       context to be used by temaplte engine
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
