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
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
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
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BTypes;
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
    protected String currentPackagePath;
    protected BallerinaFile.BFileBuilder bFileBuilder;

    protected SymbolScope currentScope;

    // Builds connectors and services.
    protected CallableUnitGroupBuilder currentCUGroupBuilder;

    // Builds functions, actions and resources.
    protected CallableUnitBuilder currentCUBuilder;

    // Keep the parent CUBuilder for worker
    protected CallableUnitBuilder parentCUBuilder;

    // Builds user defined structs.
    protected StructDef.StructBuilder currentStructBuilder;

    // Builds user defined annotations.
    protected AnnotationDef.AnnotationDefBuilder annotationDefBuilder;
    
    protected Stack<AnnotationAttachment.AnnotationBuilder> annonAttachmentBuilderStack = new Stack<>();
    protected Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    protected Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    protected Stack<TryCatchStmt.TryCatchStmtBuilder> tryCatchStmtBuilderStack = new Stack<>();

    protected Stack<ForkJoinStmt.ForkJoinStmtBuilder> forkJoinStmtBuilderStack = new Stack<>();
    protected Stack<List<Worker>> workerStack = new Stack<>();

    protected Stack<Expression> exprStack = new Stack<>();

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    protected Stack<List<Expression>> exprListStack = new Stack<>();
    
    protected Stack<List<MapStructInitKeyValueExpr>> mapStructKVListStack = new Stack<>();
    protected Stack<AnnotationAttachment> annonAttachmentStack = new Stack<>();

    // This variable keeps the package scope so that workers (and any global things) can be added to package scope
    protected SymbolScope packageScope = null;

    // This variable keeps the fork-join scope when adding workers and resolve back to current scope once done
    protected SymbolScope forkJoinScope = null;

    // This variable keeps the current scope when adding workers and resolve back to current scope once done
    protected SymbolScope workerOuterBlockScope = null;

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    protected Map<String, ImportPackage> importPkgMap = new HashMap<>();

    protected Stack<AnnotationAttributeValue> annotationAttributeValues = new Stack<AnnotationAttributeValue>();
    
    protected List<String> errorMsgs = new ArrayList<>();

    public BLangModelBuilder(BLangPackage.PackageBuilder packageBuilder, String bFileName) {
        this.currentScope = packageBuilder.getCurrentScope();
        this.packageScope = currentScope;
        bFileBuilder = new BallerinaFile.BFileBuilder(bFileName, packageBuilder);

    }

    public BallerinaFile build() {
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


    // Packages and import packages

    public void addPackageDcl(String pkgPath) {
        // TODO Validate whether this file is in the correct package
        // TODO example this is in com/greet/hello directory, but package Path is come.greet.bye. This is wrong
        currentPackagePath = pkgPath;
        bFileBuilder.setPackagePath(currentPackagePath);
    }

    public void addImportPackage(NodeLocation location, String pkgPath, String asPkgName) {
        ImportPackage importPkg;
        if (asPkgName != null) {
            importPkg = new ImportPackage(location, pkgPath, asPkgName);
        } else {
            importPkg = new ImportPackage(location, pkgPath);
        }

        if (importPkgMap.get(importPkg.getName()) != null) {
            String errMsg = BLangExceptionHelper
                    .constructSemanticError(location, SemanticErrors.REDECLARED_IMPORT_PACKAGE, importPkg.getName());
            errorMsgs.add(errMsg);
        }

        bFileBuilder.addImportPackage(importPkg);
        importPkgMap.put(importPkg.getName(), importPkg);
    }


    // Add constant definitions;

    public void addConstantDef(NodeLocation location, SimpleTypeName typeName, String constName) {
        SymbolName symbolName = new SymbolName(constName);
        ConstDef constantDef = new ConstDef(location, constName, typeName, currentPackagePath,
                symbolName, currentScope, exprStack.pop());

        getAnnotationAttachments().forEach(attachment -> constantDef.addAnnotation(attachment));
        
        // Add constant definition to current file;
        bFileBuilder.addConst(constantDef);

        // TODO Support annotations for Constants
        getAnnotationAttachments();
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
     * @param location  Location of the field in the source file
     * @param fieldName Name of the field in the {@link StructDef}
     */
    public void addFieldDefinition(NodeLocation location, SimpleTypeName typeName, String fieldName, 
            boolean defaultValueAvailable) {
        SymbolName symbolName = new SymbolName(fieldName);

        // Check whether this constant is already defined.
        StructuredUnit structScope = (StructuredUnit) currentScope;
        BLangSymbol fieldSymbol = structScope.resolveMembers(symbolName);
        if (fieldSymbol != null) {
            String errMsg = BLangExceptionHelper
                    .constructSemanticError(location, SemanticErrors.REDECLARED_SYMBOL, fieldName);
            errorMsgs.add(errMsg);
        }

        Expression defaultValExpr = null;
        if (defaultValueAvailable) {
            defaultValExpr = exprStack.pop();
        }
        
        if (currentScope instanceof StructDef) {
            VariableDef fieldDef = new VariableDef(location, fieldName, typeName, symbolName, currentScope);
            VariableRefExpr fieldRefExpr = new VariableRefExpr(location, fieldName);
            fieldRefExpr.setVariableDef(fieldDef);
            VariableDefStmt fieldDefStmt = new VariableDefStmt(location, fieldDef, fieldRefExpr, defaultValExpr);
            currentStructBuilder.addField(fieldDefStmt);
        } else if (currentScope instanceof AnnotationDef) {
            AnnotationAttributeDef annotationField = new AnnotationAttributeDef(location, fieldName, typeName, 
                (BasicLiteral) defaultValExpr, symbolName, currentScope, currentPackagePath);
            currentScope.define(symbolName, annotationField);
            annotationDefBuilder.addAttributeDef(annotationField);
        }
    }

    /**
     * Creates a {@link StructDef}.
     *
     * @param name Name of the {@link StructDef}
     */
    public void addStructDef(String name) {
        currentStructBuilder.setName(name);
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

    public void createAnnotationKeyValue(String key) {
        AnnotationAttachment.AnnotationBuilder annotationBuilder = annonAttachmentBuilderStack.peek();
        annotationBuilder.addAttributeNameValuePair(key, annotationAttributeValues.pop());
    }

    public void addAnnotationAttachment(NodeLocation location, NameReference nameReference, int attributesCount) {
        AnnotationAttachment.AnnotationBuilder annonAttachmentBuilder = annonAttachmentBuilderStack.pop();
        annonAttachmentBuilder.setName(nameReference.getName());
        annonAttachmentBuilder.setPkgName(nameReference.getPackageName());
        annonAttachmentBuilder.setPkgPath(nameReference.getPackagePath());
        annonAttachmentBuilder.setNodeLocation(location);
        annonAttachmentStack.add(annonAttachmentBuilder.build());
    }

    /**
     * Start an annotation definition.
     * 
     * @param location Location of the annotation definition in the source file
     */
    public void startAnnotationDef(NodeLocation location) {
        annotationDefBuilder = new AnnotationDef.AnnotationDefBuilder(location, currentScope);
        currentScope = annotationDefBuilder.getCurrentScope();
    }
    
    /**
     * Creates a {@code AnnotationDef}.
     * 
     * @param location Location of this {@code AnnotationDef} in the source file
     * @param name Name of the {@code AnnotationDef}
     */
    public void addAnnotationtDef(NodeLocation location, String name) {
        annotationDefBuilder.setName(name);
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
     * @param attachmentPoint Point to which this annotation can be attached
     */
    public void addAnnotationtAttachmentPoint(NodeLocation location, String attachmentPoint) {
        annotationDefBuilder.addAttachmentPoint(attachmentPoint);
    }

    /**
     * Create a literal type attribute value.
     * 
     * @param location Location of the value in the source file
     */
    public void createLiteralTypeAttributeValue(NodeLocation location) {
        Expression expr = exprStack.pop();
        if (!(expr instanceof BasicLiteral)) {
            String errMsg = BLangExceptionHelper.constructSemanticError(expr.getNodeLocation(),
                    SemanticErrors.UNSUPPORTED_ANNOTATION_ATTRIBUTE_VALUE);
            errorMsgs.add(errMsg);
        }
        BasicLiteral basicLiteral = (BasicLiteral) expr;
        BValue value = basicLiteral.getBValue();
        annotationAttributeValues.push(new AnnotationAttributeValue(value, basicLiteral.getTypeName(), location));
    }
    
    /**
     * Create an annotation type attribute value.
     * 
     * @param location Location of the value in the source file
     */
    public void createAnnotationTypeAttributeValue(NodeLocation location) {
        AnnotationAttachment value = annonAttachmentStack.pop();
        SimpleTypeName valueType = new SimpleTypeName(value.getName(), value.getPkgName(), value.getPkgPath());
        annotationAttributeValues.push(new AnnotationAttributeValue(value, valueType, location));
    }
    
    /**
     * Create an array type attribute value.
     * 
     * @param location Location of the value in the source file
     */
    public void createArrayTypeAttributeValue(NodeLocation location) {
        SimpleTypeName valueType = new SimpleTypeName(null, true, 1);
        AnnotationAttributeValue arrayValue = new AnnotationAttributeValue(
            annotationAttributeValues.toArray(new AnnotationAttributeValue[annotationAttributeValues.size()]),
            valueType, location);
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
     * @param paramName name of the function parameter
     * @param location  Location of the parameter in the source file
     */
    public void addParam(NodeLocation location, SimpleTypeName typeName, String paramName,
                         int annotationCount, boolean isReturnParam) {
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this parameter is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
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
            ParameterDef paramDef = new ParameterDef(location, null, typeName, null, currentScope);
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
     * @param varName  name of the variable
     */
    public void createVarRefExpr(NodeLocation location, String varName) {
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, varName);
        exprStack.push(variableRefExpr);
    }

    public void createMapArrayVarRefExpr(NodeLocation location, String varName, int dimensions) {
        SymbolName symName = new SymbolName(varName);
        VariableRefExpr arrayVarRefExpr = new VariableRefExpr(location, varName);

        Expression[] indexExprs = new Expression[dimensions];
        int i = 0;
        while (i < dimensions) {
            indexExprs[i++] = exprStack.pop();
        }
        checkArgExprValidity(location, Arrays.asList(indexExprs));

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(symName);
        builder.setIndexExprs(indexExprs);
        builder.setArrayMapVarRefExpr(arrayVarRefExpr);
        builder.setNodeLocation(location);

        ArrayMapAccessExpr accessExpr = builder.build();
        exprStack.push(accessExpr);
    }

    public void createBinaryExpr(NodeLocation location, String opStr) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        Expression lExpr = exprStack.pop();
        checkArgExprValidity(location, lExpr);

        BinaryExpression expr;
        switch (opStr) {
            case "+":
                expr = new AddExpression(location, lExpr, rExpr);
                break;

            case "-":
                expr = new SubtractExpression(location, lExpr, rExpr);
                break;

            case "*":
                expr = new MultExpression(location, lExpr, rExpr);
                break;

            case "/":
                expr = new DivideExpr(location, lExpr, rExpr);
                break;

            case "%":
                expr = new ModExpression(location, lExpr, rExpr);
                break;

            case "&&":
                expr = new AndExpression(location, lExpr, rExpr);
                break;

            case "||":
                expr = new OrExpression(location, lExpr, rExpr);
                break;

            case "==":
                expr = new EqualExpression(location, lExpr, rExpr);
                break;

            case "!=":
                expr = new NotEqualExpression(location, lExpr, rExpr);
                break;

            case ">=":
                expr = new GreaterEqualExpression(location, lExpr, rExpr);
                break;

            case ">":
                expr = new GreaterThanExpression(location, lExpr, rExpr);
                break;

            case "<":
                expr = new LessThanExpression(location, lExpr, rExpr);
                break;

            case "<=":
                expr = new LessEqualExpression(location, lExpr, rExpr);
                break;

            // TODO Add support for bracedExpression, binaryPowExpression, binaryModExpression

            default:
                String errMsg = BLangExceptionHelper.constructSemanticError(location,
                        SemanticErrors.UNSUPPORTED_OPERATOR, opStr);
                errorMsgs.add(errMsg);
                // Creating a dummy expression
                expr = new BinaryExpression(location, lExpr, null, rExpr);
        }

        exprStack.push(expr);
    }

    public void createUnaryExpr(NodeLocation location, String op) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        UnaryExpression expr;
        switch (op) {
            case "+":
                expr = new UnaryExpression(location, Operator.ADD, rExpr);
                break;

            case "-":
                expr = new UnaryExpression(location, Operator.SUB, rExpr);
                break;

            case "!":
                expr = new UnaryExpression(location, Operator.NOT, rExpr);
                break;

            default:
                String errMsg = BLangExceptionHelper
                        .constructSemanticError(location, SemanticErrors.UNSUPPORTED_OPERATOR, op);
                errorMsgs.add(errMsg);

                // Creating a dummy expression
                expr = new UnaryExpression(location, null, rExpr);
        }

        exprStack.push(expr);
    }

    public void createBacktickExpr(NodeLocation location, String stringContent) {
        String templateStr = getValueWithinBackquote(stringContent);
        BacktickExpr backtickExpr = new BacktickExpr(location, templateStr);
        exprStack.push(backtickExpr);
    }

    public void startExprList() {
        exprListStack.push(new ArrayList<>());
    }

    public void endExprList(int exprCount) {
        List<Expression> exprList = exprListStack.peek();
        addExprToList(exprList, exprCount);
    }

    public void addFunctionInvocationExpr(NodeLocation location, NameReference nameReference, boolean argsAvailable) {
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
        exprStack.push(invocationExpr);
    }

    public void addActionInvocationExpr(NodeLocation location, NameReference nameReference, String actionName,
                                        boolean argsAvailable) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);

        if (argsAvailable) {
            List<Expression> argExprList = exprListStack.pop();
            checkArgExprValidity(location, argExprList);
            cIExprBuilder.setExpressionList(argExprList);
        }

        cIExprBuilder.setName(actionName);
        cIExprBuilder.setPkgName(nameReference.pkgName);
        cIExprBuilder.setPkgPath(nameReference.pkgPath);
        cIExprBuilder.setConnectorName(nameReference.name);

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        exprStack.push(invocationExpr);
    }

    public void createTypeCastExpr(NodeLocation location, SimpleTypeName typeName) {
        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        TypeCastExpression typeCastExpression = new TypeCastExpression(location, typeName, rExpr);
        exprStack.push(typeCastExpression);
    }

    public void createArrayInitExpr(NodeLocation location, boolean argsAvailable) {
        List<Expression> argExprList;
        if (argsAvailable) {
            argExprList = exprListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }

        checkArgExprValidity(location, argExprList);

        ArrayInitExpr arrayInitExpr = new ArrayInitExpr(location,
                argExprList.toArray(new Expression[argExprList.size()]));
        exprStack.push(arrayInitExpr);
    }

    public void addMapStructKeyValue(NodeLocation location) {
        Expression valueExpr = exprStack.pop();
        Expression keyExpr = exprStack.pop();

        List<MapStructInitKeyValueExpr> keyValueList = mapStructKVListStack.peek();
        keyValueList.add(new MapStructInitKeyValueExpr(location, keyExpr, valueExpr));
    }

    public void startMapStructLiteral() {
        mapStructKVListStack.push(new ArrayList<>());
    }

    public void createMapStructLiteral(NodeLocation location) {
        List<MapStructInitKeyValueExpr> keyValueExprList = mapStructKVListStack.pop();
        for (MapStructInitKeyValueExpr argExpr : keyValueExprList) {

            if (argExpr.getKeyExpr() instanceof BacktickExpr) {
                String errMsg = BLangExceptionHelper.constructSemanticError(location,
                        SemanticErrors.TEMPLATE_EXPRESSION_NOT_ALLOWED_HERE);
                errorMsgs.add(errMsg);

            }

            if (argExpr.getValueExpr() instanceof BacktickExpr) {
                String errMsg = BLangExceptionHelper.constructSemanticError(location,
                        SemanticErrors.TEMPLATE_EXPRESSION_NOT_ALLOWED_HERE);
                errorMsgs.add(errMsg);

            }
        }

        Expression[] argExprs;
        if (keyValueExprList.size() == 0) {
            argExprs = new Expression[0];
        } else {
            argExprs = keyValueExprList.toArray(new Expression[keyValueExprList.size()]);
        }

        RefTypeInitExpr refTypeInitExpr = new RefTypeInitExpr(location, argExprs);
        exprStack.push(refTypeInitExpr);
    }

    public void createConnectorInitExpr(NodeLocation location, SimpleTypeName typeName, boolean argsAvailable) {
        List<Expression> argExprList;
        if (argsAvailable) {
            argExprList = exprListStack.pop();
            checkArgExprValidity(location, argExprList);
        } else {
            argExprList = new ArrayList<>(0);
        }

        ConnectorInitExpr connectorInitExpr = new ConnectorInitExpr(location, typeName,
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
            parentCUBuilder = currentCUBuilder;
        }
        currentCUBuilder = new Worker.WorkerBuilder(packageScope);
        //setting workerOuterBlockScope if it is not a fork join statement
        if (forkJoinScope == null) {
            workerOuterBlockScope = currentScope;
        }
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addFunction(String name, boolean isNative) {
        currentCUBuilder.setName(name);
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

    public void addTypeMapper(NodeLocation location, String name, SimpleTypeName returnTypeName, boolean isNative) {
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setNative(isNative);
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

        currentCUBuilder = new Resource.ResourceBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
    }

    public void addResource(NodeLocation location, String name, int annotationCount) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments(annotationCount).forEach(attachment -> currentCUBuilder.addAnnotation(attachment));

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        currentScope = resource.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void createWorker(NodeLocation sourceLocation, String name, String paramName) {
        currentCUBuilder.setName(name);
        currentCUBuilder.setNodeLocation(sourceLocation);

        // define worker parameter
        SymbolName paramSymbolName = new SymbolName(paramName);
        ParameterDef paramDef = new ParameterDef(sourceLocation, paramName,
            new SimpleTypeName(BTypes.typeMessage.getName()), paramSymbolName, currentScope);
        currentScope.define(paramSymbolName, paramDef);
        currentCUBuilder.addParameter(paramDef);
        
        Worker worker = currentCUBuilder.buildWorker();
        if (forkJoinStmtBuilderStack.isEmpty()) {
            parentCUBuilder.addWorker(worker);
            //setting the current scope to resource block
            currentScope = workerOuterBlockScope;
        } else {
            workerStack.peek().add(worker);
            currentScope = forkJoinScope;
        }

        currentCUBuilder = parentCUBuilder;
        parentCUBuilder = null;
        workerOuterBlockScope = null;
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

    public void addAction(String name, boolean isNative, int annotationCount) {
        currentCUBuilder.setName(name);
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

    public void createService(String name) {
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments().forEach(attachment -> currentCUGroupBuilder.addAnnotation(attachment));

        Service service = currentCUGroupBuilder.buildService();
        bFileBuilder.addService(service);

        currentScope = service.getEnclosingScope();
        currentCUGroupBuilder = null;
    }

    public void createConnector(String name) {
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        getAnnotationAttachments().forEach(attachment -> currentCUGroupBuilder.addAnnotation(attachment));

        BallerinaConnectorDef connector = currentCUGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

        currentScope = connector.getEnclosingScope();
        currentCUGroupBuilder = null;
    }


    // Statements

    public void addVariableDefinitionStmt(NodeLocation location, SimpleTypeName typeName,
                                          String varName, boolean exprAvailable) {

        VariableRefExpr variableRefExpr = new VariableRefExpr(location, varName);
        SymbolName symbolName = new SymbolName(varName);

        VariableDef variableDef = new VariableDef(location, varName, typeName, symbolName, currentScope);
        variableRefExpr.setVariableDef(variableDef);

        Expression rhsExpr = exprAvailable ? exprStack.pop() : null;
        VariableDefStmt variableDefStmt = new VariableDefStmt(location, variableDef, variableRefExpr, rhsExpr);

        if (blockStmtBuilderStack.size() == 0 && currentCUGroupBuilder != null) {
            if (rhsExpr != null) {
                if (rhsExpr instanceof ActionInvocationExpr) {
                    String errMsg = BLangExceptionHelper.constructSemanticError(location,
                            SemanticErrors.ACTION_INVOCATION_NOT_ALLOWED_HERE);
                    errorMsgs.add(errMsg);
                }

//                if (rhsExpr instanceof BasicLiteral || rhsExpr instanceof VariableRefExpr) {
//                    currentCUGroupBuilder.addVariableDef(variableDefStmt);
//                } else {
//                    String errMsg = getNodeLocationStr(location) +
//                            "only a basic literal or a variable reference is allowed here ";
//                    errorMsgs.add(errMsg);
//                }
            }
            currentCUGroupBuilder.addVariableDef(variableDefStmt);
        } else {
            addToBlockStmt(variableDefStmt);
        }
    }

    public void addCommentStmt(NodeLocation location, String comment) {
        CommentStmt commentStmt = new CommentStmt(location, comment);
        addToBlockStmt(commentStmt);
    }

    public void createAssignmentStmt(NodeLocation location) {
        Expression rExpr = exprStack.pop();
        List<Expression> lExprList = exprListStack.pop();

        AssignStmt assignStmt = new AssignStmt(location, lExprList.toArray(new Expression[lExprList.size()]), rExpr);
        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt(NodeLocation location) {
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

        ReturnStmt returnStmt = new ReturnStmt(location, exprs);
        addToBlockStmt(returnStmt);
    }

    public void createReplyStmt(NodeLocation location) {
        Expression argExpr = exprStack.pop();
        if (!(argExpr instanceof VariableRefExpr)) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REF_TYPE_MESSAGE_ALLOWED);
            errorMsgs.add(errMsg);
        }
        ReplyStmt replyStmt = new ReplyStmt(location, argExpr);
        addToBlockStmt(replyStmt);
    }

    public void startWhileStmt(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void createWhileStmt(NodeLocation location) {
        // Create a while statement builder
        WhileStmt.WhileStmtBuilder whileStmtBuilder = new WhileStmt.WhileStmtBuilder();
        whileStmtBuilder.setNodeLocation(location);

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

    public void createBreakStmt(NodeLocation location) {
        BreakStmt.BreakStmtBuilder breakStmtBuilder = new BreakStmt.BreakStmtBuilder();
        breakStmtBuilder.setNodeLocation(location);
        BreakStmt breakStmt = breakStmtBuilder.build();
        addToBlockStmt(breakStmt);
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

    public void addIfClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        Expression condition = exprStack.pop();
        checkArgExprValidity(ifElseStmtBuilder.getLocation(), condition);
        ifElseStmtBuilder.setIfCondition(condition);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        ifElseStmtBuilder.setThenBody(blockStmt);

        currentScope = blockStmt.getEnclosingScope();
    }

    public void addElseIfClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseIfStmtBlock = blockStmtBuilder.build();

        Expression condition = exprStack.pop();
        checkArgExprValidity(ifElseStmtBuilder.getLocation(), condition);
        ifElseStmtBuilder.addElseIfBlock(elseIfStmtBlock.getNodeLocation(), condition, elseIfStmtBlock);

        currentScope = elseIfStmtBlock.getEnclosingScope();
    }

    public void startElseClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void addElseClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
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

    public void startCatchClause(NodeLocation location) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        // Creating Try clause.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt tryBlock = blockStmtBuilder.build();
        tryCatchStmtBuilder.setTryBlock(tryBlock);
        currentScope = tryBlock.getEnclosingScope();

        // Staring parsing catch clause.
        TryCatchStmt.CatchBlock catchBlock = new TryCatchStmt.CatchBlock(currentScope);
        tryCatchStmtBuilder.setCatchBlock(catchBlock);
        currentScope = catchBlock;

        BlockStmt.BlockStmtBuilder catchBlockBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(catchBlockBuilder);

        currentScope = catchBlockBuilder.getCurrentScope();
    }

    public void addCatchClause(NodeLocation location, SimpleTypeName exceptionType, String argName) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        if (!TypeConstants.EXCEPTION_TNAME.equals(exceptionType.getName())) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.ONLY_EXCEPTION_TYPE_HERE);
            errorMsgs.add(errMsg);
        }

        BlockStmt.BlockStmtBuilder catchBlockBuilder = blockStmtBuilderStack.pop();
        BlockStmt catchBlock = catchBlockBuilder.build();
        currentScope = catchBlock.getEnclosingScope();

        SymbolName symbolName = new SymbolName(argName);
        ParameterDef paramDef = new ParameterDef(catchBlock.getNodeLocation(), argName, exceptionType, symbolName,
                currentScope);
        currentScope.resolve(symbolName);
        currentScope.define(symbolName, paramDef);
        tryCatchStmtBuilder.getCatchBlock().setParameterDef(paramDef);
        tryCatchStmtBuilder.setCatchBlockStmt(catchBlock);
    }

    public void addTryCatchStmt() {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.pop();
        TryCatchStmt tryCatchStmt = tryCatchStmtBuilder.build();
        addToBlockStmt(tryCatchStmt);
    }

    public void createThrowStmt(NodeLocation location) {
        Expression expression = exprStack.pop();
        if (expression instanceof VariableRefExpr || expression instanceof FunctionInvocationExpr) {
            ThrowStmt throwStmt = new ThrowStmt(location, expression);
            addToBlockStmt(throwStmt);
            return;
        }
        String errMsg = BLangExceptionHelper.constructSemanticError(location, SemanticErrors.ONLY_EXCEPTION_TYPE_HERE);
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
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt forkJoinStmt = blockStmtBuilder.build();
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
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
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt timeoutStmt = blockStmtBuilder.build();
        forkJoinStmtBuilder.setTimeoutBlock(timeoutStmt);
        forkJoinStmtBuilder.setTimeoutExpression(exprStack.pop());
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
        forkJoinStmtBuilder.setTimeoutResult(paramDef);
        currentScope = forkJoinStmtBuilder.getTimeout().getEnclosingScope();
    }

    public void endForkJoinStmt() {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.pop();

        List<Worker> workerList = workerStack.pop();
        if (workerList != null) {
            forkJoinStmtBuilder.setWorkers(workerList.toArray(new Worker[workerList.size()]));
        }
        forkJoinStmtBuilder.setMessageReference((VariableRefExpr) exprStack.pop());
        ForkJoinStmt forkJoinStmt = forkJoinStmtBuilder.build();
        addToBlockStmt(forkJoinStmt);
        currentScope = forkJoinStmt.getEnclosingScope();

    }

    public void createFunctionInvocationStmt(NodeLocation location, NameReference nameReference,
                                             boolean argsAvailable) {

        addFunctionInvocationExpr(location, nameReference, argsAvailable);
        FunctionInvocationExpr invocationExpr = (FunctionInvocationExpr) exprStack.pop();


        FunctionInvocationStmt functionInvocationStmt = new FunctionInvocationStmt(location, invocationExpr);
        blockStmtBuilderStack.peek().addStmt(functionInvocationStmt);
    }

    public void createWorkerInvocationStmt(String receivingMsgRef, String workerName, NodeLocation sourceLocation) {
        VariableRefExpr variableRefExpr = new VariableRefExpr(sourceLocation, new SymbolName(receivingMsgRef));
        WorkerInvocationStmt workerInvocationStmt = new WorkerInvocationStmt(workerName, sourceLocation);
        //workerInvocationStmt.setLocation(sourceLocation);
        workerInvocationStmt.setInMsg(variableRefExpr);
        blockStmtBuilderStack.peek().addStmt(workerInvocationStmt);
    }

    public void createWorkerReplyStmt(String receivingMsgRef, String workerName, NodeLocation sourceLocation) {
        VariableRefExpr variableRefExpr = new VariableRefExpr(sourceLocation, new SymbolName(receivingMsgRef));
        WorkerReplyStmt workerReplyStmt = new WorkerReplyStmt(variableRefExpr, workerName, sourceLocation);
        //workerReplyStmt.setLocation(sourceLocation);
        blockStmtBuilderStack.peek().addStmt(workerReplyStmt);
    }

    public void createActionInvocationStmt(NodeLocation location, NameReference nameReference, String actionName,
                                           boolean argsAvailable) {
        addActionInvocationExpr(location, nameReference, actionName, argsAvailable);
        ActionInvocationExpr invocationExpr = (ActionInvocationExpr) exprStack.pop();

        ActionInvocationStmt actionInvocationStmt = new ActionInvocationStmt(location, invocationExpr);
        blockStmtBuilderStack.peek().addStmt(actionInvocationStmt);
    }


    // Literal Values

    public void createIntegerLiteral(NodeLocation location, String value) {
        BValueType bValue = new BInteger(Long.parseLong(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.INT_TNAME), bValue);
    }

    public void createFloatLiteral(NodeLocation location, String value) {
        BValueType bValue = new BFloat(Double.parseDouble(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.FLOAT_TNAME), bValue);
    }

    public void createStringLiteral(NodeLocation location, String value) {
        BValueType bValue = new BString(value);
        createLiteral(location, new SimpleTypeName(TypeConstants.STRING_TNAME), bValue);
    }

    public void createBooleanLiteral(NodeLocation location, String value) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.BOOLEAN_TNAME), bValue);
    }

    public void createNullLiteral(NodeLocation location, String value) {
        NullLiteral nullLiteral = new NullLiteral(location);
        exprStack.push(nullLiteral);
    }

    public void validateAndSetPackagePath(NodeLocation location, NameReference nameReference) {
        String name = nameReference.getName();
        String pkgName = nameReference.getPackageName();
        ImportPackage importPkg = getImportPackage(pkgName);
        checkForUndefinedPackagePath(location, pkgName, importPkg, () -> pkgName + ":" + name);

        if (importPkg == null) {
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

    private void createLiteral(NodeLocation location, SimpleTypeName typeName, BValueType bValueType) {
        BasicLiteral basicLiteral = new BasicLiteral(location, typeName, bValueType);
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
     * Create an expression for accessing fields of user defined struct types.
     *
     * @param location Source location of the ballerina file
     */
    public void createStructFieldRefExpr(NodeLocation location) {
        if (exprStack.size() < 2) {
            return;
        }
        ReferenceExpr field = (ReferenceExpr) exprStack.pop();
        StructFieldAccessExpr fieldExpr;
        if (field instanceof StructFieldAccessExpr) {
            fieldExpr = (StructFieldAccessExpr) field;
        } else {
            fieldExpr = new StructFieldAccessExpr(location, field.getSymbolName(), field);
        }

        ReferenceExpr parent = (ReferenceExpr) exprStack.pop();
        StructFieldAccessExpr parentExpr = new StructFieldAccessExpr(location, parent, fieldExpr);

        exprStack.push(parentExpr);
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
        if (argExpr instanceof BacktickExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.TEMPLATE_EXPRESSION_NOT_ALLOWED_HERE);

        } else if (argExpr instanceof ArrayInitExpr) {
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


    /**
     * This class represents CallableUnitName used in function and action invocation expressions.
     */
    public static class NameReference {
        private String pkgName;
        private String name;
        private String pkgPath;

        public NameReference(String pkgName, String name) {
            this.name = name;
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
    }
}
