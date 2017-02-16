/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerina.docgen.docs.html;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.docgen.docs.BallerinaDocConstants;
import org.wso2.ballerina.docgen.docs.DocumentWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * HTML document writer generates ballerina API documentation in HTML format.
 */
public class HtmlDocumentWriter implements DocumentWriter {

    private static final String HTML = ".html";
    private static final String INDEX_HTML = "index.html";
    private static final String UTF_8 = "UTF-8";

    private static PrintStream out = System.out;

    private String templatesFolderPath;
    private String outputFilePath;
    private String packageTemplateName;
    private String indexTemplateName;

    public HtmlDocumentWriter() {
        String userDir = System.getProperty("user.dir");
        this.outputFilePath =
                System.getProperty(BallerinaDocConstants.HTML_OUTPUT_PATH_KEY, userDir + File.separator + "api-docs"
                        + File.separator + "html");
        this.templatesFolderPath =
                System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, userDir + File.separator
                        + "templates" + File.separator + "html");
        this.packageTemplateName = System.getProperty(BallerinaDocConstants.PACKAGE_TEMPLATE_NAME_KEY, "package");
        this.indexTemplateName = System.getProperty(BallerinaDocConstants.INDEX_TEMPLATE_NAME_KEY, "index");
    }

    @Override
    public void write(Collection<BLangPackage> packages) {
        if (packages == null || packages.size() == 0) {
            out.println("No package definitions found!");
            return;
        }

        out.println("Generating HTML API documentation...");
        for (BLangPackage balPackage : packages) {
            String filePath = outputFilePath + File.separator + balPackage.getPackagePath() + HTML;
            writeHtmlDocument(balPackage, packageTemplateName, filePath);
        }
        String filePath = outputFilePath + File.separator + INDEX_HTML;
        writeHtmlDocument(packages, indexTemplateName, filePath);
    }

    /**
     * Write HTML document using a data object
     *
     * @param object           object to be passed to hbs template
     * @param templateName     hbs template name without the extension
     * @param absoluteFilePath absolute file path of the output html file
     */
    private void writeHtmlDocument(Object object, String templateName, String absoluteFilePath) {
        PrintWriter writer = null;
        try {
            TemplateLoader templateLoader = new FileTemplateLoader(templatesFolderPath);
            Handlebars handlebars = new Handlebars(templateLoader);
            DataHolder dataHolder = DataHolder.getInstance();
            handlebars
                    .registerHelper("hasFunctions", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getFunctions().length > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    .registerHelper("hasConnectors", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getConnectors().length > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    .registerHelper("hasStructs", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getStructDefs().length > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    // usage: {{currentObject this}}
                    .registerHelper("currentObject", (Helper<Object>) (obj, options) -> {
                        dataHolder.setCurrentObject(obj);
                        return null;
                    })
                    // usage: {{paramAnnotation this "<annotationName>"}}
                    // eg: {{paramAnnotation this "param"}}
                    .registerHelper(
                            "paramAnnotation",
                            (Helper<ParameterDef>) (param, options) -> {
                                String annotationName = options.param(0);
                                if (annotationName == null || param.getName() == null) {
                                    return "";
                                }
                                String subName = param.getName();
                                for (Annotation annotation : getAnnotations(dataHolder)) {
                                    if (annotationName.equalsIgnoreCase(annotation.getName())
                                            && annotation.getValue().startsWith(subName + ":")) {
                                        return annotation.getValue().split(subName + ":")[1].trim();
                                    }
                                }
                                return "";
                    })
                    // usage: {{oneValueAnnotation "<annotationName>"}}
                    // eg: {{oneValueAnnotation "description"}} - this would retrieve the description annotation of the
                    // currentObject
                    .registerHelper("oneValueAnnotation", (Helper<String>) (annotationName, options) -> {
                        if (annotationName == null) {
                            return null;
                        }
                        for (Annotation annotation : getAnnotations(dataHolder)) {
                            if (annotationName.equalsIgnoreCase(annotation.getName())) {
                                return annotation.getValue().trim();
                            }
                        }
                        return "";
                    })
                    //this would bind a link to the custom types defined
                    .registerHelper("bindLink", (Helper<SymbolName>) (type, options) -> {
                        if ((type.getPkgPath() != null) && (!type.getPkgPath().isEmpty())) {
                            return type.getPkgPath() + ".html#" + type.getName();
                        }
                        return "#" + type.getName();
                    })
                    // usage: {{typeTitle <BType>}}
                    // eg: {{typeTitle type}}
                    .registerHelper("typeTitle", (Helper<BType>) (type, options) -> {
                        if (type == null) {
                            return null;
                        }
                        if ((type.getPackagePath() != null) && (!type.getPackagePath().isEmpty())) {
                            return new Handlebars.SafeString(" title=\"" + type + "\"");
                        }
                        return "";
                    });
            Template template = handlebars.compile(templateName);

            writer = new PrintWriter(absoluteFilePath, UTF_8);
            writer.println(template.apply(object));
            out.println("HTML file written: " + absoluteFilePath);
        } catch (IOException e) {
            out.println("Docerina: Could not write HTML file " + absoluteFilePath +
                    System.lineSeparator() + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private Annotation[] getAnnotations(DataHolder dataHolder) {
        if (dataHolder.getCurrentObject() instanceof BallerinaFunction) {
            return ((BallerinaFunction) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof BallerinaConnectorDef) {
            return ((BallerinaConnectorDef) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof BallerinaAction) {
            return ((BallerinaAction) dataHolder.getCurrentObject()).getAnnotations();
        } else {
            return new Annotation[0];
        }
    }

    /**
     * Holds the current object which is processed by Handlebars
     */
    static class DataHolder {

        private static final DataHolder instance = new DataHolder();
        private Object currentObject;

        public static DataHolder getInstance() {
            return instance;
        }

        public Object getCurrentObject() {
            return currentObject;
        }

        public void setCurrentObject(Object currentObject) {
            this.currentObject = currentObject;
        }
    }
}
