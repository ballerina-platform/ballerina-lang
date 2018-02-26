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

package org.ballerinalang.code.generator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BallerinaFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ModelPackage;
import org.ballerinalang.composer.service.ballerina.parser.service.util.ParserUtils;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import org.wso2.ballerinalang.compiler.tree.BLangService;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>This class generates Service definitions, clients or connectors for a provided ballerina service.</p>
 */
public class CodeGenerator {

    /**
     * Generates code(client, server, connector etc) for ballerina source provided  in <code>definitionPath</code>
     * <p>Method can be used for generating OAS 3.0.0, Swagger or Ballerina Client</p>
     *
     * @param type        Output type. Following types are supported
     *                    <ul>
     *                    <li>oas3</li>
     *                    <li>swagger</li>
     *                    <li>client</li>
     *                    </ul>
     * @param serviceNode Input Ballerina Service node to be process.
     * @param outPath     Destination file path to save generated source files. If not provided
     *                    <code>destinationPath</code> will be used as the default destination path'
     * @throws CodeGeneratorException when file operations fail
     * @Return generated source is string representation of generated source.
     */
    public String generateOutput(GeneratorConstants.GenType type, ServiceNode serviceNode, String outPath)
            throws CodeGeneratorException {
        ServiceNode context = serviceNode;
        String output = "";
        switch (type) {
            case CLIENT:
                return getConvertedString(context, GeneratorConstants.DEFAULT_CLIENT_DIR,
                        GeneratorConstants.CLIENT_TEMPLATE_NAME);
            case OPENAPI:
                return getConvertedString(context, GeneratorConstants.DEFAULT_OPEN_API_DIR,
                        GeneratorConstants.OPEN_API_TEMPLATE_NAME);
            case SWAGGER:
                return getConvertedString(context, GeneratorConstants.DEFAULT_SWAGGER_DIR,
                        GeneratorConstants.SWAGGER_TEMPLATE_NAME);
            default:
                return output;
        }
    }

    /**
     * Write generated definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws CodeGeneratorException when file operations fail
     */
    public void writeGeneratedSource(Object object, String templateDir, String templateName, String outPath)
            throws CodeGeneratorException {

        try (PrintWriter writer = new PrintWriter(outPath, "UTF-8")) {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
            try {
                writer.println(template.apply(context));
            } catch (IOException e) {
                throw new CodeGeneratorException("Error while writing converted string", e);
            }
        } catch (FileNotFoundException e) {
            throw new CodeGeneratorException("Error while writing converted string due to output file not found",
                    e);
        } catch (UnsupportedEncodingException e) {
            throw new CodeGeneratorException("Error while writing converted string due to unsupported encoding",
                    e);
        }
    }


    /**
     * Get converted string for given service definition.
     *
     * @param object       Object to be build as parsable context
     * @param templateDir  Template directory which contains templates for specific output type.
     * @param templateName Name of the template to be use for code generation.
     * @return generated string representation of passed object(ballerina service node)
     * @throws CodeGeneratorException when error occurs while compile, build context.
     */
    public String getConvertedString(Object object, String templateDir, String templateName)
            throws CodeGeneratorException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new CodeGeneratorException("Error while generating converted string", e);
        }
    }


    /**
     * This method will compile and return template of passed template definition.
     *
     * @param defaultTemplateDir template directory which contains set of templates
     * @param templateName       template file name to be used as template
     * @return compiled template generated for template definition.
     * @throws CodeGeneratorException throws IOException when compilation error occurs.
     */
    private Template compileTemplate(String defaultTemplateDir, String templateName) throws CodeGeneratorException {
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

        try {
            return handlebars.compile(templateName);
        } catch (IOException e) {
            throw new CodeGeneratorException("Error while compiling template", e);
        }
    }


    /**
     * This method is responsible for generating code based on provided ballerina source and service name.
     *
     * @param genType         Generation type used to generate code.
     * @param ballerinaSource Ballerina string source.
     * @param serviceName     Service name to be used to generate code(if multiple service available within ballerina
     *                        source).
     * @param outPath         Output path to be written generated code.
     * @return generated string output which represent ballerina service(or its client).
     * @throws CodeGeneratorException when error occurs while conversion happens.
     */
    public String generate(GeneratorConstants.GenType genType, String ballerinaSource, String serviceName,
                           String outPath) throws CodeGeneratorException {
        // Get the ballerina model using the ballerina source code.
        BFile balFile = new BFile();
        balFile.setContent(ballerinaSource);
        BLangCompilationUnit topCompilationUnit = getTopLevelNodeFromBallerinaFile(balFile);
        String swaggerSource = StringUtils.EMPTY;
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangService) {
                ServiceNode serviceDefinition = (ServiceNode) topLevelNode;
                // Generate swagger string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        swaggerSource = generateOutput(genType, serviceDefinition, outPath);
                    }
                } else {
                    swaggerSource = generateOutput(genType, serviceDefinition, outPath);
                    break;
                }
            }
        }

        return swaggerSource;
    }


    /**
     * Generate ballerina fine from the String definition.
     *
     * @param bFile ballerina string definition
     * @return ballerina file created from ballerina string definition
     * @throws CodeGeneratorException IO exception
     */
    public static BLangCompilationUnit getTopLevelNodeFromBallerinaFile(BFile bFile) throws CodeGeneratorException {

        String filePath = bFile.getFilePath();
        String fileName = bFile.getFileName();
        String content = bFile.getContent();

        org.wso2.ballerinalang.compiler.tree.BLangPackage model;

        // Sometimes we are getting Ballerina content without a file in the file-system.
        if (!Files.exists(Paths.get(filePath, fileName))) {
            BallerinaFile ballerinaFile = ParserUtils.getBallerinaFileForContent(fileName, content,
                    CompilerPhase.CODE_ANALYZE);
            model = ballerinaFile.getBLangPackage();

        } else {
            BallerinaFile ballerinaFile = ParserUtils.getBallerinaFile(filePath, fileName);
            model = ballerinaFile.getBLangPackage();
        }

        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        ParserUtils.loadPackageMap("Current Package", model, modelPackage);


        return model.getCompilationUnits().stream()
                .filter(compUnit -> fileName.equals(compUnit.getName()))
                .findFirst()
                .orElse(null);
    }
}
