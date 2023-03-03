/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

import io.ballerina.projects.JarResolver;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;

/**
 * Responsible of processing testerina mock annotations. This class is an implementation of the
 * {@link org.ballerinalang.compiler.plugins.CompilerPlugin}. Lifetime of an instance of this class will end upon the
 * completion of processing a ballerina package.
 */
@SupportedAnnotationPackages(
        value = "ballerina/test"
)
public class MockAnnotationProcessor extends AbstractCompilerPlugin {
    private static final String MOCK_ANNOTATION_NAME = "Mock";
    private static final String MODULE = "moduleName";
    private static final String FUNCTION = "functionName";
    private static final String MOCK_FN_DELIMITER = "#";
    private static final String MOCK_LEGACY_DELIMITER = "~";
    private static final String MODULE_DELIMITER = "§";

    private CompilerContext compilerContext;
    private DiagnosticLog diagnosticLog;
    private PackageCache packageCache;
    private Map<BPackageSymbol, SymbolEnv> packageEnvironmentMap;
    private SymbolResolver symbolResolver;
    private Types typeChecker;
    private final TesterinaRegistry registry = TesterinaRegistry.getInstance();

    /**
     * this property is used as a work-around to initialize test suites only once for a package as Compiler
     * Annotation currently emits package import events too to the process method.
     */

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
        this.packageEnvironmentMap = SymbolTable.getInstance(compilerContext).pkgEnvMap;
        this.packageCache = PackageCache.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);
        this.typeChecker = Types.getInstance(compilerContext);
    }

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.compilerContext = context;
    }

    // Extract mock function information
    @Override
    public void process(SimpleVariableNode simpleVariableNode, List<AnnotationAttachmentNode> annotations) {
        BLangPackage parent = (BLangPackage) ((BLangSimpleVariable) simpleVariableNode).parent;
        String packageName = getPackageName(parent);

        annotations = annotations.stream().distinct().collect(Collectors.toList());
        // Iterate through all the annotations
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            // Check if the package belongs to a single file project
            if (packageName.equals(DOT)) {
                diagnosticLog.logDiagnostic(
                        DiagnosticSeverity.ERROR, (ModuleDescriptor) null, attachmentNode.getPosition(),
                        "function mocking is not supported with standalone Ballerina files");
                return;
            }
            String annotationName = attachmentNode.getAnnotationName().getValue();
            if (MOCK_ANNOTATION_NAME.equals(annotationName)) {
                String type = ((BLangUserDefinedType) ((BLangSimpleVariable) simpleVariableNode).typeNode).
                        typeName.getValue();
                // Check if the simpleVariableNode that the annotation is attached to is in fact the MockFunction object
                if (type.equals("MockFunction")) {
                    String mockFnObjectName = simpleVariableNode.getName().getValue();
                    String[] annotationValues = new String[2]; // [0] - moduleName, [1] - functionName
                    annotationValues[0] = packageName; // Set default value of the annotation as the current package
                    if (null == attachmentNode.getExpression()
                            || attachmentNode.getExpression().getKind() != NodeKind.RECORD_LITERAL_EXPR) {
                        diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                                "missing required 'functionName' field");
                        continue;
                    }
                    // Get list of attributes in the Mock annotation
                    List<RecordLiteralNode.RecordField> fields =
                            ((BLangRecordLiteral) attachmentNode.getExpression()).getFields();
                    setAnnotationValues(fields, annotationValues, attachmentNode, parent);
                    PackageID functionToMockID = getPackageID(annotationValues[0]);
                    boolean validFunctionName = isValidFunctionName(
                            annotationValues[1], annotationValues[0], functionToMockID, attachmentNode);
                    if (!validFunctionName) {
                        return;
                    }
                    BLangTestablePackage bLangTestablePackage =
                            (BLangTestablePackage) ((BLangSimpleVariable) simpleVariableNode).parent;
                    // Value added to the map '<packageId> # <functionToMock> --> <MockFnObjectName>`
                    bLangTestablePackage.addMockFunction(
                            functionToMockID + MOCK_FN_DELIMITER + annotationValues[1],
                            mockFnObjectName);

                    if (functionToMockID != null) {
                        // Adding `<className> # <functionToMock> --> <MockFnObjectName>` to registry
                        String className = getQualifiedClassName(bLangTestablePackage,
                                functionToMockID.toString(), annotationValues[1]);
                        registry.addMockFunctionsSourceMap(bLangTestablePackage.packageID.getName().toString()
                                + MODULE_DELIMITER + className + MOCK_FN_DELIMITER + annotationValues[1],
                                mockFnObjectName);
                    }
                } else {
                    // Throw an error saying its not a MockFunction object
                    diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                            "Annotation can only be attached to a test:MockFunction object");
                }
            }
        }
    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        BLangPackage parent = (BLangPackage) ((BLangFunction) functionNode).parent;
        String packageName = getPackageName(parent);
        annotations = annotations.stream().distinct().collect(Collectors.toList());

        // Iterate through all the annotations
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationName = attachmentNode.getAnnotationName().getValue();
            String functionName = functionNode.getName().getValue();

            if (MOCK_ANNOTATION_NAME.equals(annotationName)) {
                String[] vals = new String[2];
                // TODO: when default values are supported in annotation struct we can remove this
                vals[0] = packageName;
                vals[1] = "";

                if (attachmentNode.getExpression() instanceof BLangRecordLiteral) {
                    List<RecordLiteralNode.RecordField> attributes = ((BLangRecordLiteral) attachmentNode
                            .getExpression()).getFields();
                    attributes.forEach(field -> {
                        String name;
                        BLangExpression valueExpr;

                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField attributeNode =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            name = attributeNode.getKey().toString();
                            valueExpr = attributeNode.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            name = varNameField.variableName.value;
                            valueExpr = varNameField;
                        }

                        String value = valueExpr.toString();

                        if (MODULE.equals(name)) {
                            value = formatPackageName(value, parent);
                            vals[0] = value;
                        } else if (FUNCTION.equals(name)) {
                            vals[1] = value;
                        }
                    });

                    // Check if Function in annotation is empty
                    if (vals[1].isEmpty()) {
                        diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                                "function name cannot be empty");
                        break;
                    }

                    // Find functionToMock in the packageID
                    PackageID functionToMockID = getPackageID(vals[0]);
                    if (functionToMockID == null) {
                        diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                                "could not find specified module '" + vals[0] + "'");
                        break;
                    }

                    BType functionToMockType = getFunctionType(packageEnvironmentMap, functionToMockID, vals[1]);
                    BType mockFunctionType = getFunctionType(packageEnvironmentMap, parent.packageID,
                            ((BLangFunction) functionNode).name.toString());

                    if (functionToMockType != null && mockFunctionType != null) {
                        if (!typeChecker.isAssignable(mockFunctionType, functionToMockType)) {
                            diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, ((BLangFunction) functionNode).pos,
                                    "incompatible types: expected " + functionToMockType
                                            + " but found " + mockFunctionType);
                            break;
                        }
                    } else {
                        diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                                "could not find function '" + vals[1] + "' in module '" + vals[0] + "'");
                        break;
                    }

                    //Creating a bLangTestablePackage to add a mock function
                    BLangTestablePackage bLangTestablePackage =
                            (BLangTestablePackage) ((BLangFunction) functionNode).parent;
                    bLangTestablePackage.addMockFunction(functionToMockID + MOCK_LEGACY_DELIMITER + vals[1],
                            functionName);

                    // Adding `<className> # <functionToMock> --> <MockFnObjectName>` to registry
                    String className = getQualifiedClassName(bLangTestablePackage,
                            functionToMockID.toString(), vals[1]);
                    vals[1] = vals[1].replaceAll("\\\\", "");
                    registry.addMockFunctionsSourceMap(bLangTestablePackage.packageID.getName().toString()
                                    + MODULE_DELIMITER + className + MOCK_LEGACY_DELIMITER + vals[1], functionName);
                } else {
                    diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                            "missing required 'functionName' field");
                }
            }
        }
    }

    /**
     * Iterate through each field and assign the annotation values for moduleName and functionName.
     *
     * @param fields list of fields
     * @param annotationValues Array of annotation values
     * @param attachmentNode AnnotationAttachmentNode
     * @param parent BLangPackage
     */
    private void setAnnotationValues(List<RecordLiteralNode.RecordField> fields, String[] annotationValues,
                                     AnnotationAttachmentNode attachmentNode, BLangPackage parent) {
        // Iterate through each field and assign the annotation values for moduleName and functionName
        fields.forEach(field -> {
            String name;
            BLangExpression valueExpr;
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField attributeNode =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                name = attributeNode.getKey().toString();
                valueExpr = attributeNode.getValue();
                String value = valueExpr.toString();
                if (MODULE.equals(name)) {
                    value = formatPackageName(value, parent);
                    annotationValues[0] = value;
                } else if (FUNCTION.equals(name)) {
                    annotationValues[1] = value;
                }
            } else {
                diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                        "Annotation fields must be key-value pairs");
            }
        });
    }

    /**
     * Returns a PackageID for the passed moduleName.
     *
     * @param moduleName Module name passed via function annotation
     * @return Module packageID
     */
    private PackageID getPackageID(String moduleName) {
        if (packageCache.getSymbol(moduleName) != null) {
            return packageCache.getSymbol(moduleName).pkgID;
        } else {
            return null;
        }
    }

    /**
     * Formats the package name obtained from the mock annotation.
     * Checks for empty, '.', or single module names and replaces them.
     * Ballerina modules and fully qualified packages are simply returned
     *
     * @param value package name
     * @return formatted package name
     */
    private String formatPackageName(String value, BLangPackage parent) {
        // If empty or '.' then return the current package ID
        if (value.isEmpty() || value.equals(Names.DOT.value)) {
            value = parent.packageID.toString();

            // If value does NOT contain 'ballerina/' then it could be fully qualified
        } else if (!value.contains(Names.ORG_NAME_SEPARATOR.value) && !value.contains(Names.VERSION_SEPARATOR.value)) {
            value = new PackageID(parent.packageID.orgName, new Name(value),
                    parent.packageID.version).toString();
        }
        return value;
    }

    /**
     * Validates the function name provided in the annotation.
     *
     * @param functionName   Name of the function to mock
     * @param attachmentNode MockFunction object attachment node
     * @return true if the provided function name valid
     */
    private boolean isValidFunctionName(String functionName, String moduleName, PackageID functionToMockID,
                                      AnnotationAttachmentNode attachmentNode) {
        if (functionToMockID == null) {
            diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                    "could not find specified module '" + moduleName + "'");
        } else {
            if (functionName == null) {
                diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                        "function name cannot be empty");
            } else {
                // Iterate through package map entries
                for (Map.Entry<BPackageSymbol, SymbolEnv> entry : this.packageEnvironmentMap.entrySet()) {
                    if (entry.getKey().pkgID.equals(functionToMockID)) {
                        // Check if the current package has the function name
                        if (entry.getValue().scope.entries.containsKey(new Name(functionName))) {
                            // Exit validate function if the function exists in the entry
                            return true;
                        }
                    }
                }
                // If it reaches this part, then the function has'nt been found in both packages
                diagnosticLog.logDiagnostic(DiagnosticSeverity.ERROR, attachmentNode.getPosition(),
                        "could not find function '" + functionName + "' in module '" + moduleName + "'");
            }
        }
        return false;
    }

    /**
     * Get Package Name.
     * @param packageNode PackageNode instance
     * @return package name
     */
    private String getPackageName(PackageNode packageNode) {
        BLangPackage bLangPackage = ((BLangPackage) packageNode);
        return bLangPackage.packageID.toString();
    }

    /**
     * Get the function type by iterating through the packageEnvironmentMap.
     *
     * @param pkgEnvMap    map of BPackageSymbol and its respective SymbolEnv
     * @param packageID    Fully qualified package ID of the respective function
     * @param functionName Name of the function
     * @return Function type if found, null if not found
     */
    private BType getFunctionType(Map<BPackageSymbol, SymbolEnv> pkgEnvMap, PackageID packageID, String functionName) {
        // Symbol resolver, Pass the acquired package Symbol from package cache
        for (Map.Entry<BPackageSymbol, SymbolEnv> entry : pkgEnvMap.entrySet()) {
            // Multiple packages may be present with same name, so all entries must be checked
            if (entry.getKey().pkgID.equals(packageID)) {
                BSymbol symbol = symbolResolver.lookupSymbolInMainSpace(entry.getValue(), new Name(functionName));
                if (!symbol.getType().toString().equals("other")) {
                    return symbol.getType();
                }
            }
        }
        return null;
    }

    private String getQualifiedClassName(BLangTestablePackage bLangTestablePackage,
                             String pkgId, String functionName) {
        String className;
        if (bLangTestablePackage.packageID.toString().equals(pkgId)) {
            if (bLangTestablePackage.symbol.scope.entries.containsKey(new Name(functionName))) {
                BSymbol symbol = bLangTestablePackage.symbol.scope.entries.get(new Name(functionName)).symbol;
                className = getClassName(bLangTestablePackage.symbol, symbol.getPosition());
            } else {
                BLangPackage parentPkg = bLangTestablePackage.parent;
                BSymbol symbol = parentPkg.symbol.scope.entries.get(new Name(functionName)).symbol;
                className = getClassName(parentPkg.symbol, symbol.getPosition());
            }

        } else {
            className = getImportedFunctionClassName(bLangTestablePackage,
                    pkgId, functionName);
        }
        return className;
    }

    private String getImportedFunctionClassName(BLangTestablePackage bLangTestablePackage,
                                                String pkgId, String functionName) {
        String className = getClassName(bLangTestablePackage.getImports(), pkgId, functionName);

        if (className == null) {
            className = getClassName(bLangTestablePackage.parent.getImports(), pkgId, functionName);
        }
        return className;
    }

    private String getClassName(List<BLangImportPackage> imports, String pkgId, String functionName) {
        for (BLangImportPackage importPackage : imports) {
            if (importPackage.symbol.pkgID.toString().equals(pkgId)) {
                BSymbol bInvokableSymbol = importPackage.symbol.scope.entries
                        .get(new Name(functionName)).symbol;
                return getClassName(importPackage.symbol, bInvokableSymbol.getPosition());
            }
        }
        return null;
    }

    private String getClassName(BPackageSymbol bPackageSymbol, Location pos) {
        return JarResolver.getQualifiedClassName(
                bPackageSymbol.pkgID.orgName.getValue(),
                bPackageSymbol.pkgID.name.getValue(),
                bPackageSymbol.pkgID.version.getValue(),
                pos.lineRange().filePath()
                        .replace(ProjectConstants.BLANG_SOURCE_EXT, "")
                        .replace(ProjectConstants.DOT, FILE_NAME_PERIOD_SEPARATOR)
                        .replace("/", ProjectConstants.DOT));
    }
}
