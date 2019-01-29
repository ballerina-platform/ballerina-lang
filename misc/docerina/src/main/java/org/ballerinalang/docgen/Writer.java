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
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.model.AnnotationDoc;
import org.ballerinalang.docgen.model.ConstantDoc;
import org.ballerinalang.docgen.model.Documentable;
import org.ballerinalang.docgen.model.EndpointDoc;
import org.ballerinalang.docgen.model.EnumDoc;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.ObjectDoc;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.RecordDoc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the HTML pages from the Page objects.
 */
public class Writer {
    private static PrintStream out = System.out;

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
        String templatesFolderPath = System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, File
                .separator + "docerina-templates" + File.separator + "html");

        String templatesClassPath = System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY,
                "/docerina-templates/html");
        PrintWriter writer = null;
        try {
            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(templatesClassPath), new
                    FileTemplateLoader(templatesFolderPath));
            handlebars.registerHelpers(StringHelpers.class);
            handlebars.registerHelper("exists", new Helper<List<Documentable>>() {
                @Override
                public Object apply(List<Documentable> context, Options options) throws IOException {
                    String construct = options.param(0);
                    switch (construct) {
                        case "primitive":
                            return context.stream().anyMatch(c -> c instanceof PrimitiveTypeDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "type":
                            return context.stream().anyMatch(c -> c instanceof EnumDoc) ? options.fn(this)
                                                                                        : options.inverse(this);
                        case "annotation":
                            return context.stream().anyMatch(c -> c instanceof AnnotationDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "record":
                            return context.stream().anyMatch(c -> c instanceof RecordDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "object":
                            return context.stream().anyMatch(c -> c instanceof ObjectDoc) ? options.fn(this)
                                                                                          : options.inverse(this);
                        case "endpoint":
                            return context.stream().anyMatch(c -> c instanceof EndpointDoc) ? options.fn(this) :
                                   options.inverse(this);
                        case "function":
                            return context.stream().anyMatch(c -> c instanceof FunctionDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "globalvar":
                            return context.stream().anyMatch(c -> c instanceof GlobalVariableDoc) ? options.fn(this)
                                    : options.inverse(this);
                        case "constant":
                            return context.stream().anyMatch(c -> c instanceof ConstantDoc) ? options.fn(this)
                                    : options.inverse(this);
                    }
                    return false;
                }
            });
            handlebars.registerHelper("dataTypeLink", new Helper<String>() {
                @Override
                public Object apply(String dataType, Options options) throws IOException {
                    String href = options.param(0);
                    Map<String, String> typeToLink = new HashMap<>();
                    String[] types, hrefs;
                    hrefs = href.split(",");
                    types = dataType.split("\\(|\\)|,|\\|");
                    int idx = 0;
                    for (String type : types) {
                        type = type.trim();
                        if (!type.isEmpty()) {
                            if (idx >= hrefs.length) {
                                break;
                            }
                            typeToLink.putIfAbsent(type, getHtmlLink(type, hrefs[idx]));
                            idx++;
                        }
                    }
                    final String[] dataTypesWithLinks = {dataType};
                    typeToLink.forEach((type, link) -> {
                        dataTypesWithLinks[0] = dataTypesWithLinks[0].replaceAll(Pattern.quote(type), Matcher
                                .quoteReplacement(link));
                    });

                    return dataTypesWithLinks[0];
                }
            });
            Template template = handlebars.compile(packageTemplateName);

            writer = new PrintWriter(filePath, "UTF-8");

            Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
            writer.println(template.apply(context));
            out.println("docerina: HTML file written: " + filePath);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String getHtmlLink(String value, String href) {
        return "<a href=\"" + href + "\">" + value + "</a>";
    }
}
