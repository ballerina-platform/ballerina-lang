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
package org.wso2.ballerina.core.model.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.ConnectorInitExpr;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapStructInitKeyValueExpr;
import org.wso2.ballerina.core.model.expressions.ModExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.RefTypeInitExpr;
import org.wso2.ballerina.core.model.expressions.ReferenceExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.types.TypeConstants;
import org.wso2.ballerina.core.model.types.TypeVertex;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.ballerina.core.model.util.LangModelUtils.getNodeLocationStr;

/**
 * {@code BLangModelBuilder} provides an high-level API to create Ballerina language object model(AST).
 * <p>
 * Here we define constants, Structs, services symbols. Other symbols will be defined in the next phase
 *
 * @since 0.8.0
 */
public class BLangModelBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BLangModelBuilder.class);

    private String currentPackagePath;
    private BallerinaFile.BFileBuilder bFileBuilder = new BallerinaFile.BFileBuilder();

    private SymbolScope currentScope;

    // Builds connectors and services.
    private CallableUnitGroupBuilder currentCUGroupBuilder;

    // Builds functions, actions and resources.
    private CallableUnitBuilder currentCUBuilder;

    // Builds user defined structs.
    private StructDef.StructBuilder currentStructBuilder;

    private Stack<Annotation.AnnotationBuilder> annotationBuilderStack = new Stack<>();
    private Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    private Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    private Stack<SimpleTypeName> typeNameStack = new Stack<>();
    private Stack<CallableUnitName> callableUnitNameStack = new Stack<>();
    private Stack<Expression> exprStack = new Stack<>();

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    private Stack<List<Expression>> exprListStack = new Stack<>();
    private Stack<List<Annotation>> annotationListStack = new Stack<>();
    private Stack<MapStructInitKeyValueExpr> mapStructInitKVStack = new Stack<>();
    private Stack<List<MapStructInitKeyValueExpr>> mapStructInitKVListStack = new Stack<>();

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    private Map<String, ImportPackage> importPkgMap = new HashMap<>();

    private List<String> errorMsgs = new ArrayList<>();

    public BLangModelBuilder() {
    }

    public BLangModelBuilder(SymbolScope packageScope) {
        this.currentScope = packageScope;

        // TODO Add a description why.
        startRefTypeInitExpr();
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

                    throw new SemanticException(getNodeLocationStr(location) +
                            "unused import package " + importPkgErrStr + "");
                });

        if (errorMsgs.size() > 0) {
            throw new SemanticException(errorMsgs.get(0));
        }

        bFileBuilder.setImportPackageMap(importPkgMap);
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
            String errMsg = getNodeLocationStr(location) +
                    "redeclared imported package name '" + importPkg.getName() + "'";
            errorMsgs.add(errMsg);
        }

        bFileBuilder.addImportPackage(importPkg);
        importPkgMap.put(importPkg.getName(), importPkg);
    }


    // Add types. SimpleTypes, Types with full scheme, schema URL or schema ID

    public void addSimpleTypeName(NodeLocation location, String name, String pkgName, boolean isArrayType) {
        SimpleTypeName typeName = null;
        ImportPackage importPkg = getImportPackage(pkgName);
        checkForUndefinedPackagePath(location, pkgName, importPkg, () -> pkgName + ":" + name);

        if (importPkg != null) {
            importPkg.markUsed();
            typeName = new SimpleTypeName(name, pkgName, importPkg.getPath());
        }

        if (typeName == null) {
            typeName = new SimpleTypeName(name);
        }

        typeName.setArrayType(isArrayType);
        typeNameStack.add(typeName);
    }


    // Add constant definitions;

    public void addConstantDef(NodeLocation location, String name, boolean isPublic) {
        SymbolName symbolName = new SymbolName(name, currentPackagePath);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + name + "'";
            errorMsgs.add(errMsg);
        }

        SimpleTypeName typeName = typeNameStack.pop();
        ConstDef constantDef = new ConstDef(location, name, typeName, currentPackagePath,
                isPublic, symbolName, currentScope, exprStack.pop());

        // Define the variableRef symbol in the current scope
        currentScope.define(symbolName, constantDef);

        // Add constant definition to current file;
        bFileBuilder.addConst(constantDef);
    }


    // Add Struct definition

    /**
     * Start a struct builder.
     */
    public void startStructDef(NodeLocation location) {
        currentStructBuilder = new StructDef.StructBuilder(location, currentScope);
        currentScope = currentStructBuilder.getCurrentScope();
    }

    /**
     * Add an field of the {@link StructDef}.
     *
     * @param location  Location of the field in the source file
     * @param fieldName Name of the field in the {@link StructDef}
     */
    public void addStructField(NodeLocation location, String fieldName) {
        // TODO: add currentPackagePath path to symbol name. i.e:  new SymbolName(fieldName, currentPackagePath);
        SymbolName symbolName = new SymbolName(fieldName);

        // Check whether this constant is already defined.
        StructDef structScope = (StructDef) currentScope;
        BLangSymbol fieldSymbol = structScope.resolveMembers(symbolName);
        if (fieldSymbol != null) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + fieldName + "'";
            errorMsgs.add(errMsg);
        }

        // TODO Fix this..
        SimpleTypeName typeName = typeNameStack.remove(0);
        VariableDef variableDef = new VariableDef(location, fieldName, typeName, symbolName, currentScope);

        // Define the variableRef symbol in the current scope
        currentScope.define(symbolName, variableDef);

        // Add Struct field to current Struct;
        currentStructBuilder.addField(variableDef);
    }

    /**
     * Creates a {@link StructDef}.
     *
     * @param location Location of this {@link StructDef} in the source file
     * @param name     Name of the {@link StructDef}
     */
    public void addStructDef(NodeLocation location, String name) {
        currentStructBuilder.setName(name);
        
        // TODO: Fix the package path
//        currentStructBuilder.setPackagePath(currentPackagePath);
        StructDef structDef = currentStructBuilder.build();

        // Close Struct scope
        currentScope = structDef.getEnclosingScope();
        currentStructBuilder = null;

        // Define StructDef Symbol in the package scope..
        // TODO: add currentPackagePath when creating the SymbolName
        SymbolName symbolName = new SymbolName(name);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + name + "'";
            errorMsgs.add(errMsg);
        }

        currentScope.define(symbolName, structDef);
        bFileBuilder.addStruct(structDef);
    }


    // Annotations

    public void startAnnotation() {
        annotationBuilderStack.push(new Annotation.AnnotationBuilder());
    }

    public void createAnnotationKeyValue(String key) {
        //        // Assuming the annotation value is a string literal
        //        String value = exprStack.pop().getBValueRef().getString();
        //
        //        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.peek();
        //        annotationBuilder.addKeyValuePair(new Identifier(key), value);

        LOGGER.warn("Warning: Key/Value pairs in annotations are not supported");
    }

    public void endAnnotation(String name, boolean valueAvailable, NodeLocation location) {
        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.pop();
        annotationBuilder.setNodeLocation(location);
        annotationBuilder.setName(new SymbolName(name));

        if (valueAvailable) {
            Expression expr = exprStack.pop();

            // Assuming the annotation value is a string literal
            if (expr instanceof BasicLiteral &&
                    ((BasicLiteral) expr).getTypeName().getName().equals(TypeConstants.STRING_TNAME)) {
                String value = ((BasicLiteral) expr).getBValue().stringValue();
                annotationBuilder.setValue(value);
            } else {
                throw new RuntimeException("Annotations with key/value pars are not support at the moment" + " in " +
                        location.getFileName() + ":" + location.getLineNumber());
            }
        }

        List<Annotation> annotationList = annotationListStack.peek();
        Annotation annotation = annotationBuilder.build();
        annotationList.add(annotation);
    }


    // Function/action input and out parameters

    /**
     * Create a function parameter and a corresponding variable reference expression.
     * <p/>
     * Set the even function to get the value from the function arguments with the correct index.
     * Store the reference in the symbol table.
     *
     * @param paramName name of the function parameter
     */
    public void addParam(String paramName, NodeLocation location) {
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + paramName + "'";
            errorMsgs.add(errMsg);
        }


        SimpleTypeName typeName = typeNameStack.pop();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);

        if (currentCUBuilder != null) {
            // Add the parameter to callableUnitBuilder.
            currentCUBuilder.addParameter(paramDef);
        } else {
            currentCUGroupBuilder.addParameter(paramDef);
        }

        currentScope.define(symbolName, paramDef);
    }

    public void createReturnTypes(NodeLocation location) {
        // TODO This implement is inefficient. Refactor this ASAP
        List<SimpleTypeName> typeNameList = new ArrayList<>(typeNameStack.size());
        while (!typeNameStack.isEmpty()) {
            typeNameList.add(typeNameStack.pop());
        }
        Collections.reverse(typeNameList);

        for (SimpleTypeName typeName : typeNameList) {
            ParameterDef paramDef = new ParameterDef(location, null, typeName, null, currentScope);
            currentCUBuilder.addReturnParameter(paramDef);
        }
    }

    public void createNamedReturnParam(NodeLocation location, String paramName) {
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                    ": redeclared symbol '" + paramName + "'";
            errorMsgs.add(errMsg);
        }

        SimpleTypeName typeName = typeNameStack.pop();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
        currentCUBuilder.addReturnParameter(paramDef);

        currentScope.define(symbolName, paramDef);
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
     * Create variable reference expression.
     * <p/>
     * There are three types of variables references as per the grammar file.
     * 1) Simple variable references. a, b, index etc
     * 2) Map or array access a[1], m["key"]
     * 3) Struct field access  Person.name
     */
    public void createVarRefExpr(NodeLocation location, String varName) {
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, varName);
        exprStack.push(variableRefExpr);
    }

    public void createMapArrayVarRefExpr(NodeLocation location, String varName) {
        SymbolName symName = new SymbolName(varName);
        VariableRefExpr arrayVarRefExpr = new VariableRefExpr(location, varName);

        Expression indexExpr = exprStack.pop();
        checkArgExprValidity(location, indexExpr);

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(symName);
        builder.setIndexExpr(indexExpr);
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
                String errMsg = getNodeLocationStr(location) + "unsupported operator '" + opStr + "'";
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
                String errMsg = getNodeLocationStr(location) +
                        "unsupported operator '" + op + "'";
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

    public void addFunctionInvocationExpr(NodeLocation location) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);

        List<Expression> argExprList = exprListStack.pop();
        checkArgExprValidity(location, argExprList);
        cIExprBuilder.setExpressionList(argExprList);

        CallableUnitName callableUnitName = callableUnitNameStack.pop();
        ImportPackage importPkg = getImportPackage(callableUnitName.pkgName);
        checkForUndefinedPackagePath(location, callableUnitName.pkgName, importPkg,
                () -> callableUnitName.pkgName + ":" + callableUnitName.name);

        if (importPkg != null) {
            importPkg.markUsed();
            cIExprBuilder.setPkgPath(importPkg.getPath());
        }

        cIExprBuilder.setName(callableUnitName.name);
        cIExprBuilder.setPkgName(callableUnitName.pkgName);
        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        exprStack.push(invocationExpr);
    }

    public void addActionInvocationExpr(NodeLocation location, String actionName) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);

        List<Expression> argExprList = exprListStack.pop();
        checkArgExprValidity(location, argExprList);
        cIExprBuilder.setExpressionList(argExprList);

        CallableUnitName callableUnitName = callableUnitNameStack.pop();
        ImportPackage importPkg = getImportPackage(callableUnitName.pkgName);
        checkForUndefinedPackagePath(location, callableUnitName.pkgName, importPkg,
                () -> callableUnitName.pkgName + ":" + callableUnitName.name + "." + actionName);

        if (importPkg != null) {
            importPkg.markUsed();
            cIExprBuilder.setPkgPath(importPkg.getPath());
        }

        cIExprBuilder.setName(actionName);
        cIExprBuilder.setPkgName(callableUnitName.pkgName);
        cIExprBuilder.setConnectorName(callableUnitName.name);

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        exprStack.push(invocationExpr);
    }

    public void createTypeCastExpr(NodeLocation location) {
        SimpleTypeName typeName = typeNameStack.pop();

        Expression rExpr = exprStack.pop();
        checkArgExprValidity(location, rExpr);

        TypeCastExpression typeCastExpression = new TypeCastExpression(location, typeName, rExpr);
        exprStack.push(typeCastExpression);
    }

    public void createArrayInitExpr(NodeLocation location) {
        List<Expression> argExprList;
        if (!exprListStack.isEmpty()) {
            argExprList = exprListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }

        checkArgExprValidity(location, argExprList);

        ArrayInitExpr arrayInitExpr = new ArrayInitExpr(location,
                argExprList.toArray(new Expression[argExprList.size()]));
        exprStack.push(arrayInitExpr);
    }

    public void createMapStructInitKeyValue(NodeLocation location) {
        // TODO Validate key/value expressions. e.g. key can't be an array init expression.
        Expression valueExpr = exprStack.pop();
        Expression keyExpr = exprStack.pop();

        mapStructInitKVStack.push(new MapStructInitKeyValueExpr(location, keyExpr, valueExpr));
    }

    public void endMapStructInitKeyValueList(int exprCount) {
        List<MapStructInitKeyValueExpr> keyValueList = mapStructInitKVListStack.peek();
        addKeyValueToList(keyValueList, exprCount);
    }

    public void createRefTypeInitExpr(NodeLocation location) {
        List<MapStructInitKeyValueExpr> keyValueExprList = mapStructInitKVListStack.pop();
        for (MapStructInitKeyValueExpr argExpr : keyValueExprList) {
            checkArgExprValidity(location, argExpr.getKeyExpr());
            checkArgExprValidity(location, argExpr.getValueExpr());
        }

        Expression[] argExprs;
        if (keyValueExprList.size() == 0) {
            argExprs = new Expression[0];
        } else {
            argExprs = keyValueExprList.toArray(new Expression[keyValueExprList.size()]);
        }

        RefTypeInitExpr refTypeInitExpr = new RefTypeInitExpr(location, argExprs);
        exprStack.push(refTypeInitExpr);

        startRefTypeInitExpr();
    }

    public void createConnectorInitExpr(NodeLocation location) {
        List<Expression> argExprList = exprListStack.pop();
        checkArgExprValidity(location, argExprList);
        SimpleTypeName typeName = typeNameStack.pop();

        ConnectorInitExpr connectorInitExpr = new ConnectorInitExpr(location, typeName,
                argExprList.toArray(new Expression[argExprList.size()]));
        exprStack.push(connectorInitExpr);
    }

    public void addCallableUnitName(String pkgName, String name) {
        CallableUnitName callableUnitName = new CallableUnitName(pkgName, name);
        callableUnitNameStack.push(callableUnitName);
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

    public void startFunctionDef() {
        currentCUBuilder = new BallerinaFunction.BallerinaFunctionBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addFunction(NodeLocation location, String name, boolean isPublic) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPublic(isPublic);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaFunction function = currentCUBuilder.buildFunction();
        bFileBuilder.addFunction(function);

        // Define function is delayed due to missing type info of Parameters.

        currentScope = function.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startTypeConverterDef() {
        currentCUBuilder = new BTypeConvertor.BTypeConvertorBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addTypeConverter(String source, String target, String name, NodeLocation location, boolean isPublic) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setPublic(isPublic);

        BTypeConvertor typeConvertor = currentCUBuilder.buildTypeConverter();
        TypeVertex sourceV = new TypeVertex(BTypes.resolveType(new SimpleTypeName(source),
                currentScope, location), currentPackagePath);
        TypeVertex targetV = new TypeVertex(BTypes.resolveType(new SimpleTypeName(target),
                currentScope, location), currentPackagePath);
        bFileBuilder.addTypeConvertor(sourceV, targetV, typeConvertor, currentPackagePath);

        // Define type converter is delayed due to missing type info of Parameters.

        currentScope = typeConvertor.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startResourceDef() {
        if (currentScope instanceof BlockStmt) {
            endCallableUnitBody();
        }

        currentCUBuilder = new Resource.ResourceBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addResource(NodeLocation location, String name) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);
        // TODO Figure out whether we need to support public type convertors
//        currentCUBuilder.setPublic(isPublic);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        // Define resource is delayed due to missing type info of Parameters.

        currentScope = resource.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startActionDef() {
        if (currentScope instanceof BlockStmt) {
            endCallableUnitBody();
        }

        currentCUBuilder = new BallerinaAction.BallerinaActionBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addAction(NodeLocation location, String name) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaAction action = currentCUBuilder.buildAction();
        currentCUGroupBuilder.addAction(action);

        // Define action is delayed due to missing type info of Parameters.

        currentScope = action.getEnclosingScope();
        currentCUBuilder = null;
    }


    // Services and Connectors

    public void startServiceDef(NodeLocation location) {
        currentCUGroupBuilder = new Service.ServiceBuilder(currentScope);
        currentCUGroupBuilder.setNodeLocation(location);
        currentScope = currentCUGroupBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void startConnectorDef(NodeLocation location) {
        currentCUGroupBuilder = new BallerinaConnectorDef.BallerinaConnectorDefBuilder(currentScope);
        currentCUGroupBuilder.setNodeLocation(location);
        currentScope = currentCUGroupBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void createService(NodeLocation location, String name) {
        currentCUGroupBuilder.setNodeLocation(location);
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        Service service = currentCUGroupBuilder.buildService();
        bFileBuilder.addService(service);

        // Define Service Symbol in the package scope..
        SymbolName symbolName = new SymbolName(name, currentPackagePath);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + name + "'";
            errorMsgs.add(errMsg);
        }

        currentScope = service.getEnclosingScope();
        currentCUGroupBuilder = null;
    }

    public void createConnector(NodeLocation location, String name) {
        currentCUGroupBuilder.setNodeLocation(location);
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        BallerinaConnectorDef connector = currentCUGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

        // Define ConnectorDef Symbol in the package scope..
        SymbolName symbolName = new SymbolName(name);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = getNodeLocationStr(location) +
                    "redeclared symbol '" + name + "'";
            errorMsgs.add(errMsg);
        }

        currentScope = connector.getEnclosingScope();
        currentCUGroupBuilder = null;
    }


    // Statements

    public void addVariableDefinitionStmt(NodeLocation location, String varName, boolean exprAvailable) {
        SimpleTypeName typeName = typeNameStack.pop();
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, varName);

        SymbolName symbolName = new SymbolName(varName);
        VariableDef variableDef = new VariableDef(location, varName, typeName, symbolName, currentScope);
        variableRefExpr.setVariableDef(variableDef);

        Expression rhsExpr = exprAvailable ? exprStack.pop() : null;
        VariableDefStmt variableDefStmt = new VariableDefStmt(location, variableDef, variableRefExpr, rhsExpr);

        if (blockStmtBuilderStack.size() == 0 && currentCUGroupBuilder != null) {

            if (rhsExpr != null) {
                checkArgExprValidity(location, rhsExpr);
                if (rhsExpr instanceof FunctionInvocationExpr) {
                    String errMsg = getNodeLocationStr(location) +
                            "function invocation is not allowed here";
                    errorMsgs.add(errMsg);

                } else if (!(rhsExpr instanceof BasicLiteral) && !(rhsExpr instanceof VariableRefExpr)) {
                    String errMsg = getNodeLocationStr(location) +
                            "a basic literal or a variable reference is allowed here";
                    errorMsgs.add(errMsg);
                }
            }

            currentCUGroupBuilder.addVariableDef(variableDefStmt);
        } else {
            addToBlockStmt(variableDefStmt);
        }
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
            String errMsg = getNodeLocationStr(location) +
                    "only a variable reference of type 'message' is allowed here";
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

    public void startIfElseStmt(NodeLocation location) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = new IfElseStmt.IfElseStmtBuilder();
        ifElseStmtBuilder.setNodeLocation(location);
        ifElseStmtBuilderStack.push(ifElseStmtBuilder);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
    }

    public void startElseIfClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder.getCurrentScope();
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

        Expression condition = exprStack.pop();
        checkArgExprValidity(ifElseStmtBuilder.getLocation(), condition);
        ifElseStmtBuilder.setIfCondition(condition);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        ifElseStmtBuilder.setThenBody(blockStmt);

        IfElseStmt ifElseStmt = ifElseStmtBuilder.build();
        addToBlockStmt(ifElseStmt);

        currentScope = blockStmt.getEnclosingScope();
    }

    public void createFunctionInvocationStmt(NodeLocation location) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);
        List<Expression> argExprList = exprListStack.pop();
        checkArgExprValidity(location, argExprList);
        cIExprBuilder.setExpressionList(argExprList);

        CallableUnitName callableUnitName = callableUnitNameStack.pop();
        ImportPackage importPkg = getImportPackage(callableUnitName.pkgName);
        checkForUndefinedPackagePath(location, callableUnitName.pkgName, importPkg,
                () -> callableUnitName.pkgName + ":" + callableUnitName.name);

        if (importPkg != null) {
            importPkg.markUsed();
            cIExprBuilder.setPkgPath(importPkg.getPath());
        }

        cIExprBuilder.setName(callableUnitName.name);
        cIExprBuilder.setPkgName(callableUnitName.pkgName);

        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        FunctionInvocationStmt functionInvocationStmt = new FunctionInvocationStmt(location, invocationExpr);
        blockStmtBuilderStack.peek().addStmt(functionInvocationStmt);
    }

    public void createActionInvocationStmt(NodeLocation location, String actionName) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);

        List<Expression> argExprList = exprListStack.pop();
        checkArgExprValidity(location, argExprList);
        cIExprBuilder.setExpressionList(argExprList);

        CallableUnitName callableUnitName = callableUnitNameStack.pop();
        ImportPackage importPkg = getImportPackage(callableUnitName.pkgName);
        checkForUndefinedPackagePath(location, callableUnitName.pkgName, importPkg,
                () -> callableUnitName.pkgName + ":" + callableUnitName.name + "." + actionName);

        if (importPkg != null) {
            importPkg.markUsed();
            cIExprBuilder.setPkgPath(importPkg.getPath());
        }

        cIExprBuilder.setName(actionName);
        cIExprBuilder.setPkgName(callableUnitName.pkgName);
        cIExprBuilder.setConnectorName(callableUnitName.name);

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();

        ActionInvocationStmt actionInvocationStmt = new ActionInvocationStmt(location, invocationExpr);
        blockStmtBuilderStack.peek().addStmt(actionInvocationStmt);
    }


    // Literal Values

    public void createIntegerLiteral(String value, NodeLocation location) {
        BValueType bValue = new BInteger(Integer.parseInt(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.INT_TNAME), bValue);
    }

    public void createLongLiteral(String value, NodeLocation location) {
        BValueType bValue = new BLong(Long.parseLong(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.LONG_TNAME), bValue);
    }

    public void createFloatLiteral(String value, NodeLocation location) {
        BValueType bValue = new BFloat(Float.parseFloat(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.FLOAT_TNAME), bValue);
    }

    public void createDoubleLiteral(String value, NodeLocation location) {
        BValueType bValue = new BDouble(Double.parseDouble(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.DOUBLE_TNAME), bValue);
    }

    public void createStringLiteral(String value, NodeLocation location) {
        BValueType bValue = new BString(value);
        createLiteral(location, new SimpleTypeName(TypeConstants.STRING_TNAME), bValue);
    }

    public void createBooleanLiteral(String value, NodeLocation location) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(location, new SimpleTypeName(TypeConstants.BOOLEAN_TNAME), bValue);
    }

    public void createNullLiteral(String value, NodeLocation location) {
        throw new RuntimeException("null values are not yet supported in Ballerina in " + location.getFileName()
                + ":" + location.getLineNumber());
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
     * @param keyValueDataHolderList List<KeyValueDataHolder>
     * @param n                      number of expression to be added the given list
     */
    private void addKeyValueToList(List<MapStructInitKeyValueExpr> keyValueDataHolderList, int n) {

        if (mapStructInitKVStack.isEmpty()) {
            throw new IllegalStateException("KeyValue stack cannot be empty in processing a KeyValueList");
        }

        if (n == 1) {
            MapStructInitKeyValueExpr keyValue = mapStructInitKVStack.pop();
            keyValueDataHolderList.add(keyValue);
        } else {
            MapStructInitKeyValueExpr keyValue = mapStructInitKVStack.pop();
            addKeyValueToList(keyValueDataHolderList, n - 1);
            keyValueDataHolderList.add(keyValue);
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

    private void startRefTypeInitExpr() {
        mapStructInitKVListStack.push(new ArrayList<>());
    }

    private ImportPackage getImportPackage(String pkgName) {
        return (pkgName != null) ? importPkgMap.get(pkgName) : null;
    }

    private void checkForUndefinedPackagePath(NodeLocation location,
                                              String pkgName,
                                              ImportPackage importPackage,
                                              Supplier<String> symbolNameSupplier) {
        if (pkgName != null && importPackage == null) {
            String errMsg = getNodeLocationStr(location) +
                    "undefined package name '" + pkgName + "' in '" + symbolNameSupplier.get() + "'";
            errorMsgs.add(errMsg);
        }
    }

    private void checkArgExprValidity(NodeLocation location, List<Expression> argExprList) {
        for (Expression argExpr : argExprList) {
            checkArgExprValidity(location, argExpr);
        }
    }

    private void checkArgExprValidity(NodeLocation location, Expression argExpr) {
        String errMsg = null;
        if (argExpr instanceof BacktickExpr) {
            errMsg = getNodeLocationStr(location) +
                    "xml/json template expression is not allowed here";

        } else if (argExpr instanceof ActionInvocationExpr) {
            errMsg = getNodeLocationStr(location) +
                    "action invocation is not allowed here";

        } else if (argExpr instanceof ArrayInitExpr) {
            errMsg = getNodeLocationStr(location) +
                    "array initializer is not allowed here";

        } else if (argExpr instanceof ConnectorInitExpr) {
            errMsg = getNodeLocationStr(location) +
                    "connector initializer is not allowed here";

        } else if (argExpr instanceof RefTypeInitExpr) {
            errMsg = getNodeLocationStr(location) +
                    "reference type initializer is not allowed here";
        }

        if (errMsg != null) {
            errorMsgs.add(errMsg);
        }
    }


    /**
     * This class represents CallableUnitName used in function and action invocation expressions.
     */
    private static class CallableUnitName {
        String pkgName;

        // This used in function/action invocation expressions
        String name;

        CallableUnitName(String pkgName, String name) {
            this.name = name;
            this.pkgName = pkgName;
        }
    }


}
