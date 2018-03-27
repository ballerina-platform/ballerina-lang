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
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.model.BallerinaOpenApi;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.ballerinalang.swagger.utils.GeneratorConstants.GenType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class generates Ballerina Services/Connectors for a provided OAS definition.
 */
public class CodeGenerator {
    private String apiPackage;

    /**
     * Generates ballerina source for provided Open API Definition in <code>definitionPath</code>
     * <p>Method can be user for generating Ballerina mock services and connectors</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>connector</li>
     *                       </ul>
     * @param definitionPath Input Open Api Definition file path
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       <code>definitionPath</code> will be used as the default destination path
     * @throws IOException when file operations fail
     */
    public void generate(GenType type, String definitionPath, String outPath) throws IOException,
            BallerinaOpenApiException {
        OpenAPI api = new OpenAPIV3Parser().read(definitionPath);
        BallerinaOpenApi definitionContext = new BallerinaOpenApi().buildContext(api).apiPackage(apiPackage)
                .modelPackage(apiPackage);
        String fileName = api.getInfo().getTitle().replaceAll(" ", "") + ".bal";
        outPath = outPath == null || outPath.isEmpty() ? "." : outPath;
        String destination =  outPath + File.separator + fileName;
        String modelDestination = outPath + File.separator + GeneratorConstants.MODELS_FILE_NAME;

        switch (type) {
            case CONNECTOR:
                writeBallerina(definitionContext, GeneratorConstants.DEFAULT_CONNECTOR_DIR,
                        GeneratorConstants.CONNECTOR_TEMPLATE_NAME, destination);

                // Write ballerina structs
                writeBallerina(definitionContext, GeneratorConstants.DEFAULT_TEMPLATE_DIR,
                        GeneratorConstants.MODELS_TEMPLATE_NAME, modelDestination);
                break;
            case MOCK:
                writeBallerina(definitionContext, GeneratorConstants.DEFAULT_MOCK_DIR,
                        GeneratorConstants.MOCK_TEMPLATE_NAME, destination);

                // Write ballerina structs
                writeBallerina(definitionContext, GeneratorConstants.DEFAULT_TEMPLATE_DIR,
                        GeneratorConstants.MODELS_TEMPLATE_NAME, modelDestination);
                break;
            default:
                return;
        }
    }

    /**
     * Write ballerina definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws IOException when file operations fail
     */
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

    public String getApiPackage() {
        return apiPackage;
    }

    public void setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
    }
}
