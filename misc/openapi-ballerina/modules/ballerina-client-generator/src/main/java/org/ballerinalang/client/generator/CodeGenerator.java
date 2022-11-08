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

package org.ballerinalang.client.generator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.ballerinalang.client.generator.exception.ClientGeneratorException;
import org.ballerinalang.client.generator.model.ClientContextHolder;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>This class generates Service definitions, clients for a provided ballerina service.</p>
 */
public class CodeGenerator {

    /**
     * Generates code(client, server etc) for ballerina source provided  in <code>definitionPath</code>.
     * <p>Method can be used for generating OAS 3.0.0 or Ballerina Client</p>
     *
     * @param type    Output type. Following types are supported
     *                <ul>
     *                <li>client</li>
     *                <li>openapi</li>
     *                </ul>
     * @param context Context details for generating the client
     * @return generated source is string representation of generated source.
     * @throws ClientGeneratorException when file operations fail
     */
    public static String generateOutput(GeneratorConstants.GenType type, ClientContextHolder context)
            throws ClientGeneratorException {
        String output = "";
        switch (type) {
            case CLIENT:
                return getConvertedString(context, GeneratorConstants.DEFAULT_CLIENT_DIR,
                        GeneratorConstants.CLIENT_TEMPLATE_NAME);
            case OPENAPI3:
                return getConvertedString(context, GeneratorConstants.DEFAULT_OPEN_API_DIR,
                        GeneratorConstants.OPEN_API_TEMPLATE_NAME);
            case OPENAPI:
                return getConvertedString(context, GeneratorConstants.DEFAULT_OPENAPI_DIR,
                        GeneratorConstants.OPENAPI_TEMPLATE_NAME);
            default:
                return output;
        }
    }

    /**
     * Write given file content to a file at <code>targetDirPath</code>.
     *
     * @param targetDirPath target dir path where the generated files to be created
     * @param fileName      file name to be created
     * @param fileContent   content to be written in to the file
     * @throws ClientGeneratorException when error occurred while generating the code
     */
    public static void writeFile(Path targetDirPath, String fileName, String fileContent)
            throws ClientGeneratorException {
        if (targetDirPath == null) {
            throw new ClientGeneratorException("Target file directory path is null.");
        }
        try {
            // Create parent directory if doesn't exist.
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
            }

            // Write the given file content in to a file.
            Path clientFilePath = Paths.get(targetDirPath.toString(), fileName);
            Files.write(clientFilePath, fileContent.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new ClientGeneratorException("Error while writing generated client to a file.",
                    e);
        }
    }


    /**
     * Get converted string for given service definition.
     *
     * @param object       Object to be built as parsable context
     * @param templateDir  Template directory which contains templates for specific output type.
     * @param templateName Name of the template to be use for code generation.
     * @return generated string representation of passed object(ballerina service node)
     * @throws ClientGeneratorException when error occurs while compile, build context.
     */
    private static String getConvertedString(Object object, String templateDir, String templateName)
            throws ClientGeneratorException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object).resolver(
                MapValueResolver.INSTANCE,
                JavaBeanValueResolver.INSTANCE,
                new CustomFieldValueResolver()).build();
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new ClientGeneratorException("Error while generating converted string", e);
        }
    }

    static class CustomFieldValueResolver extends FieldValueResolver {
        @Override
        protected Set<FieldWrapper> members(Class<?> clazz) {
            Set members = super.members(clazz);
            return (Set<FieldWrapper>) members.stream()
                    .filter(fw -> isValidField((FieldWrapper) fw))
                    .collect(Collectors.toSet());
        }

        boolean isValidField(FieldWrapper fw) {
            if (fw instanceof AccessibleObject) {
                if (isUseSetAccessible(fw)) {
                    return true;
                }
                return false;
            }
            return true;
        }
    }


    /**
     * This method will compile and return template of passed template definition.
     *
     * @param defaultTemplateDir template directory which contains set of templates
     * @param templateName       template file name to be used as template
     * @return compiled template generated for template definition.
     * @throws ClientGeneratorException throws IOException when compilation error occurs.
     */
    private static Template compileTemplate(String defaultTemplateDir, String templateName)
            throws ClientGeneratorException {
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

        try {
            return handlebars.compile(templateName);
        } catch (IOException e) {
            throw new ClientGeneratorException("Error while compiling template", e);
        }
    }
}
