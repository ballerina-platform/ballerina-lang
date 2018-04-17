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
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.code.generator.model.ClientContextHolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * <p>This class generates Service definitions, clients for a provided ballerina service.</p>
 */
public class CodeGenerator {

    /**
     * Generates code(client, server etc) for ballerina source provided  in <code>definitionPath</code>.
     * <p>Method can be used for generating OAS 3.0.0, Swagger or Ballerina Client</p>
     *
     * @param type        Output type. Following types are supported
     *                    <ul>
     *                    <li>client</li>
     *                    <li>openapi</li>
     *                    <li>swagger</li>
     *                    </ul>
     * @param context Context details for generating the client
     * @return generated source is string representation of generated source.
     * @throws CodeGeneratorException when file operations fail
     */
    public String generateOutput(GeneratorConstants.GenType type, ClientContextHolder context)
            throws CodeGeneratorException {
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
     * Write generated source of a given <code>context</code> to a file at <code>outpath</code>.
     *
     * @param type    Generator Type to be used. Following types are supported.
     *                <ul>
     *                <li>client</li>
     *                <li>openapi</li>
     *                <li>swagger</li>
     *                </ul>
     * @param context Definition object containing required properties to be extracted for code generation
     * @param outPath resulting file path
     * @throws CodeGeneratorException when error occurred while generating the code
     */
    public void writeGeneratedSource(GeneratorConstants.GenType type, ClientContextHolder context, String outPath)
            throws CodeGeneratorException {

        try (PrintWriter writer = new PrintWriter(outPath, "UTF-8")) {
            String generatedSource = generateOutput(type, context);
            writer.println(generatedSource);
        } catch (FileNotFoundException e) {
            throw new CodeGeneratorException("Error while writing converted string due to output file not found", e);
        } catch (UnsupportedEncodingException e) {
            throw new CodeGeneratorException("Error while writing converted string due to unsupported encoding", e);
        }
    }


    /**
     * Get converted string for given service definition.
     *
     * @param object       Object to be built as parsable context
     * @param templateDir  Template directory which contains templates for specific output type.
     * @param templateName Name of the template to be use for code generation.
     * @return generated string representation of passed object(ballerina service node)
     * @throws CodeGeneratorException when error occurs while compile, build context.
     */
    public String getConvertedString(Object object, String templateDir, String templateName)
            throws CodeGeneratorException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object).resolver(
                MapValueResolver.INSTANCE,
                JavaBeanValueResolver.INSTANCE,
                FieldValueResolver.INSTANCE).build();
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

}
