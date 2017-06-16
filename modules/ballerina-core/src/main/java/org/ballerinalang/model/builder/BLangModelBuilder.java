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
package org.ballerinalang.model.builder;

import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.Operator;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.StructuredUnit;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.SemanticErrors;
import org.ballerinalang.util.exceptions.SemanticException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code BLangModelBuilder} provides an high-level API to create Ballerina language object model(AST).
 * <p>
 * Here we define constants, Structs, services symbols. Other symbols will be defined in the next phase
 *
 * @since 0.8.0
 */
public class BLangModelBuilder {
    public static final String ATTACHMENT_POINTS = "attachmentPoints";
    public static final String IF_CLAUSE = "IfClause";
    public static final String ELSE_CLAUSE = "ElseClause";
    public static final String TRY_CLAUSE = "TryClause";
    public static final String FINALLY_CLAUSE = "FinallyClause";

    protected String currentPackagePath;
    protected BallerinaFile.BFileBuilder bFileBuilder;

    protected SymbolScope currentScope;

    // Builds connectors and services.
    protected CallableUnitGroupBuilder currentCUGroupBuilder;

    // Builds functions, actions and resources.
    protected CallableUnitBuilder currentCUBuilder;

    // Keep the parent CUBuilder for worker
    protected Stack<CallableUnitBuilder> parentCUBuilder = new Stack<>();

    // Builds user defined structs.
    protected StructDef.StructBuilder currentStructBuilder;

    // Builds user defined annotations.
    protected AnnotationDef.AnnotationDefBuilder annotationDefBuilder;
    
    protected Stack<AnnotationAttachment.AnnotationBuilder> annonAttachmentBuilderStack = new Stack<>();
    protected Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    protected Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    protected Stack<TryCatchStmt.TryCatchStmtBuilder> tryCatchStmtBuilderStack = new Stack<>();

    protected Stack<TransactionStmt.TransactionStmtBuilder> transactionStmtBuilderStack = new Stack<>();

    protected Stack<ForkJoinStmt.ForkJoinStmtBuilder> forkJoinStmtBuilderStack = new Stack<>();
    protected Stack<List<Worker>> workerStack = new Stack<>();

    protected Stack<Expression> exprStack = new Stack<>();

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    protected Stack<List<Expression>> exprListStack = new Stack<>();
    
    protected Stack<List<KeyValueExpr>> mapStructKVListStack = new Stack<>();
    protected Stack<AnnotationAttachment> annonAttachmentStack = new Stack<>();

    // This variable keeps the package scope so that workers (and any global things) can be added to package scope
    protected SymbolScope packageScope = null;

    // This variable keeps the fork-join scope when adding workers and resolve back to current scope once done
    protected SymbolScope forkJoinScope = null;

    // This variable keeps the current scope when adding workers and resolve back to current scope once done
    protected Stack<SymbolScope> workerOuterBlockScope = new Stack<>();

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    protected Map<String, ImportPackage> importPkgMap = new HashMap<>();

    protected Stack<AnnotationAttributeValue> annotationAttributeValues = new Stack<AnnotationAttributeValue>();
    
    protected List<String> errorMsgs = new ArrayList<>();

    public BLangModelBuilder(BLangPackage.PackageBuilder packageBuilder, String bFileName) {
        this.currentScope = packageBuilder.getCurrentScope();
        this.packageScope = currentScope;
        //currentWorker.push("default");
        bFileBuilder = new BallerinaFile.BFileBuilder(bFileName, packageBuilder);
        currentPackagePath = ".";

    }

    public BallerinaFile build() {
        addImplicitImportPackages();
        importPkgMap.values()
                .stream()
                .filter(importPkg -> !importPkg.isUsed())
                .forEach(importPkg -> {
                    NodeLocation location = importPkg.getNodeLocation();
                    String pkgPathStr = "\"" + importPkg.getPath() + "\"";
                    String importPkgErrStr = (importPkg.getAsName() == null) ? pkgPathStr : pkgPathStr + " as '" +
                            importPkg.getAsName() + "'";

                    errorMsgs.add(BLangExceptionHelper
                            .constructSemanticError(location, SemanticErrors.UNUSED_IMPORT_PACKAGE, importPkgErrStr));
                });

        if (errorMsgs.size() > 0) {
            throw new SemanticException(errorMsgs.get(0));
        }

        return bFileBuilder.build();
    }

    public void addBFileWhiteSpaceRegion(int regionId, String whitespace) {
        if (bFileBuilder.getWhiteSpaceDescriptor() == null) {
            bFileBuilder.setWhiteSpaceDescriptor(new WhiteSpaceDescriptor());
        }
        bFileBuilder.getWhiteSpaceDescriptor()
                        .addWhitespaceRegion(regionId, whitespace);
    }


    // Packages and import packages

    public void addPackageDcl(NodeLocation location, String pkgPath) {
        currentPackagePath = pkgPath;
        bFileBuilder.setPackagePath(currentPackagePath);
        bFileBuilder.setPackageLocation(location);
    }

    public void addImplicitImportPackages() {
        if (!"ballerina.lang.errors".equals(currentPackagePath)) {
            ImportPackage error = new ImportPackage(null, null, "ballerina.lang.errors", "@error");
            error.setImplicitImport(true);
            bFileBuilder.addImportPackage(error);
            importPkgMap.put(error.getName(), error);
        }
    }

    public void addImportPackage(NodeLocation location, WhiteSpaceDescriptor wsDescriptor,
                                String pkgPath, String asPkgName) {
        ImportPackage importPkg;
        if (asPkgName != null) {
            importPkg = new ImportPackage(location, wsDescriptor, pkgPath, asPkgName);
        } else {
            importPkg = new ImportPackage(location, wsDescriptor, pkgPath);
        }

        if (importPkgMap.get(importPkg.getName()) != null) {
            String errMsg = BLangExceptionHelper
                    .constructSemanticError(location, SemanticErrors.REDECLARED_IMPORT_PACKAGE, importPkg.getName());
            errorMsgs.add(errMsg);
        }

        bFileBuilder.addImportPackage(importPkg);
        importPkgMap.put(importPkg.getName(), importPkg);
    }


    // Add constant definition

    public void addConstantDef(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                               SimpleTypeName typeName, String constName) {
        validateIdentifier(constName, location);
        Identifier identifier = new Identifier(constName);
        SymbolName symbolName = new SymbolName(identifier.getName());
        ConstDef constantDef = new ConstDef(location, whiteSpaceDescriptor, identifier, typeName, currentPackagePath,
                symbolName, currentScope, exprStack.pop());

        getAnnotationAttachments().forEach(attachment -> constantDef.addAnnotation(attachment));
        
        // Add constant definition to current file;
        bFileBuilder.addConst(constantDef);
    }


    // Add global variable definition
    public void addGlobalVarDef(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                SimpleTypeName typeName, String varName, boolean exprAvailable) {
        validateIdentifier(varName, location);
        Identifier identifier = new Identifier(varName);
        SymbolName symbolName = new SymbolName(identifier.getName());
        GlobalVariableDef globalVariableDef = new GlobalVariableDef(location, whiteSpaceDescriptor, identifier,
                typeName, currentPackagePath, symbolName, currentScope);

        getAnnotationAttachments().forEach(attachment -> globalVariableDef.addAnnotation(attachment));

        // Create Variable definition statement for the global variable
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, whiteSpaceDescriptor, identifier.getName());
        variableRefExpr.setVariableDef(globalVariableDef);

        Expression rhsExpr = exprAvailable ? exprStack.pop() : null;
        VariableDefStmt variableDefStmt = new VariableDefStmt(location, globalVariableDef, variableRefExpr, rhsExpr);
        globalVariableDef.setVariableDefStmt(variableDefStmt);

        // Add constant definition to current file;
        bFileBuilder.addGlobalVar(globalVariableDef);
    }


    // Add Struct definition

    /**
     * Start a struct builder.
     *
     * @param location Location of the struct definition in the source bal file
     */
    public void startStructDef(NodeLocation location) {
        currentStructBuilder = new StructDef.StructBuilder(location, currentScope);
        currentScope = currentStructBuilder.getCurrentScope();
    }

    /**
     * Add a field definition. Field definition can be a child of {@code StructDef} or {@code AnnotationDef}.
     *
     * @param location Location of the field in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param typeName Type name of the field definition
     * @param fieldName Name of the field in the {@link StructDef}
     * @param defaultValueAvailable has a default value or not
     */
    public void addFieldDefinition(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                   SimpleTypeName typeName, String fieldName, boolean defaultValueAvailable) {
        validateIdentifier(fieldName, location);
        Identifier identifier = new Identifier(fieldName);
        SymbolName symbolName = new SymbolName(identifier.getName());

        // Check whether this constant is already defined.
        BLangSymbol fieldSymbol = ((StructuredUnit) currentScope).resolveMembers(symbolName);
        if (fieldSymbol != null) {
            String errMsg = BLangExceptionHelper
                    .constructSemanticError(location, SemanticErrors.REDECLARED_SYMBOL, identifier.getName());
            errorMsgs.add(errMsg);
        }

        Expression defaultValExpr = null;
        if (defaultValueAvailable) {
            defaultValExpr = exprStack.pop();
        }
        
        if (currentScope instanceof StructDef) {
            VariableDef fieldDef = new VariableDef(location, null, identifier, typeName, symbolName, currentScope);
            VariableRefExpr fieldRefExpr = new VariableRefExpr(location, null, identifier.getName());
            fieldRefExpr.setVariableDef(fieldDef);
            VariableDefStmt fieldDefStmt = new VariableDefStmt(location, fieldDef, fieldRefExpr, defaultValExpr);
            fieldDefStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
            currentStructBuilder.addField(fieldDefStmt);
        } else if (currentScope instanceof AnnotationDef) {
            AnnotationAttributeDef annotationField = new AnnotationAttributeDef(location, identifier, typeName,
                (BasicLiteral) defaultValExpr, symbolName, currentScope, currentPackagePath);
            annotationField.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
            currentScope.define(symbolName, annotationField);
            annotationDefBuilder.addAttributeDef(annotationField);
        }
    }

    /**
     * Creates a {@link StructDef}.
     *
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param name Name of the {@link StructDef}
     */
    public void addStructDef(WhiteSpaceDescriptor whiteSpaceDescriptor, String name) {
        currentStructBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentStructBuilder.setIdentifier(new Identifier(name));
        currentStructBuilder.setPackagePath(currentPackagePath);
        getAnnotationAttachments().forEach(attachment -> currentStructBuilder.addAnnotation(attachment));

        StructDef structDef = currentStructBuilder.build();

        // Close Struct scope
        currentScope = structDef.getEnclosingScope();
        currentStructBuilder = null;

        bFileBuilder.addStruct(structDef);
    }


    // Annotations

    public void startAnnotationAttachment(NodeLocation location) {
        AnnotationAttachment.AnnotationBuilder annotationBuilder = new AnnotationAttachment.AnnotationBuilder();
        annotationBuilder.setNodeLocation(location);
        annonAttachmentBuilderStack.push(annotationBuilder);
    }

    public void createAnnotationKeyValue(WhiteSpaceDescriptor whiteSpaceDescriptor, String key) {
        AnnotationAttachment.AnnotationBuilder annotationBuilder = annonAttachmentBuilderStack.peek();
        if (whiteSpaceDescriptor != null) {
            WhiteSpaceDescriptor existingDescriptor = annotationAttributeValues.peek().getWhiteSpaceDescriptor();
            if (existingDescriptor != null) {
                annotationAttributeValues.peek().getWhiteSpaceDescriptor()
                        .getWhiteSpaceRegions().putAll(whiteSpaceDescriptor.getWhiteSpaceRegions());
            }
        }
        annotationBuilder.addAttributeNameValuePair(key, annotationAttributeValues.pop());
    }

    public void addAnnotationAttachment(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                        NameReference nameReference, int attributesCount) {
        AnnotationAttachment.AnnotationBuilder annonAttachmentBuilder = annonAttachmentBuilderStack.pop();
        annonAttachmentBuilder.setName(nameReference.getName());
        annonAttachmentBuilder.setPkgName(nameReference.getPackageName());
        annonAttachmentBuilder.setPkgPath(nameReference.getPackagePath());
        annonAttachmentBuilder.setNodeLocation(location);
        annonAttachmentBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        annonAttachmentStack.add(annonAttachmentBuilder.build());
    }

    /**
     * Start an annotation definition.
     * 
     * @param location Location of the annotation definition in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     */
    public void startAnnotationDef(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        annotationDefBuilder = new AnnotationDef.AnnotationDefBuilder(location, currentScope);
        annotationDefBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentScope = annotationDefBuilder.getCurrentScope();
    }
    
    /**
     * Creates a {@code AnnotationDef}.
     * 
     * @param location Location of this {@code AnnotationDef} in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param name Name of the {@code AnnotationDef}
     */
    public void addAnnotationtDef(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String name) {

        if (whiteSpaceDescriptor != null) {
            annotationDefBuilder.getWhiteSpaceDescriptor().getWhiteSpaceRegions()
                    .putAll(whiteSpaceDescriptor.getWhiteSpaceRegions());
        }
        validateIdentifier(name, location);
        annotationDefBuilder.setIdentifier(new Identifier(name));
        annotationDefBuilder.setPackagePath(currentPackagePath);
        
        getAnnotationAttachments().forEach(attachment -> annotationDefBuilder.addAnnotation(attachment));
        
        AnnotationDef annotationDef = annotationDefBuilder.build();
        bFileBuilder.addAnnotationDef(annotationDef);
        
        // Close annotation scope
        currentScope = annotationDef.getEnclosingScope();
        currentStructBuilder = null;
    }
    
    /**
     * Add a target to the annotation.
     * 
     * @param location Location of the target in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param attachmentPoint Point to which this annotation can be attached
     */
    public void addAnnotationtAttachmentPoint(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                              String attachmentPoint) {
        if (whiteSpaceDescriptor != null) {
            annotationDefBuilder.getWhiteSpaceDescriptor()
                    .getChildDescriptor(ATTACHMENT_POINTS)
                    .addChildDescriptor(attachmentPoint, whiteSpaceDescriptor);
        }
        annotationDefBuilder.addAttachmentPoint(attachmentPoint);
    }

    /**
     * Create a literal type attribute value.
     * 
     * @param location Location of the value in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     */
    public void createLiteralTypeAttributeValue(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression expr = exprStack.pop();
        if (!(expr instanceof BasicLiteral)) {
            String errMsg = BLangExceptionHelper.constructSemanticError(expr.getNodeLocation(),
                    SemanticErrors.UNSUPPORTED_ANNOTATION_ATTRIBUTE_VALUE);
            errorMsgs.add(errMsg);
        }
        BasicLiteral basicLiteral = (BasicLiteral) expr;
        BValue value = basicLiteral.getBValue();
        annotationAttributeValues.push(new AnnotationAttributeValue(value, basicLiteral.getTypeName(), location,
                whiteSpaceDescriptor));
    }
    
    /**
     * Create an annotation type attribute value.
     * 
     * @param location Location of the value in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     */
    public void createAnnotationTypeAttributeValue(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        AnnotationAttachment value = annonAttachmentStack.pop();
        SimpleTypeName valueType = new SimpleTypeName(value.getName(), value.getPkgName(), value.getPkgPath());
        annotationAttributeValues.push(new AnnotationAttributeValue(value, valueType, location, whiteSpaceDescriptor));
    }
    
    /**
     * Create an array type attribute value.
     * 
     * @param location Location of the value in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     */
    public void createArrayTypeAttributeValue(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        SimpleTypeName valueType = new SimpleTypeName(null, true, 1);
        AnnotationAttributeValue arrayValue = new AnnotationAttributeValue(
            annotationAttributeValues.toArray(new AnnotationAttributeValue[annotationAttributeValues.size()]),
            valueType, location, whiteSpaceDescriptor);
        arrayValue.setNodeLocation(location);
        annotationAttributeValues.clear();
        annotationAttributeValues.push(arrayValue);
    }

    // Function parameters and types

    /**
     * <p>Create a function parameter and a corresponding variable reference expression.</p>
     * Set the even function to get the value from the function arguments with the correct index.
     * Store the reference in the symbol table.
     *
     * @param location  Location of the parameter in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param typeName Type name of the parameter
     * @param paramName name of the function parameter
     * @param annotationCount number of annotations
     * @param isReturnParam return parameter or not
     */
    public void addParam(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, SimpleTypeName typeName,
                         String paramName, int annotationCount, boolean isReturnParam) {
        validateIdentifier(paramName, location);
        Identifier identifier = new Identifier(paramName);
        SymbolName symbolName = new SymbolName(identifier.getName(), currentPackagePath);

        // Check whether this parameter is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, identifier.getName());
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, whiteSpaceDescriptor, identifier, typeName, symbolName,
                                        currentScope);
        getAnnotationAttachments(annotationCount).forEach(attachment -> paramDef.addAnnotation(attachment));

        if (currentCUBuilder != null) {
            // Add the parameter to callableUnitBuilder.
            if (isReturnParam) {
                currentCUBuilder.addReturnParameter(paramDef);
            } else {
                currentCUBuilder.addParameter(paramDef);
            }

        } else {
            currentCUGroupBuilder.addParameter(paramDef);
        }

        currentScope.define(symbolName, paramDef);
    }

    public void addReturnTypes(NodeLocation location, SimpleTypeName[] returnTypeNames) {
        for (SimpleTypeName typeName : returnTypeNames) {
            ParameterDef paramDef = new ParameterDef(location, null, null, typeName, null, currentScope);
            currentCUBuilder.addReturnParameter(paramDef);
        }
    }


    // Expressions

    public void startVarRefList() {
        exprListStack.push(new ArrayList<>());
    }

    public void endVarRefList(int exprCount) {
        List<Expression> exprList = exprListStack.peek();
        addExprToList(exprList, exprCount);
    }

    /**
     * <p>Create variable reference expression.</p>
     * There are three types of variables references as per the grammar file.
     * <ol>
     * <li> Simple variable references. a, b, index etc</li>
     * <li> Map or arrays access a[1], m["key"]</li>
     * <li> Struct field access  Person.name</li>
     * </ol>
     *
     * @param location Location of the variable reference expression in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param nameReference  nameReference of the variable
     */
    public void createVarRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                 NameReference nameReference) {
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, whiteSpaceDescriptor, nameReference.name,
                nameReference.pkgName, nameReference.pkgPath);
        variableRefExpr.setWhiteSpaceDescriptor(nameReference.getWhiteSpaceDescriptor());
        exprStack.push(variableRefExpr);
    }

    /**
     * <p>Create map array variable reference expression.</p>
     *
     * @param location location of the variable reference expression in the source file.
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param nameReference nameReference of the variable.
     * @param dimensions dimensions of map array.
     */
    public void createMapArrayVarRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                         NameReference nameReference, int dimensions) {
        FieldAccessExpr fieldExpr = null;
        for (int i = 0; i <= dimensions; i++) {
            Expression parent;
            if (i == dimensions) {
                parent = new VariableRefExpr(location, whiteSpaceDescriptor, nameReference.name, nameReference.pkgName,
                        nameReference.pkgPath);
            } else {
                parent = exprStack.pop();
            }
            FieldAccessExpr parentExpr = new FieldAccessExpr(location, whiteSpaceDescriptor, parent, fieldExpr);
            parentExpr.setIsArrayIndexExpr(true);
            fieldExpr = parentExpr;
        }
        exprStack.push(fieldExpr);
    }

    public void createBinaryExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String opStr) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        Expression lExpr = exprStack.pop();
        checkArgExprValidity(location, lExpr);

        BinaryExpression expr;
        switch (opStr) {
            case "+":
                expr = new AddExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "-":
                expr = new SubtractExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "*":
                expr = new MultExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "/":
                expr = new DivideExpr(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "%":
                expr = new ModExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "&&":
                expr = new AndExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "||":
                expr = new OrExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "==":
                expr = new EqualExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "!=":
                expr = new NotEqualExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case ">=":
                expr = new GreaterEqualExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case ">":
                expr = new GreaterThanExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "<":
                expr = new LessThanExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            case "<=":
                expr = new LessEqualExpression(location, whiteSpaceDescriptor, lExpr, rExpr);
                break;

            // TODO Add support for bracedExpression, binaryPowExpression, binaryModExpression

            default:
                String errMsg = BLangExceptionHelper.constructSemanticError(location,
                        SemanticErrors.UNSUPPORTED_OPERATOR, opStr);
                errorMsgs.add(errMsg);
                // Creating a dummy expression
                expr = new BinaryExpression(location, whiteSpaceDescriptor, lExpr, null, rExpr);
        }

        exprStack.push(expr);
    }

    public void createUnaryExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String op) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        UnaryExpression expr;
        switch (op) {
            case "+":
                expr = new UnaryExpression(location, whiteSpaceDescriptor, Operator.ADD, rExpr);
                break;

            case "-":
                expr = new UnaryExpression(location, whiteSpaceDescriptor, Operator.SUB, rExpr);
                break;

            case "!":
                expr = new UnaryExpression(location, whiteSpaceDescriptor, Operator.NOT, rExpr);
                break;

            default:
                String errMsg = BLangExceptionHelper
                        .constructSemanticError(location, SemanticErrors.UNSUPPORTED_OPERATOR, op);
                errorMsgs.add(errMsg);

                // Creating a dummy expression
                expr = new UnaryExpression(location, whiteSpaceDescriptor, null, rExpr);
        }

        exprStack.push(expr);
    }

    public void startExprList() {
        exprListStack.push(new ArrayList<>());
    }

    public void endExprList(int exprCount) {
        List<Expression> exprList = exprListStack.peek();
        addExprToList(exprList, exprCount);
    }

    public void addFunctionInvocationExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                          NameReference nameReference, boolean argsAvailable) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);

        if (argsAvailable) {
            List<Expression> argExprList = exprListStack.pop();
            checkArgExprValidity(location, argExprList);
            cIExprBuilder.setExpressionList(argExprList);
        }

        cIExprBuilder.setName(nameReference.name);
        cIExprBuilder.setPkgName(nameReference.pkgName);
        cIExprBuilder.setPkgPath(nameReference.pkgPath);
        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        invocationExpr.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        exprStack.push(invocationExpr);
    }

    public void addActionInvocationExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                        NameReference nameReference, String actionName, boolean argsAvailable) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);
        cIExprBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);

        if (argsAvailable) {
            List<Expression> argExprList = exprListStack.pop();
            checkArgExprValidity(location, argExprList);
            cIExprBuilder.setExpressionList(argExprList);
        }

        cIExprBuilder.setName((new Identifier(actionName)).getName());
        cIExprBuilder.setPkgName(nameReference.pkgName);
        cIExprBuilder.setPkgPath(nameReference.pkgPath);
        cIExprBuilder.setConnectorName(nameReference.name);

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        exprStack.push(invocationExpr);
    }

    public void createTypeCastExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                   SimpleTypeName typeName) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        TypeCastExpression typeCastExpression = new TypeCastExpression(location, whiteSpaceDescriptor, typeName, rExpr);
        exprStack.push(typeCastExpression);
    }

    public void createTypeConversionExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
        SimpleTypeName typeName) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        TypeConversionExpr typeConversionExpression = new TypeConversionExpr(location, 
                whiteSpaceDescriptor, typeName, rExpr);
        exprStack.push(typeConversionExpression);
    }
    
    public void createArrayInitExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                    boolean argsAvailable) {
        List<Expression> argExprList;
        if (argsAvailable) {
            argExprList = exprListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }

        ArrayInitExpr arrayInitExpr = new ArrayInitExpr(location, whiteSpaceDescriptor,
                argExprList.toArray(new Expression[argExprList.size()]));
        exprStack.push(arrayInitExpr);
    }

    public void addMapStructKeyValue(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression valueExpr = exprStack.pop();
        Expression keyExpr = exprStack.pop();

        List<KeyValueExpr> keyValueList = mapStructKVListStack.peek();
        keyValueList.add(new KeyValueExpr(location, whiteSpaceDescriptor, keyExpr, valueExpr));
    }

    public void startMapStructLiteral() {
        mapStructKVListStack.push(new ArrayList<>());
    }

    public void createMapStructLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        List<KeyValueExpr> keyValueExprList = mapStructKVListStack.pop();

        Expression[] argExprs;
        if (keyValueExprList.size() == 0) {
            argExprs = new Expression[0];
        } else {
            argExprs = keyValueExprList.toArray(new Expression[keyValueExprList.size()]);
        }

        RefTypeInitExpr refTypeInitExpr = new RefTypeInitExpr(location, whiteSpaceDescriptor, argExprs);
        exprStack.push(refTypeInitExpr);
    }

    public void createConnectorInitExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                        SimpleTypeName typeName, boolean argsAvailable) {
        List<Expression> argExprList;
        if (argsAvailable) {
            argExprList = exprListStack.pop();
            checkArgExprValidity(location, argExprList);
        } else {
            argExprList = new ArrayList<>(0);
        }

        ConnectorInitExpr connectorInitExpr = new ConnectorInitExpr(location, whiteSpaceDescriptor, typeName,
                argExprList.toArray(new Expression[argExprList.size()]));
        exprStack.push(connectorInitExpr);
    }


    // Functions, Actions and Resources

    public void startCallableUnitBody(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void endCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        currentCUBuilder.setBody(blockStmt);
        currentScope = blockStmt.getEnclosingScope();
    }

    public void startFunctionDef(NodeLocation location) {
        currentCUBuilder = new BallerinaFunction.BallerinaFunctionBuilder(currentScope);
        currentCUBuilder.setNodeLocation(location);
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void startWorkerUnit() {
        if (currentCUBuilder != null) {
            parentCUBuilder.push(currentCUBuilder);
        }
        currentCUBuilder = new Worker.WorkerBuilder(currentScope.getEnclosingScope());
        //setting workerOuterBlockScope if it is not a fork join statement
        if (forkJoinScope == null) {
            workerOuterBlockScope.push(currentScope);
        }
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addFunction(WhiteSpaceDescriptor whiteSpaceDescriptor, String name, boolean isNative) {
        currentCUBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentCUBuilder.setIdentifier(new Identifier(name));
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setNative(isNative);

        getAnnotationAttachments().forEach(attachment -> currentCUBuilder.addAnnotation(attachment));

        BallerinaFunction function = currentCUBuilder.buildFunction();
        bFileBuilder.addFunction(function);

        currentScope = function.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startTypeMapperDef(NodeLocation location) {
        currentCUBuilder = new BTypeMapper.BTypeMapperBuilder(currentScope);
        currentCUBuilder.setNodeLocation(location);
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addTypeMapper(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String name,
                              SimpleTypeName returnTypeName, boolean isNative) {
        currentCUBuilder.setIdentifier(new Identifier(name));
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setNative(isNative);
        currentCUBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        addReturnTypes(location, new SimpleTypeName[]{returnTypeName});

        getAnnotationAttachments().forEach(attachment -> currentCUBuilder.addAnnotation(attachment));

        BTypeMapper typeMapper = currentCUBuilder.buildTypeMapper();
        bFileBuilder.addTypeMapper(typeMapper);
        currentScope = typeMapper.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startResourceDef() {
        if (currentScope instanceof BlockStmt) {
            endCallableUnitBody();
        }
//        currentWorker.push("default");
        currentCUBuilder = new Resource.ResourceBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addResource(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                            String name, int annotationCount) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentCUBuilder.setIdentifier(new Identifier(name));
        currentCUBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments(annotationCount).forEach(attachment -> currentCUBuilder.addAnnotation(attachment));

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        currentScope = resource.getEnclosingScope();
        currentCUBuilder = null;
//        workerInteractionDataHolders.clear();
    }


    public void createWorker(NodeLocation sourceLocation, WhiteSpaceDescriptor whiteSpaceDescriptor,
                             String name) {
        validateIdentifier(name, sourceLocation);
        Identifier workerIdentifier = new Identifier(name);
        currentCUBuilder.setIdentifier(workerIdentifier);
        currentCUBuilder.setNodeLocation(sourceLocation);
        currentCUBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);

        Worker worker = currentCUBuilder.buildWorker();
        if (forkJoinStmtBuilderStack.isEmpty() && !parentCUBuilder.isEmpty()) {
            parentCUBuilder.peek().addWorker(worker);
            //setting the current scope to resource block
            currentScope = workerOuterBlockScope.pop();
        } else {
            workerStack.peek().add(worker);
            currentScope = forkJoinScope;
        }

        currentCUBuilder = parentCUBuilder.pop();
        //parentCUBuilder = null;
        //workerOuterBlockScope = null;
    }

    public void createWorkerDefinition(NodeLocation sourceLocation, String name) {
//        currentWorker.push(name);
    }

    public void startActionDef(NodeLocation location) {
        // TODO Check whether the following if block is needed anymore.
        if (currentScope instanceof BlockStmt) {
            endCallableUnitBody();
        }
        currentCUBuilder = new BallerinaAction.BallerinaActionBuilder(currentScope);
        currentCUBuilder.setNodeLocation(location);
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addAction(WhiteSpaceDescriptor whiteSpaceDescriptor, String name, boolean isNative,
                          int annotationCount) {
        currentCUBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentCUBuilder.setIdentifier(new Identifier(name));
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setNative(isNative);

        getAnnotationAttachments(annotationCount).forEach(attachment -> currentCUBuilder.addAnnotation(attachment));

        BallerinaAction action = currentCUBuilder.buildAction();
        currentCUGroupBuilder.addAction(action);

        currentScope = action.getEnclosingScope();
        currentCUBuilder = null;
    }


    // Services and Connectors

    public void startServiceDef(NodeLocation location) {
        currentCUGroupBuilder = new Service.ServiceBuilder(currentScope);
        currentCUGroupBuilder.setNodeLocation(location);
        currentScope = currentCUGroupBuilder.getCurrentScope();
    }

    public void startConnectorDef(NodeLocation location) {
        currentCUGroupBuilder = new BallerinaConnectorDef.BallerinaConnectorDefBuilder(currentScope);
        currentCUGroupBuilder.setNodeLocation(location);
        currentScope = currentCUGroupBuilder.getCurrentScope();
    }

    public void createService(WhiteSpaceDescriptor whiteSpaceDescriptor, String name) {
        currentCUGroupBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentCUGroupBuilder.setIdentifier(new Identifier(name));
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments().forEach(attachment -> currentCUGroupBuilder.addAnnotation(attachment));

        Service service = currentCUGroupBuilder.buildService();
        bFileBuilder.addService(service);

        currentScope = service.getEnclosingScope();
        currentCUGroupBuilder = null;
    }

    public void createConnector(WhiteSpaceDescriptor whiteSpaceDescriptor, String name) {
        currentCUGroupBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        currentCUGroupBuilder.setIdentifier(new Identifier(name));
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments().forEach(attachment -> currentCUGroupBuilder.addAnnotation(attachment));

        BallerinaConnectorDef connector = currentCUGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

        currentScope = connector.getEnclosingScope();
        currentCUGroupBuilder = null;
    }


    // Statements
    public void addVariableDefinitionStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                            SimpleTypeName typeName, String varName, boolean exprAvailable) {
        validateIdentifier(varName, location);
        Identifier identifier = new Identifier(varName);
        VariableRefExpr variableRefExpr = new VariableRefExpr(location,  whiteSpaceDescriptor, identifier.getName());
        SymbolName symbolName = new SymbolName(identifier.getName());

        VariableDef variableDef = new VariableDef(location, whiteSpaceDescriptor, identifier, typeName, symbolName,
                currentScope);
        variableRefExpr.setVariableDef(variableDef);

        Expression rhsExpr = exprAvailable ? exprStack.pop() : null;
        VariableDefStmt variableDefStmt = new VariableDefStmt(location, variableDef, variableRefExpr, rhsExpr);
        variableDefStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        if (blockStmtBuilderStack.size() == 0 && currentCUGroupBuilder != null) {
            if (rhsExpr != null) {
                if (rhsExpr instanceof ActionInvocationExpr) {
                    String errMsg = BLangExceptionHelper.constructSemanticError(location,
                            SemanticErrors.ACTION_INVOCATION_NOT_ALLOWED_HERE);
                    errorMsgs.add(errMsg);
                }
            }
            currentCUGroupBuilder.addVariableDef(variableDefStmt);
        } else {
            addToBlockStmt(variableDefStmt);
        }
    }

    public void addCommentStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String comment) {
        CommentStmt commentStmt = new CommentStmt(location, whiteSpaceDescriptor, comment);
        addToBlockStmt(commentStmt);
    }

    public void createAssignmentStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression rExpr = exprStack.pop();
        List<Expression> lExprList = exprListStack.pop();

        AssignStmt assignStmt = new AssignStmt(location, lExprList.toArray(new Expression[lExprList.size()]), rExpr);
        assignStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression[] exprs;
        // Get the expression list from the expression list stack
        if (!exprListStack.isEmpty()) {
            // Return statement with empty expression list.
            // Just a return statement
            List<Expression> exprList = exprListStack.pop();
            checkArgExprValidity(location, exprList);
            exprs = exprList.toArray(new Expression[exprList.size()]);
        } else {
            exprs = new Expression[0];
        }

        ReturnStmt returnStmt = new ReturnStmt(location, whiteSpaceDescriptor, exprs);
        addToBlockStmt(returnStmt);
    }

    public void createReplyStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression argExpr = exprStack.pop();
        if (!(argExpr instanceof VariableRefExpr)) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REF_TYPE_MESSAGE_ALLOWED);
            errorMsgs.add(errMsg);
        }
        ReplyStmt replyStmt = new ReplyStmt(location, whiteSpaceDescriptor, argExpr);
        addToBlockStmt(replyStmt);
    }

    public void startWhileStmt(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void createWhileStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        // Create a while statement builder
        WhileStmt.WhileStmtBuilder whileStmtBuilder = new WhileStmt.WhileStmtBuilder();
        whileStmtBuilder.setNodeLocation(location);
        whileStmtBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);

        // Get the expression at the top of the expression stack and set it as the while condition
        Expression condition = exprStack.pop();
        checkArgExprValidity(location, condition);
        whileStmtBuilder.setCondition(condition);

        // Get the statement block at the top of the block statement stack and set as the while body.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        whileStmtBuilder.setWhileBody(blockStmt);

        // Close the current scope and open the enclosing scope
        currentScope = blockStmt.getEnclosingScope();

        // Add the while statement to the statement block which is at the top of the stack.
        WhileStmt whileStmt = whileStmtBuilder.build();
        blockStmtBuilderStack.peek().addStmt(whileStmt);
    }

    public void createBreakStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        BreakStmt.BreakStmtBuilder breakStmtBuilder = new BreakStmt.BreakStmtBuilder();
        breakStmtBuilder.setNodeLocation(location);
        breakStmtBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        BreakStmt breakStmt = breakStmtBuilder.build();
        addToBlockStmt(breakStmt);
    }

    public void startTransformStmt(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void createTransformStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        // Create a transform statement builder
        TransformStmt.TransformStmtBuilder transformStmtBuilder = new TransformStmt.TransformStmtBuilder();
        transformStmtBuilder.setNodeLocation(location);
        transformStmtBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);

        // Get the statement block at the top of the block statement stack and set as the transform body.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        transformStmtBuilder.setTransformBody(blockStmt);

        Map<String, Expression> inputs = new HashMap<>(); // right hand expressions by variable
        Map<String, Expression> outputs = new HashMap<>(); //left hand expressions by variable

        validateTransformStatementBody(blockStmt, inputs, outputs);

        transformStmtBuilder.setInputExprs((inputs.values()).toArray(new Expression[inputs.values().size()]));
        transformStmtBuilder.setOutputExprs((outputs.values()).toArray(new Expression[outputs.values().size()]));

        // Close the current scope and open the enclosing scope
        currentScope = blockStmt.getEnclosingScope();

        // Add the transform statement to the statement block which is at the top of the stack.
        TransformStmt transformStmt = transformStmtBuilder.build();
        blockStmtBuilderStack.peek().addStmt(transformStmt);
    }

    public void startIfElseStmt(NodeLocation location) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = new IfElseStmt.IfElseStmtBuilder();
        ifElseStmtBuilder.setNodeLocation(location);
        ifElseStmtBuilderStack.push(ifElseStmtBuilder);
    }

    public void startIfClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void startElseIfClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void addIfClause(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
        if (whiteSpaceDescriptor != null) {
            WhiteSpaceDescriptor ws = ifElseStmtBuilder.getWhiteSpaceDescriptor();
            if (ws == null) {
                ws = new WhiteSpaceDescriptor();
                ifElseStmtBuilder.setWhiteSpaceDescriptor(ws);
            }
            ws.addChildDescriptor(IF_CLAUSE, whiteSpaceDescriptor);
        }
        Expression condition = exprStack.pop();
        checkArgExprValidity(ifElseStmtBuilder.getLocation(), condition);
        ifElseStmtBuilder.setIfCondition(condition);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        ifElseStmtBuilder.setThenBody(blockStmt);

        currentScope = blockStmt.getEnclosingScope();
    }

    public void addElseIfClause(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseIfStmtBlock = blockStmtBuilder.build();

        Expression condition = exprStack.pop();
        checkArgExprValidity(ifElseStmtBuilder.getLocation(), condition);
        ifElseStmtBuilder.addElseIfBlock(elseIfStmtBlock.getNodeLocation(), whiteSpaceDescriptor,
                                condition, elseIfStmtBlock);

        currentScope = elseIfStmtBlock.getEnclosingScope();
    }

    public void startElseClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void addElseClause(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
        if (whiteSpaceDescriptor != null) {
            WhiteSpaceDescriptor ws = ifElseStmtBuilder.getWhiteSpaceDescriptor();
            if (ws == null) {
                ws = new WhiteSpaceDescriptor();
                ifElseStmtBuilder.setWhiteSpaceDescriptor(ws);
            }
            ws.addChildDescriptor(ELSE_CLAUSE, whiteSpaceDescriptor);
        }
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseStmt = blockStmtBuilder.build();
        ifElseStmtBuilder.setElseBody(elseStmt);

        currentScope = elseStmt.getEnclosingScope();
    }

    public void addIfElseStmt() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.pop();
        IfElseStmt ifElseStmt = ifElseStmtBuilder.build();
        addToBlockStmt(ifElseStmt);
    }


    public void startTryCatchStmt(NodeLocation location) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = new TryCatchStmt.TryCatchStmtBuilder();
        tryCatchStmtBuilder.setLocation(location);
        tryCatchStmtBuilderStack.push(tryCatchStmtBuilder);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void addTryCatchBlockStmt() {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        // Creating Try clause.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt tryBlock = blockStmtBuilder.build();
        tryCatchStmtBuilder.setTryBlock(tryBlock);
        currentScope = tryBlock.getEnclosingScope();
    }

    public void startCatchClause(NodeLocation location) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        // Staring parsing catch clause.
        TryCatchStmt.CatchBlock catchBlock = new TryCatchStmt.CatchBlock(currentScope);
        tryCatchStmtBuilder.addCatchBlock(catchBlock);
        currentScope = catchBlock;

        BlockStmt.BlockStmtBuilder catchBlockBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(catchBlockBuilder);

        currentScope = catchBlockBuilder.getCurrentScope();
    }

    public void addCatchClause(NodeLocation nodeLocation, WhiteSpaceDescriptor whiteSpaceDescriptor,
                               SimpleTypeName errorType, String argName) {
        validateIdentifier(argName, nodeLocation);
        Identifier identifier = new Identifier(argName);
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        if (whiteSpaceDescriptor != null) {
            tryCatchStmtBuilder.getLastCatchBlock().setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        }

        BlockStmt.BlockStmtBuilder catchBlockBuilder = blockStmtBuilderStack.pop();
        BlockStmt catchBlock = catchBlockBuilder.build();
        currentScope = catchBlock.getEnclosingScope();

        SymbolName symbolName = new SymbolName(identifier.getName(), currentPackagePath);
        ParameterDef paramDef = new ParameterDef(catchBlock.getNodeLocation(), null, identifier, errorType, symbolName,
                currentScope);
        currentScope.resolve(symbolName);
        currentScope.define(symbolName, paramDef);
        tryCatchStmtBuilder.getLastCatchBlock().setParameterDef(paramDef);
        tryCatchStmtBuilder.setLastCatchBlockStmt(catchBlock);
    }

    public void startFinallyBlock(NodeLocation location) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        // Start Parsing finally clause.
        TryCatchStmt.FinallyBlock finallyBlock = new TryCatchStmt.FinallyBlock(currentScope);
        tryCatchStmtBuilder.setFinallyBlock(finallyBlock);
        currentScope = finallyBlock;

        BlockStmt.BlockStmtBuilder finallyBlockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(finallyBlockStmtBuilder);

        currentScope = finallyBlockStmtBuilder.getCurrentScope();
    }

    public void addFinallyBlock(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();
        if (whiteSpaceDescriptor != null) {
            WhiteSpaceDescriptor ws = tryCatchStmtBuilder.getWhiteSpaceDescriptor();
            if (ws == null) {
                ws = new WhiteSpaceDescriptor();
                tryCatchStmtBuilder.setWhiteSpaceDescriptor(ws);
            }
            ws.addChildDescriptor(FINALLY_CLAUSE, whiteSpaceDescriptor);
        }
        BlockStmt.BlockStmtBuilder catchBlockBuilder = blockStmtBuilderStack.pop();
        BlockStmt finallyBlock = catchBlockBuilder.build();
        currentScope = finallyBlock.getEnclosingScope();
        tryCatchStmtBuilder.setFinallyBlockStmt(finallyBlock);
    }

    public void addTryCatchStmt(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.pop();
        if (whiteSpaceDescriptor != null) {
            WhiteSpaceDescriptor ws = tryCatchStmtBuilder.getWhiteSpaceDescriptor();
            if (ws == null) {
                ws = new WhiteSpaceDescriptor();
                tryCatchStmtBuilder.setWhiteSpaceDescriptor(ws);
            }
            ws.addChildDescriptor(TRY_CLAUSE, whiteSpaceDescriptor);
        }
        TryCatchStmt tryCatchStmt = tryCatchStmtBuilder.build();
        addToBlockStmt(tryCatchStmt);
    }

    public void createThrowStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        Expression expression = exprStack.pop();
        if (expression instanceof VariableRefExpr || expression instanceof FunctionInvocationExpr) {
            ThrowStmt throwStmt = new ThrowStmt(location, whiteSpaceDescriptor, expression);
            addToBlockStmt(throwStmt);
            return;
        }
        String errMsg = BLangExceptionHelper.constructSemanticError(location, SemanticErrors
                .ONLY_ERROR_TYPE_ALLOWED_HERE);
        errorMsgs.add(errMsg);
    }

    public void startForkJoinStmt(NodeLocation nodeLocation) {
        //blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder(nodeLocation, currentScope));
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = new ForkJoinStmt.ForkJoinStmtBuilder(currentScope);
        forkJoinStmtBuilder.setNodeLocation(nodeLocation);
        forkJoinStmtBuilderStack.push(forkJoinStmtBuilder);
        currentScope = forkJoinStmtBuilder.currentScope;
        forkJoinScope = currentScope;
        workerStack.push(new ArrayList<>());
    }

    public void startJoinClause(NodeLocation nodeLocation) {
        currentScope = forkJoinStmtBuilderStack.peek().getJoin();
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder(nodeLocation, currentScope));
    }

    public void endJoinClause(NodeLocation location, SimpleTypeName typeName, String paramName) {
        validateIdentifier(paramName, location);
        Identifier identifier = new Identifier(paramName);
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt forkJoinStmt = blockStmtBuilder.build();
        SymbolName symbolName = new SymbolName(identifier.getName(), currentPackagePath);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, identifier.getName());
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, null, identifier, typeName, symbolName, currentScope);
        forkJoinStmtBuilder.setJoinBlock(forkJoinStmt);
        forkJoinStmtBuilder.setJoinResult(paramDef);
        currentScope = forkJoinStmtBuilder.getJoin().getEnclosingScope();
    }

    public void createAnyJoinCondition(String joinType, String joinCount, NodeLocation location) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();

        forkJoinStmtBuilder.setJoinType(joinType);
        if (Integer.parseInt(joinCount) != 1) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.ONLY_COUNT_1_ALLOWED_THIS_VERSION);
            errorMsgs.add(errMsg);
        }
        forkJoinStmtBuilder.setJoinCount(Integer.parseInt(joinCount));
    }

    public void createAllJoinCondition(String joinType) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        forkJoinStmtBuilder.setJoinType(joinType);
    }

    public void createJoinWorkers(String workerName) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        forkJoinStmtBuilder.addJoinWorker(workerName);
    }

    public void startTimeoutClause(NodeLocation nodeLocation) {
        currentScope = forkJoinStmtBuilderStack.peek().getTimeout();
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder(nodeLocation, currentScope));
    }

    public void endTimeoutClause(NodeLocation location, SimpleTypeName typeName, String paramName) {
        validateIdentifier(paramName, location);
        Identifier identifier = new Identifier(paramName);
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt timeoutStmt = blockStmtBuilder.build();
        forkJoinStmtBuilder.setTimeoutBlock(timeoutStmt);
        forkJoinStmtBuilder.setTimeoutExpression(exprStack.pop());
        SymbolName symbolName = new SymbolName(identifier.getName());

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, identifier.getName());
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, null, identifier, typeName, symbolName, currentScope);

        forkJoinStmtBuilder.setTimeoutResult(paramDef);
        currentScope = forkJoinStmtBuilder.getTimeout().getEnclosingScope();
    }

    public void endForkJoinStmt() {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.pop();

        List<Worker> workerList = workerStack.pop();
        if (workerList != null) {
            forkJoinStmtBuilder.setWorkers(workerList.toArray(new Worker[workerList.size()]));
        }
        //forkJoinStmtBuilder.setMessageReference((VariableRefExpr) exprStack.pop());
        ForkJoinStmt forkJoinStmt = forkJoinStmtBuilder.build();
        addToBlockStmt(forkJoinStmt);
        currentScope = forkJoinStmt.getEnclosingScope();

    }

    public void createFunctionInvocationStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                                             NameReference nameReference, boolean argsAvailable) {

        addFunctionInvocationExpr(location, whiteSpaceDescriptor, nameReference, argsAvailable);
        FunctionInvocationExpr invocationExpr = (FunctionInvocationExpr) exprStack.pop();


        FunctionInvocationStmt functionInvocationStmt = new FunctionInvocationStmt(location, invocationExpr);
        functionInvocationStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        blockStmtBuilderStack.peek().addStmt(functionInvocationStmt);
    }

    public void createWorkerInvocationStmt(String workerName, NodeLocation sourceLocation,
                                           WhiteSpaceDescriptor whiteSpaceDescriptor) {
        //List<Expression> exprList = exprListStack.peek();
        List<Expression> exprList = exprListStack.pop();
        WorkerInvocationStmt workerInvocationStmt = new WorkerInvocationStmt(workerName, exprList, sourceLocation,
                whiteSpaceDescriptor);
        currentCUBuilder.addWorkerInteractionStatement(workerInvocationStmt);
        //workerInvocationStmt.setLocation(sourceLocation);
        blockStmtBuilderStack.peek().addStmt(workerInvocationStmt);
    }

    public void createWorkerReplyStmt(String workerName, NodeLocation sourceLocation,
                                      WhiteSpaceDescriptor whiteSpaceDescriptor) {
        //List<Expression> exprList = exprListStack.peek();
        List<Expression> exprList = exprListStack.pop();
        WorkerReplyStmt workerReplyStmt = new WorkerReplyStmt(workerName, exprList, sourceLocation,
                whiteSpaceDescriptor);
        currentCUBuilder.addWorkerInteractionStatement(workerReplyStmt);
        //workerReplyStmt.setLocation(sourceLocation);
        blockStmtBuilderStack.peek().addStmt(workerReplyStmt);
    }

    public void createActionInvocationStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        ActionInvocationExpr invocationExpr = (ActionInvocationExpr) exprStack.pop();

        ActionInvocationStmt actionInvocationStmt = new ActionInvocationStmt(location, invocationExpr);
        actionInvocationStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        blockStmtBuilderStack.peek().addStmt(actionInvocationStmt);
    }

    public void startTransactionStmt(NodeLocation location) {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = new TransactionStmt.TransactionStmtBuilder();
        transactionStmtBuilder.setLocation(location);
        transactionStmtBuilderStack.push(transactionStmtBuilder);
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void addTransactionBlockStmt() {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.peek();
        // Creating Try clause.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt transactionBlock = blockStmtBuilder.build();
        transactionStmtBuilder.setTransactionBlock(transactionBlock);
        currentScope = transactionBlock.getEnclosingScope();
    }

    public void startAbortedClause(NodeLocation location) {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.peek();
        // Staring parsing aborted clause.
        TransactionStmt.AbortedBlock abortedBlock = new TransactionStmt.AbortedBlock(currentScope);
        transactionStmtBuilder.setAbortedBlock(abortedBlock);
        currentScope = abortedBlock;
        BlockStmt.BlockStmtBuilder abortedBlockBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(abortedBlockBuilder);
        currentScope = abortedBlockBuilder.getCurrentScope();
    }

    public void addAbortedClause() {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder abortedBlockBuilder = blockStmtBuilderStack.pop();
        BlockStmt abortedBlock = abortedBlockBuilder.build();
        currentScope = abortedBlock.getEnclosingScope();
        transactionStmtBuilder.setAbortedBlockStmt(abortedBlock);
    }

    public void startCommittedClause(NodeLocation location) {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.peek();
        // Staring parsing committed clause.
        TransactionStmt.CommittedBlock committedBlock = new TransactionStmt.CommittedBlock(currentScope);
        transactionStmtBuilder.setCommittedBlock(committedBlock);
        currentScope = committedBlock;
        BlockStmt.BlockStmtBuilder committedBlockBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(committedBlockBuilder);
        currentScope = committedBlockBuilder.getCurrentScope();
    }

    public void addCommittedClause() {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder committedBlockBuilder = blockStmtBuilderStack.pop();
        BlockStmt committedBlock = committedBlockBuilder.build();
        currentScope = committedBlock.getEnclosingScope();
        transactionStmtBuilder.setCommittedBlockStmt(committedBlock);
    }

    public void addTransactionStmt(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        TransactionStmt.TransactionStmtBuilder transactionStmtBuilder = transactionStmtBuilderStack.pop();
        transactionStmtBuilder.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
        TransactionStmt transactionStmt = transactionStmtBuilder.build();
        addToBlockStmt(transactionStmt);
    }

    public void createAbortStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        addToBlockStmt(new AbortStmt(location, whiteSpaceDescriptor));
    }

    // Literal Values

    public void createIntegerLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String value) {
        BValueType bValue = new BInteger(Long.parseLong(value));
        createLiteral(location, whiteSpaceDescriptor, new SimpleTypeName(TypeConstants.INT_TNAME), bValue);
    }

    public void createFloatLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String value) {
        BValueType bValue = new BFloat(Double.parseDouble(value));
        createLiteral(location, whiteSpaceDescriptor, new SimpleTypeName(TypeConstants.FLOAT_TNAME), bValue);
    }

    public void createStringLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String value) {
        BValueType bValue = new BString(value);
        createLiteral(location, whiteSpaceDescriptor, new SimpleTypeName(TypeConstants.STRING_TNAME), bValue);
    }

    public void createBooleanLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String value) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(location, whiteSpaceDescriptor, new SimpleTypeName(TypeConstants.BOOLEAN_TNAME), bValue);
    }

    public void createNullLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String value) {
        NullLiteral nullLiteral = new NullLiteral(location, whiteSpaceDescriptor);
        exprStack.push(nullLiteral);
    }

    public void validateAndSetPackagePath(NodeLocation location, NameReference nameReference) {
        String name = nameReference.getName();
        String pkgName = nameReference.getPackageName();
        ImportPackage importPkg = getImportPackage(pkgName);
        checkForUndefinedPackagePath(location, pkgName, importPkg, () -> pkgName + ":" + name);

        if (importPkg == null) {
            nameReference.setPkgPath(currentPackagePath);
            return;
        }

        importPkg.markUsed();
        nameReference.setPkgPath(importPkg.getPath());
    }


    // Private methods

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }

    private void createLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                               SimpleTypeName typeName, BValueType bValueType) {
        BasicLiteral basicLiteral = new BasicLiteral(location, whiteSpaceDescriptor, typeName, bValueType);
        exprStack.push(basicLiteral);
    }

    /**
     * @param exprList List<Expression>
     * @param n        number of expression to be added the given list
     */
    private void addExprToList(List<Expression> exprList, int n) {

        if (exprStack.isEmpty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }

        if (n == 1) {
            Expression expr = exprStack.pop();
            exprList.add(expr);
        } else {
            Expression expr = exprStack.pop();
            addExprToList(exprList, n - 1);
            exprList.add(expr);
        }
    }

    /**
     * return value within double quotes.
     *
     * @param inputString string with double quotes
     * @return value
     */
    private static String getValueWithinBackquote(String inputString) {
        Pattern p = Pattern.compile("`([^`]*)`");
        Matcher m = p.matcher(inputString);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Create an expression for accessing fields, represented in the form of 'identifier.identifier'.
     *
     * @param location Source location of the ballerina file
     * @param whiteSpaceDescriptor Holds whitespace region data
     */
    public void createFieldRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        if (exprStack.size() < 2) {
            return;
        }

        // Accessing a field with syntax x.y.z means y and z are static field names, but x is a variable ref.
        // Hence the varRefs are replaced with a basic literal, upto the very first element in the chain.
        // First element is treated as a variableRef.
        ReferenceExpr childExpr = (ReferenceExpr) exprStack.pop();
        //TODO fix this properly - disabled the test GlobalVarErrorTest
        //TODO Since by default package path is set to Variable Reference we need differentiate the two cases
        //TODO where we put package put package name intentionally against, we get this from user programmed
        //TODO ballerina programme
        //if (childExpr.getPkgPath() != null) {
        //    String errMsg = BLangExceptionHelper.constructSemanticError(location,
        //            SemanticErrors.STRUCT_FIELD_CHILD_HAS_PKG_IDENTIFIER, childExpr.getPkgName() + ":" +
        //            childExpr.getVarName());
        //    errorMsgs.add(errMsg);
        //}

        if (childExpr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldExpr = (FieldAccessExpr) childExpr;
            Expression varRefExpr = fieldExpr.getVarRef();
            if (varRefExpr instanceof VariableRefExpr) {
                varRefExpr = new BasicLiteral(varRefExpr.getNodeLocation(), varRefExpr.getWhiteSpaceDescriptor(),
                        new SimpleTypeName(TypeConstants.STRING_TNAME),
                        new BString(((VariableRefExpr) varRefExpr).getVarName()));
                fieldExpr.setVarRef(varRefExpr);
            }
        } else if (childExpr instanceof VariableRefExpr) {
            BasicLiteral fieldNameLietral = new BasicLiteral(childExpr.getNodeLocation(),
                    childExpr.getWhiteSpaceDescriptor(), new SimpleTypeName(TypeConstants.STRING_TNAME),
                    new BString(((VariableRefExpr) childExpr).getVarName()));
            childExpr = new FieldAccessExpr(location, whiteSpaceDescriptor, fieldNameLietral);
        } else {
            childExpr = new FieldAccessExpr(location, whiteSpaceDescriptor, childExpr);
        }

        ReferenceExpr parentExpr = (ReferenceExpr) exprStack.pop();
        Expression newParentExpr;
        if (parentExpr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldOfParent = ((FieldAccessExpr) parentExpr).getFieldExpr();
            fieldOfParent.setFieldExpr((FieldAccessExpr) childExpr);
            newParentExpr = parentExpr;
        } else {
            newParentExpr = new FieldAccessExpr(location, whiteSpaceDescriptor, parentExpr.getPkgName(),
                    parentExpr.getPkgPath(), parentExpr, (FieldAccessExpr) childExpr);
        }
        exprStack.push(newParentExpr);
    }

    protected ImportPackage getImportPackage(String pkgName) {
        return (pkgName != null) ? importPkgMap.get(pkgName) : null;
    }

    protected void checkForUndefinedPackagePath(NodeLocation location,
                                                String pkgName,
                                                ImportPackage importPackage,
                                                Supplier<String> symbolNameSupplier) {
        if (pkgName != null && importPackage == null) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.UNDEFINED_PACKAGE_NAME, pkgName, symbolNameSupplier.get());
            errorMsgs.add(errMsg);
        }
    }

    protected void checkArgExprValidity(NodeLocation location, List<Expression> argExprList) {
        for (Expression argExpr : argExprList) {
            checkArgExprValidity(location, argExpr);
        }
    }

    protected void checkArgExprValidity(NodeLocation location, Expression argExpr) {
        String errMsg = null;
        if (argExpr instanceof ArrayInitExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.ARRAY_INIT_NOT_ALLOWED_HERE);

        } else if (argExpr instanceof RefTypeInitExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REF_TYPE_INTI_NOT_ALLOWED_HERE);
        }

        if (errMsg != null) {
            errorMsgs.add(errMsg);
        }
    }

    protected List<AnnotationAttachment> getAnnotationAttachments() {
        return getAnnotationAttachments(annonAttachmentStack.size());
    }

    protected List<AnnotationAttachment> getAnnotationAttachments(int count) {
        if (count == 0) {
            return new ArrayList<>(0);
        }

        int depth = annonAttachmentStack.size() - (count - 1);
        List<AnnotationAttachment> annotationAttachmentList = new ArrayList<>();
        collectAnnotationAttachments(annotationAttachmentList, depth, annonAttachmentStack.size());
        return annotationAttachmentList;
    }

    private void collectAnnotationAttachments(List<AnnotationAttachment> annonAttachmentList, int depth, int index) {
        if (index == depth) {
            annonAttachmentList.add(annonAttachmentStack.pop());
        } else {
            AnnotationAttachment attachment = annonAttachmentStack.pop();
            collectAnnotationAttachments(annonAttachmentList, depth, index - 1);
            annonAttachmentList.add(attachment);
        }
    }

    private void validateIdentifier(String identifier, NodeLocation location) {
        if (identifier.equals("_")) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location, SemanticErrors
                    .RESERVED_IDENTIFIER, identifier);
            errorMsgs.add(errMsg);
        }
    }

    /**
     * Validates the statements in the transform statement body as explained below :
     * - Left expression of Assignment Statement becomes output of transform statement
     * - Right expressions of Assignment Statement becomes input of transform statement
     * - Variables in each of left and right expressions of all statements are extracted as input and output
     * - A variable that is used as an input cannot be used as an output in another statement
     * - If inputs and outputs are used interchangeably, a semantic error is thrown.
     *
     * @param blockStmt transform statement block statement
     * @param inputs    input variable reference expressions map
     * @param outputs   output variable reference expressions map
     */
    private void validateTransformStatementBody(BlockStmt blockStmt, Map<String, Expression> inputs,
                                                Map<String, Expression> outputs) {
        for (Statement statement : blockStmt.getStatements()) {
            if (statement instanceof AssignStmt) {
                for (Expression lExpr : ((AssignStmt) statement).getLExprs()) {
                    Expression[] varRefExpressions = getVariableReferencesFromExpression(lExpr);
                    for (Expression exp : varRefExpressions) {
                        String varName = ((VariableRefExpr) exp).getVarName();
                        if (inputs.get(varName) == null) {
                            //if variable has not been used as an input before
                            if (outputs.get(varName) == null) {
                                List<Statement> stmtList = new ArrayList<>();
                                stmtList.add(statement);
                                outputs.put(varName, exp);
                            }
                        } else {
                            String errMsg = BLangExceptionHelper.constructSemanticError(statement.getNodeLocation(),
                                                  SemanticErrors.TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT, statement);
                            errorMsgs.add(errMsg);
                        }
                    }
                }
                Expression rExpr = ((AssignStmt) statement).getRExpr();
                Expression[] varRefExpressions = getVariableReferencesFromExpression(rExpr);
                for (Expression exp : varRefExpressions) {
                    String varName = ((VariableRefExpr) exp).getVarName();
                    if (outputs.get(varName) == null) {
                        //if variable has not been used as an output before
                        if (inputs.get(varName) == null) {
                            List<Statement> stmtList = new ArrayList<>();
                            stmtList.add(statement);
                            inputs.put(varName, exp);
                        }
                    } else {
                        String errMsg = BLangExceptionHelper.constructSemanticError(statement.getNodeLocation(),
                                                SemanticErrors.TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT, statement);
                        errorMsgs.add(errMsg);
                    }
                }
            }
        }
    }

    private Expression[] getVariableReferencesFromExpression(Expression expression) {
        if (expression instanceof FieldAccessExpr) {
            return new Expression[] { ((FieldAccessExpr) expression).getVarRef() };
        } else if (expression instanceof FunctionInvocationExpr) {
            Expression[] argExprs = ((FunctionInvocationExpr) expression).getArgExprs();
            List<Expression> expList = new ArrayList<>();
            for (Expression arg : argExprs) {
                Expression[] varRefExps = getVariableReferencesFromExpression(arg);
                expList.addAll(Arrays.asList(varRefExps));
            }
            return expList.toArray(new Expression[expList.size()]);
        } else if (expression instanceof VariableRefExpr) {
            return new Expression[] { expression };
        }
        return new Expression[] {};
    }

    /**
     * This class represents CallableUnitName used in function and action invocation expressions.
     */
    public static class NameReference {
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private String pkgName;
        private String name;
        private String pkgPath;

        public NameReference(String pkgName, String name) {
            Identifier identifier = new Identifier(name);
            this.name = identifier.getName();
            this.pkgName = pkgName;
        }

        public String getPackageName() {
            return pkgName;
        }

        public String getName() {
            return name;
        }

        public String getPackagePath() {
            return pkgPath;
        }

        public void setPkgPath(String pkgPath) {
            this.pkgPath = pkgPath;
        }

        public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
            return whiteSpaceDescriptor;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }
    }
}
