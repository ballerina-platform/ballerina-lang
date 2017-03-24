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
package org.ballerinalang.docgen.docs.html;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.DocumentWriter;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.TypeMapper;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.types.BType;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        init();
    }

    public HtmlDocumentWriter(String outputDir) {
        this.outputFilePath = outputDir;
        init();
    }

    private void init() {
        this.templatesFolderPath =
                System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, File.separator
                        + "docerina-templates" + File.separator + "html");
        this.packageTemplateName = System.getProperty(BallerinaDocConstants.PACKAGE_TEMPLATE_NAME_KEY, "package");
        this.indexTemplateName = System.getProperty(BallerinaDocConstants.INDEX_TEMPLATE_NAME_KEY, "index");
    }

    @Override
    public void write(Collection<BLangPackage> packages) throws IOException {
        if (packages == null || packages.size() == 0) {
            out.println("docerina: no package definitions found!");
            return;
        }

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("Generating HTML API documentation...");
        }

        // Create output directories
        Files.createDirectories(Paths.get(outputFilePath));

        // Sort packages by package path
        List<BLangPackage> packageList = new ArrayList<>(packages);
        Collections.sort(packageList, Comparator.comparing(BLangPackage::getPackagePath));

        // Write <package>.html files
        for (BLangPackage balPackage : packageList) {
            // Sort functions, connectors, structs and type mappers
            Arrays.sort(balPackage.getFunctions(), Comparator.comparing(Function::getName));
            Arrays.sort(balPackage.getConnectors(), Comparator.comparing(BallerinaConnectorDef::getName));
            Arrays.sort(balPackage.getStructDefs(), Comparator.comparing(StructDef::getName));
            Arrays.sort(balPackage.getTypeMappers(), Comparator.comparing(TypeMapper::getName));

            // Sort connector actions
            if ((balPackage.getConnectors() != null) && (balPackage.getConnectors().length > 0)) {
                for (BallerinaConnectorDef connector : balPackage.getConnectors()) {
                    Arrays.sort(connector.getActions(), Comparator.comparing(BallerinaAction::getName));
                }
            }

            String filePath = outputFilePath + File.separator + refinePackagePath(balPackage) + HTML;
            writeHtmlDocument(balPackage, packageTemplateName, filePath);
        }

        // Write index.html
        String filePath = outputFilePath + File.separator + INDEX_HTML;
        writeHtmlDocument(packageList, indexTemplateName, filePath);

        if (BallerinaDocUtils.isDebugEnabled()) {
            out.println("Copying HTML theme...");
        }
        BallerinaDocUtils.copyResources("docerina-theme", outputFilePath);
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
            Handlebars handlebars =
                    new Handlebars().with(new ClassPathTemplateLoader(templatesFolderPath), new FileTemplateLoader(
                            templatesFolderPath));
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
                    .registerHelper("hasTypeMappers", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getTypeMappers().length > 0) {
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
                            (Helper<VariableDef>) (param, options) -> {
                                String annotationName = options.param(0);
                                if (annotationName == null) {
                                    return "";
                                }
                                String subName = param.getName() == null ? param.getTypeName().getName() :
                                        param.getName();
                                for (AnnotationAttachment annotation : getAnnotations(dataHolder)) {
                                    if (isNameEqual(annotationName, annotation) && 
                                            annotation.getValue().startsWith(subName + ":")) {
                                        return annotation.getValue().split(subName + ":")[1].trim();
                                    }
                                }
                                // if the annotation values cannot be found still, return the first matching
                                // annotation's value
                                for (AnnotationAttachment annotation : getAnnotations(dataHolder)) {
                                    if (isNameEqual(annotationName, annotation)) {
                                        return annotation.getValue();
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
                        
                        for (AnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (isNameEqual(annotationName, annotation)) {
                                return annotation.getValue().trim();
                            }
                        }
                        return "";
                    })
                    //this would bind a link to the custom types defined
                    .registerHelper("bindLink", (Helper<SymbolName>) (type, options) -> {
                        if (type == null) {
                            // for native functions check the type name
                            if (options.context.model() instanceof ParameterDef) {
                                return "#" + ((ParameterDef) options.context.model()).getTypeName().toString();
                            }
                            return "";
                        }
                        if ((type.getPkgPath() != null) && (!type.getPkgPath().isEmpty())) {
                            return type.getPkgPath() + ".html#" + type.getName();
                        }
                        return "#" + type.getName();
                    })
                    // usage: {{typeTitle <BType>}}
                    // eg: {{typeTitle type}}
                    .registerHelper("typeTitle", (Helper<BType>) (type, options) -> {
                        if (type == null) {
                            // for native functions check the type name
                            if (options.context.model() instanceof ParameterDef) {
                                String pkgPath = ((ParameterDef) options.context.model()).getTypeName()
                                        .getPackagePath();
                                if (pkgPath != null && !pkgPath.isEmpty()) {
                                    return new Handlebars.SafeString(" title=\"" + type + "\"");
                                }
                            }
                            return null;
                        }
                        if ((type.getPackagePath() != null) && (!type.getPackagePath().isEmpty())) {
                            return new Handlebars.SafeString(" title=\"" + type + "\"");
                        }
                        return "";
                    })
                    .registerHelper("refinePackagePath", (Helper<BLangPackage>) (bLangPackage, options) -> {
                        if (bLangPackage == null) {
                            return null;
                        }
                        return refinePackagePath(bLangPackage);
                    });
            Template template = handlebars.compile(templateName);

            writer = new PrintWriter(absoluteFilePath, UTF_8);
            writer.println(template.apply(object));
            out.println("HTML file written: " + absoluteFilePath);
        } catch (IOException e) {
            out.println("docerina: could not write HTML file " + absoluteFilePath +
                    System.lineSeparator() + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    /**
     * Check whether an annotation has the provided fully qualified name.
     * 
     * @param annotationName fully qualified name
     * @param annotation Annotation to compare with
     * @return Flag indicating whether the annotation has the provided fully qualified name
     */
    private boolean isNameEqual(String annotationName, AnnotationAttachment annotation) {
        String [] pkgAndName = annotationName.split(":");
        return pkgAndName[0].equalsIgnoreCase(annotation.getPkgName()) && 
                pkgAndName[1].equalsIgnoreCase(annotation.getName());
    }

    /**
     * Returns ballerina file name if package name is set to "." otherwise
     * returns package path.
     *
     * @param bLangPackage ballerina package object
     * @return
     */
    private String refinePackagePath(BLangPackage bLangPackage) {
        if (bLangPackage == null) {
            return "";
        }
        if (bLangPackage.getName().equals(".") && (bLangPackage.getBallerinaFiles() != null) &&
                (bLangPackage.getBallerinaFiles().length > 0)) {
            return bLangPackage.getBallerinaFiles()[0].getFileName();
        }
        return bLangPackage.getPackagePath();
    }

    private AnnotationAttachment[] getAnnotations(DataHolder dataHolder) {
        if (dataHolder.getCurrentObject() instanceof BallerinaFunction) {
            return ((BallerinaFunction) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof BallerinaConnectorDef) {
            return ((BallerinaConnectorDef) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof BallerinaAction) {
            return ((BallerinaAction) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof BTypeMapper) {
            return ((BTypeMapper) dataHolder.getCurrentObject()).getAnnotations();
        } else if (dataHolder.getCurrentObject() instanceof StructDef) {
            return ((StructDef) dataHolder.getCurrentObject()).getAnnotations();
        } else {
            return new AnnotationAttachment[0];
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
