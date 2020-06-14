/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen;


import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.PageContext;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.docgen.generator.model.Variable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates the HTML pages from the Page objects.
 */
public class Writer {

    /**
     * Write the HTML document from the Page object for a bal package.
     *
     * @param object              Page object which is generated from the bal package.
     * @param packageTemplateName hbs template file to be used.
     * @param filePath            path of the file to write the output.
     * @throws IOException on an IO error.
     */
    public static void writeHtmlDocument(Object object, String packageTemplateName, String filePath) throws
            IOException {
        String templatesFolderPath = System.getProperty("ballerina.home") + File.separator + "lib" + File.separator +
                "templates";

        String templatesClassPath = System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY,
                "/template/html");
        PrintWriter writer = null;
        try {
            Handlebars handlebars = new Handlebars().with(new FileTemplateLoader(templatesFolderPath),
                    new ClassPathTemplateLoader(templatesClassPath));
            handlebars.registerHelpers(StringHelpers.class);
            handlebars.registerHelper("paramSummary", (Helper<List<DefaultableVariable>>)
                    (varList, options) -> varList.stream()
                            .map(variable -> getTypeLabel(variable.type, options.context) + " " + variable.name)
                            .collect(Collectors.joining(", "))
            );
            handlebars.registerHelper("returnParamSummary", (Helper<List<Variable>>)
                    (varList, options) -> varList.stream()
                            .map(variable -> getTypeLabel(variable.type, options.context) + " " + variable.name)
                            .collect(Collectors.joining(", "))
            );
            handlebars.registerHelper("unionTypeSummary", (Helper<List<Type>>)
                    (typeList, options) -> typeList.stream()
                            .map(type -> getTypeLabel(type, options.context))
                            .collect(Collectors.joining(" | "))
            );
            handlebars.registerHelper("pipeJoin", (Helper<List<String>>)
                    (typeList, options) -> String.join(" | ", typeList)
            );
            handlebars.registerHelper("typeName", (Helper<Type>)
                    (type, options) -> getTypeLabel(type, options.context));

            handlebars.registerHelper("defVal", (Helper<Type>) (type, options) -> {
                String defaultValue;
                String name = options.param(0);
                Context context = options.context;
                String root = getRootPath(context);
                String link = root + type.moduleName + "/" + type.category + "/" + name + ".html";
                if (type.category.equals("objects") && !name.equals("()")) {
                    defaultValue = "<span class=\"default\">(default</span> <span class=\"type\">" +
                            "<a href=\"" + link + "\">" + name + "</a>" + "</span><span class=\"default\">)</span>";
                } else {
                    defaultValue = "<span class=\"default\">(default " + name + ")</span>";
                }
                return defaultValue;
            });

            handlebars.registerHelper("editDescription", (Helper<String>) (description, options) -> {
                //remove anything with <pre> tag
                String newDescription = description.replaceAll("<pre>(.|\\n)*?<\\/pre>", "");
                // select only the first sentence
                String[] splits = newDescription.split("\\.", 2);
                if (splits.length < 2) {
                    return splits[0];
                } else {
                    return splits[0] + ".";
                }
            });

            handlebars.registerHelper("setStyles", (Helper<String>) (description, options) -> {
                //set css for table tags
                String newDescription = description.replaceAll("<table>", "<table class=\"ui table row-border " +
                        "pad-left\">");
                return newDescription;
            });

            handlebars.registerHelper("removePTags", (Helper<String>) (string, options) -> {
                //remove paragraph tags
                if (string != null) {
                    return string.replaceAll("<\\/?p>", "");
                } else {
                    return "";
                }
            });

            handlebars.registerHelper("equals", (arg1, options) -> {
                CharSequence result;
                Object param0 = options.param(0);
                
                if (param0 == null) {
                    throw new IllegalArgumentException("found 'null', expected 'string'");
                }
                if (arg1 != null) {
                    if (arg1.toString().equals(param0.toString())) {
                        result = options.fn(options.context);
                    } else {
                        result = options.inverse();
                    }
                } else {
                    result = null;
                }
                
                return result;
            });
            Template template = handlebars.compile(packageTemplateName);

            writer = new PrintWriter(filePath, "UTF-8");

            Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
            writer.println(template.apply(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static String getTypeLabel(Type type, Context context) {
        String root = getRootPath(context);
        String label;
        if (type.isAnonymousUnionType) {
            label = type.memberTypes.stream()
                    .map(type1 -> getTypeLabel(type1, context))
                    .collect(Collectors.joining(" | "));
        } else if (type.isTuple) {
            label = "<span>[</span>" + type.memberTypes.stream()
                    .map(type1 -> getTypeLabel(type1, context))
                    .collect(Collectors.joining(", "))
                    + "<span>]</span>";
        } else if (type.isLambda) {
            label = "<code> <span>function(</span>" + type.paramTypes.stream()
                    .map(type1 -> getTypeLabel(type1, context))
                    .collect(Collectors.joining(", "))
                    + "<span>) </span>";
            if (type.returnType != null) {
                label += "<span>returns (</span>" + getTypeLabel(type.returnType, context) + "<span>)</span>";
            } else {
                label += "<span>() </span>";
            }
            label += " </code>";
        } else if (type.isArrayType) {
            label = "<span class=\"array-type\">" + getTypeLabel(type.elementType, context) + getSuffixes(type)
                    + "</span>";
        } else if ("builtin".equals(type.category) || "lang.annotations".equals(type.moduleName)
                || !type.generateUserDefinedTypeLink || "UNKNOWN".equals(type.category)) {
            label = "<span class=\"builtin-type\">" + type.name + getSuffixes(type) + "</span>";
        } else {
            label = getHtmlLink(type, root);
        }
        return label;
    }

    private static String getRootPath(Context context) {
        return getNearestPageContext(context).rootPath;
    }

    private static PageContext getNearestPageContext(Context context) {
        return context.model() instanceof PageContext
                ? (PageContext) context.model()
                : getNearestPageContext(context.parent());
    }

    private static String getHtmlLink(Type type, String root) {
        // TODO: Create links to other modules on central if they are not available locally
        // String orgName = BallerinaDocDataHolder.getInstance().getOrgName();
        // Map<String, ModuleDoc> packageMap = BallerinaDocDataHolder.getInstance().getPackageMap();
        String link = root + type.moduleName + "/" + type.category + "/" + type.name + ".html";
        if ("types".equals(type.category) || "constants".equals(type.category) || "annotations".equals(type.category)
                || "errors".equals(type.category)) {
            link = root + type.moduleName + "/" + type.category + ".html#" + type.name;
        }
        return "<a href=\"" + link + "\">" + type.name + "</a>" + getSuffixes(type);
    }

    private static String getSuffixes(Type type) {
        String suffix = type.isArrayType ? StringUtils.repeat("[]", type.arrayDimensions) : "";
        suffix += type.isNullable ? "?" : "";
        return suffix;
    }
}
