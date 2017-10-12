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
import org.ballerinalang.model.SymbolScope;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static final String DOC_PACKAGE_NAME = "ballerina.doc";

    private static PrintStream out = System.out;

    private String templatesFolderPath;
    private String outputFilePath;
    private String packageTemplateName;
    private String indexTemplateName;
    private String currentPkgPath;

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
        Collections.sort(packageList, Comparator.comparing(pkg -> pkg.getPackageDeclaration().toString()));

        // Write <package>.html files
        for (BLangPackage balPackage : packageList) {
            // Sort functions, connectors, structs, type mappers and annotationDefs
            Collections.sort(balPackage.getFunctions(), Comparator.comparing(f -> f.getName().getValue()));
            Collections.sort(balPackage.getConnectors(), Comparator.comparing(c -> c.getName().getValue()));
            Collections.sort(balPackage.getStructs(), Comparator.comparing(s -> s.getName().getValue()));
            Collections.sort(balPackage.getAnnotations(), Comparator.comparing(a -> a.getName().getValue()));
            
            // Sort connector actions
            if ((balPackage.getConnectors() != null) && (balPackage.getConnectors().size() > 0)) {
                balPackage.getConnectors().forEach(connector ->
                                                           Collections.sort(connector.getActions(),
                                                                            Comparator.comparing(
                                                                                    a -> a.getName().getValue())));
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
                        if (balPackage.getFunctions().size() > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    .registerHelper("hasConnectors", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getConnectors().size() > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    .registerHelper("hasStructs", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getStructs().size() > 0) {
                            return options.fn(balPackage);
                        }
                        return options.inverse(null);
                    })
                    .registerHelper("hasAnnotations", (Helper<BLangPackage>) (balPackage, options) -> {
                        if (balPackage.getAnnotations().size() > 0) {
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
                    .registerHelper("paramAnnotation", (Helper<BLangVariable>) (param, options) -> {
                        String annotationName = options.param(0);
                        if (annotationName == null) {
                            return "";
                        }
                        String subName =
                                param.getName() == null ? param.type.tsymbol.name.value : param.getName().getValue();
                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            String attribVal = annotation.getAttributes().get(0).getValue().getValue()
                                    .toString();
                            if (isNameEqual(annotationName, annotation) &&
                                    attribVal.startsWith(subName + ":")) {
                                return attribVal.split(subName + ":")[1].trim();
                            }
                        }
                        // if the annotation values cannot be found still, return the first matching
                        // annotation's value
                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            if (isNameEqual(annotationName, annotation)) {
                                return annotation.getAttributes().get(0).getValue().getValue().toString();
                            }
                        }
                        return "";
                    })
                    // for struct field annotations
                    .registerHelper("fieldAnnotation", (Helper<BLangVariable>) (param, options) -> {
                        String annotationName = options.param(0);
                        if (annotationName == null) {
                            return "";
                        }

                        String subName =
                                (param.getName() == null) ? param.type.tsymbol.name.value : param.getName().getValue();

                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            String attribVal = annotation.getAttributes().get(0).getValue().getValue().toString();
                            if (isNameEqual(annotationName, annotation) && attribVal.startsWith(subName + ":")) {
                                return attribVal.split(subName + ":")[1].trim();
                            }
                        }
                        // if the annotation values cannot be found still, return the first matching
                        // annotation's value
                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            if (isNameEqual(annotationName, annotation)) {
                                return annotation.getAttributes().get(0).getValue().getValue().toString();
                            }
                        }
                        return "";
                    })
                    // for annotation attribute annotations
                    .registerHelper("attributeAnnotation", (Helper<BLangAnnotAttribute>) (param, options) -> {
                        String annotationName = options.param(0);
                        if (annotationName == null) {
                            return "";
                        }

                        String subName =
                                (param.getName() == null) ? param.type.tsymbol.name.value : param.getName().getValue();

                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            String attribVal = annotation.getAttributes().get(0).getValue().getValue().toString();
                            if (isNameEqual(annotationName, annotation) && attribVal.startsWith(subName + ":")) {
                                return attribVal.split(subName + ":")[1].trim();
                            }
                        }
                        // if the annotation values cannot be found still, return the first matching
                        // annotation's value
                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            if (isNameEqual(annotationName, annotation)) {
                                return annotation.getAttributes().get(0).getValue().getValue().toString();
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

                        for (BLangAnnotationAttachment annotation : getAnnotations(dataHolder)) {
                            if (annotation.getAttributes().size() > 1) {
                                continue;
                            }
                            if (isNameEqual(annotationName, annotation)) {
                                return annotation.getAttributes().get(0).getValue().getValue().toString();
                            }
                        }
                        return "";
                    })
                    //this would bind a link to the custom types defined
                    .registerHelper("bindLink", (Helper<BLangType>) (type, options) -> {
                        BLangType bLangType = type;
                        if (type == null) {
                            // for native functions check the type name
                            if (options.context.model() instanceof BLangVariable) {
                                bLangType = ((BLangVariable) options.context.model()).typeNode;
                            } else {
                                return "";
                            }
                        }

                        if (bLangType instanceof BLangUserDefinedType) {
                            String[] fqTypeNameParts = getFullyQualifiedTypeName(bLangType).split(":");
                            if (fqTypeNameParts.length == 2) {
                                return  fqTypeNameParts[0] + ".html#" + fqTypeNameParts[1];
                            } else {
                                return "";
                            }
                        }
                        return "#" + getTypeName(bLangType);
                    })
                    // usage: {{typeTitle <BType>}}
                    // eg: {{typeTitle type}}
                    .registerHelper("typeTitle", (Helper<BLangType>) (type, options) -> {
                        if (type == null) {
                            // for native functions check the type name
                            if (options.context.model() instanceof BLangVariable) {
                                BLangType bLangType = ((BLangVariable) options.context.model()).getTypeNode();
                                return bLangType instanceof BLangUserDefinedType ? new Handlebars.SafeString(
                                        " title=\"" + getFullyQualifiedTypeName(bLangType) + "\"") : "";
                            }
                            return "";
                        }

                        return type instanceof BLangUserDefinedType ? new Handlebars.SafeString(
                                " title=\"" + getFullyQualifiedTypeName(type) + "\"") : "";
                    })
                    .registerHelper("typeText", (Helper<BLangType>) (type, options) -> getTypeName(type))
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
    private boolean isNameEqual(String annotationName, BLangAnnotationAttachment annotation) {
        String pkgPath = annotation.annotationSymbol.pkgID.name.value == null ? currentPkgPath :
                annotation.annotationSymbol.pkgID.name.value;
        return DOC_PACKAGE_NAME.equalsIgnoreCase(pkgPath) &&
                annotationName.equalsIgnoreCase(annotation.getAnnotationName().getValue());
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

        if (bLangPackage.getPosition().getSource().getPackageName().equals(".")) {
            return bLangPackage.getPosition().getSource().getCompilationUnitName();
        }
        return bLangPackage.symbol.pkgID.name.value;
    }

    private List<BLangAnnotationAttachment> getAnnotations(DataHolder dataHolder) {
        if (dataHolder.getCurrentObject() instanceof BLangFunction) {
            BLangFunction function = (BLangFunction) dataHolder.getCurrentObject();
            currentPkgPath = function.symbol.pkgID.name.value; //getPackagePath(function);
            return function.getAnnotationAttachments();
        } else if (dataHolder.getCurrentObject() instanceof BLangConnector) {
            BLangConnector connector = (BLangConnector) dataHolder.getCurrentObject();
            currentPkgPath = connector.symbol.pkgID.name.value; //getPackagePath(connector);
            return connector.getAnnotationAttachments();
        } else if (dataHolder.getCurrentObject() instanceof BLangAction) {
            BLangAction action = (BLangAction) dataHolder.getCurrentObject();
            currentPkgPath = action.symbol.pkgID.name.value; //getPackagePath(action);
            return action.getAnnotationAttachments();
        } else if (dataHolder.getCurrentObject() instanceof BLangStruct) {
            BLangStruct struct = (BLangStruct) dataHolder.getCurrentObject();
            currentPkgPath = struct.symbol.pkgID.name.value; //getPackagePath(struct);
            return struct.getAnnotationAttachments();
        } else if (dataHolder.getCurrentObject() instanceof BLangAnnotation) {
            BLangAnnotation annotation = (BLangAnnotation) dataHolder.getCurrentObject();
            currentPkgPath = annotation.symbol.pkgID.name.value; //getPackagePath(annotation);
            return annotation.getAnnotationAttachments();
        } else {
            return new ArrayList<>();
        }
    }

    private String getTypeName(BLangType bLangType) {
        return (bLangType instanceof BLangUserDefinedType ?
                ((BLangUserDefinedType) bLangType).typeName.value : bLangType.toString());
    }

    private String getFullyQualifiedTypeName(BLangType bLangType) {
        return (bLangType instanceof BLangUserDefinedType ?
                ((BLangUserDefinedType) bLangType).type.tsymbol.toString() : bLangType.toString());
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
    
    /**
     * Get the package to which the current scope belongs to.
     * 
     * @param scope Current scope
     * @return Package path to which current scope belongs to.
     */
    private String getPackagePath(SymbolScope scope) {
        if (scope instanceof BLangPackage) {
            return ((BLangPackage) scope).symbol.pkgID.name.value;
        }
        
        return getPackagePath(scope.getEnclosingScope());
    }
}
