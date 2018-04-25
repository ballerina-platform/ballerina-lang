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
import org.ballerinalang.docgen.model.ConnectorDoc;
import org.ballerinalang.docgen.model.Documentable;
import org.ballerinalang.docgen.model.EnumDoc;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.RecordDoc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Generates the HTML pages from the Page objects.
 */
public class Writer {
    private static PrintStream out = System.out;

    /**
     * Write the HTML document from the Page object for a bal package.
     * @param object Page object which is generated from the bal package.
     * @param packageTemplateName hbs template file to be used.
     * @param filePath path of the file to write the output.
     */
    public static void writeHtmlDocument(Object object, String packageTemplateName, String filePath) {
        String templatesFolderPath = System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, File
                .separator + "docerina-templates" + File.separator + "html");
        PrintWriter writer = null;
        try {
            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(templatesFolderPath), new
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
                            return context.stream().anyMatch(c -> c instanceof EnumDoc) ? options.fn(this) : options
                                    .inverse(this);
                        case "annotation":
                            return context.stream().anyMatch(c -> c instanceof AnnotationDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "record":
                            return context.stream().anyMatch(c -> c instanceof RecordDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "object":
                            return context.stream().anyMatch(c -> {
                                if (c instanceof ConnectorDoc) {
                                    ConnectorDoc connectorDoc = (ConnectorDoc) c;
                                    if (connectorDoc.isObject) {
                                        return true;
                                    }
                                }
                                return false;
                            }) ? options.fn(this) : options.inverse(this);
                        case "endpoint":
                            return context.stream().anyMatch(c -> {
                                if (c instanceof ConnectorDoc) {
                                    ConnectorDoc connectorDoc = (ConnectorDoc) c;
                                    if (connectorDoc.isConnector) {
                                        return true;
                                    }
                                }
                                return false;
                            }) ? options.fn(this) : options.inverse(this);
                        case "function":
                            return context.stream().anyMatch(c -> c instanceof FunctionDoc) ? options.fn(this) :
                                    options.inverse(this);
                        case "globalvar":
                            return context.stream().anyMatch(c -> c instanceof GlobalVariableDoc) ? options.fn(this)
                                    : options.inverse(this);
                    }
                    return false;
                }
            });
            Template template = handlebars.compile(packageTemplateName);

            writer = new PrintWriter(filePath, "UTF-8");

            Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
            writer.println(template.apply(context));
            out.println("HTML file written: " + filePath);
        } catch (IOException e) {
            out.println("docerina: could not write HTML file " + filePath + System.lineSeparator() + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
