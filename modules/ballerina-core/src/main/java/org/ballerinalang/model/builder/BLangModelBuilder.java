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

import org.ballerinalang.model.Annotation;
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
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.SemanticErrors;
import org.ballerinalang.util.exceptions.SemanticException;

import java.util.ArrayList;
import java.util.Collections;
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

    protected Stack<Annotation.AnnotationBuilder> annotationBuilderStack = new Stack<>();
    protected Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    protected Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    protected Stack<TryCatchStmt.TryCatchStmtBuilder> tryCatchStmtBuilderStack = new Stack<>();

    protected Stack<ForkJoinStmt.ForkJoinStmtBuilder> forkJoinStmtBuilderStack = new Stack<>();
    protected Stack<List<Worker>> workerStack = new Stack<>();

    protected Stack<SimpleTypeName> typeNameStack = new Stack<>();
    protected Stack<CallableUnitName> callableUnitNameStack = new Stack<>();
    protected Stack<Expression> exprStack = new Stack<>();

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    protected Stack<List<Expression>> exprListStack = new Stack<>();
    protected Stack<List<Annotation>> annotationListStack = new Stack<>();
    protected Stack<MapStructInitKeyValueExpr> mapStructInitKVStack = new Stack<>();
    protected Stack<List<MapStructInitKeyValueExpr>> mapStructInitKVListStack = new Stack<>();

    // This variable keeps the package scope so that workers (and any global things) can be added to package scope
    protected SymbolScope packageScope = null;

    // This variable keeps the fork-join scope when adding workers and resolve back to current scope once done
    protected SymbolScope forkJoinScope = null;

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    protected Map<String, ImportPackage> importPkgMap = new HashMap<>();

    protected List<String> errorMsgs = new ArrayList<>();

    public BLangModelBuilder(BLangPackage.PackageBuilder packageBuilder, String bFileName) {
        this.currentScope = packageBuilder.getCurrentScope();
        this.packageScope = currentScope;
        bFileBuilder = new BallerinaFile.BFileBuilder(bFileName, packageBuilder);

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
        SymbolName symbolName = new SymbolName(name);
        SimpleTypeName typeName = typeNameStack.pop();
        ConstDef constantDef = new ConstDef(location, name, typeName, currentPackagePath,
                isPublic, symbolName, currentScope, exprStack.pop());

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
        annotationListStack.push(new ArrayList<>());
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
            String errMsg = BLangExceptionHelper
                    .constructSemanticError(location, SemanticErrors.REDECLARED_SYMBOL, fieldName);
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
        
        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentStructBuilder::addAnnotation);

        // TODO: Fix the package path
//        currentStructBuilder.setPackagePath(currentPackagePath);
        StructDef structDef = currentStructBuilder.build();

        // Close Struct scope
        currentScope = structDef.getEnclosingScope();
        currentStructBuilder = null;

        // Define StructDef Symbol in the package scope..
        // TODO: add currentPackagePath when creating the SymbolName
//        SymbolName symbolName = new SymbolName(name);

//        currentScope.define(symbolName, structDef);
        bFileBuilder.addStruct(structDef);
    }


    // Annotations

    public void startAnnotation() {
        annotationBuilderStack.push(new Annotation.AnnotationBuilder());
    }

    public void createAnnotationKeyValue(String key) {
        Expression expr = exprStack.peek();
        if (expr instanceof BasicLiteral &&
                ((BasicLiteral) expr).getTypeName().getName().equals(TypeConstants.STRING_TNAME)) {
            String value = ((BasicLiteral) expr).getBValue().stringValue();
            Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.peek();
            annotationBuilder.addKeyValuePair(new SymbolName(key), value);
        }
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

    public void startTypeMapperInput() {
        annotationListStack.push(new ArrayList<>());
    }


    public void endTypeMapperInput() {
        annotationListStack.pop();
    }

    // Function/action input and out parameters
    public void startParamList() {
        annotationListStack.push(new ArrayList<>());
    }


    public void endParamList() {
        annotationListStack.pop();
    }

    // Function parameters and types

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
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        SimpleTypeName typeName = typeNameStack.pop();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);

        // Annotation list is maintained for each parameter
        if (!annotationListStack.isEmpty() && !annotationListStack.peek().isEmpty()) {
            annotationListStack.peek().forEach(paramDef::addAnnotation);
            // Clear all added annotations for the current parameter.
            annotationListStack.peek().clear();
        }

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
            String errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
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
     * 2) Map or arrays access a[1], m["key"]
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

    public void createMapStructInitKeyValue(NodeLocation location) {
        // TODO Validate key/value expressions. e.g. key can't be an arrays init expression.
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

    public void startWorkerUnit() {
        if (currentCUBuilder != null) {
            parentCUBuilder = currentCUBuilder;
        }
        currentCUBuilder = new Worker.WorkerBuilder(packageScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addFunction(NodeLocation location, String name, boolean isPublic,  boolean isNative) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPublic(isPublic);
        currentCUBuilder.setNative(isNative);
        
        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaFunction function = currentCUBuilder.buildFunction();
        bFileBuilder.addFunction(function);

        // Define function is delayed due to missing type info of Parameters.

        currentScope = function.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void startTypeMapperDef() {
        currentCUBuilder = new BTypeMapper.BTypeMapperBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addTypeMapper(String source, String target, String name,
            NodeLocation location, boolean isPublic, boolean isNative) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        //currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setPublic(isPublic);
        currentCUBuilder.setNative(isNative);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BTypeMapper typeMapper = currentCUBuilder.buildTypeMapper();

        bFileBuilder.addTypeMapper(typeMapper);

        // Define type mapper is delayed due to missing type info of Parameters.

        currentScope = typeMapper.getEnclosingScope();
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
        // TODO Figure out whether we need to support public type typemappers
//        currentCUBuilder.setPublic(isPublic);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        // Define resource is delayed due to missing type info of Parameters.

        currentScope = resource.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void createWorker(String name, NodeLocation sourceLocation) {
        currentCUBuilder.setName(name);
        currentCUBuilder.setNodeLocation(sourceLocation);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        Worker worker = currentCUBuilder.buildWorker();
        if (forkJoinStmtBuilderStack.isEmpty()) {
            parentCUBuilder.addWorker(worker);
        } else {
            workerStack.peek().add(worker);
            currentScope = forkJoinScope;
        }

        currentCUBuilder = parentCUBuilder;
        parentCUBuilder = null;
//        // Take the function body and set that as the CUBuilder body
//        if (!blockStmtBuilderStack.empty()) {
//            BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
//            BlockStmt blockStmt = blockStmtBuilder.build();
//            currentCUBuilder.setBody(blockStmt);
//        }
    }

    public void startActionDef() {
        if (currentScope instanceof BlockStmt) {
            endCallableUnitBody();
        }

        currentCUBuilder = new BallerinaAction.BallerinaActionBuilder(currentScope);
        currentScope = currentCUBuilder.getCurrentScope();
        annotationListStack.push(new ArrayList<>());
    }

    public void addAction(NodeLocation location, String name, boolean isNative) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setNative(isNative);
        
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

        currentScope = service.getEnclosingScope();
        currentCUGroupBuilder = null;
    }

    public void createConnector(NodeLocation location, String name, boolean isNative) {
        currentCUGroupBuilder.setNodeLocation(location);
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);
        currentCUGroupBuilder.setNative(isNative);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        BallerinaConnectorDef connector = currentCUGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

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

    public void addCatchClause(NodeLocation location, String argName) {
        TryCatchStmt.TryCatchStmtBuilder tryCatchStmtBuilder = tryCatchStmtBuilderStack.peek();

        SimpleTypeName exceptionType = typeNameStack.pop();
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

    public void endJoinClause(String paramName, NodeLocation location) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt forkJoinStmt = blockStmtBuilder.build();
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg =  BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        SimpleTypeName typeName = typeNameStack.pop();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
        forkJoinStmtBuilder.setJoinBlock(forkJoinStmt);
        forkJoinStmtBuilder.setJoinResult(paramDef);
        currentScope = forkJoinStmtBuilder.getJoin().getEnclosingScope();
    }

    public void createAnyJoinCondition(String joinType, String joinCount, NodeLocation location) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();

        forkJoinStmtBuilder.setJoinType(joinType);
        if (Integer.parseInt(joinCount) != 1) {
            String errMsg =  BLangExceptionHelper.constructSemanticError(location,
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

    public void endTimeoutClause(String paramName, NodeLocation location) {
        ForkJoinStmt.ForkJoinStmtBuilder forkJoinStmtBuilder = forkJoinStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt timeoutStmt = blockStmtBuilder.build();
        forkJoinStmtBuilder.setTimeoutBlock(timeoutStmt);
        forkJoinStmtBuilder.setTimeoutExpression(exprStack.pop());
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        BLangSymbol paramSymbol = currentScope.resolve(symbolName);
        if (paramSymbol != null && paramSymbol.getSymbolScope().getScopeName() == SymbolScope.ScopeName.LOCAL) {
            String errMsg =  BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REDECLARED_SYMBOL, paramName);
            errorMsgs.add(errMsg);
        }

        SimpleTypeName typeName = typeNameStack.pop();
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

    protected void startRefTypeInitExpr() {
        mapStructInitKVListStack.push(new ArrayList<>());
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

        } else if (argExpr instanceof ActionInvocationExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.ACTION_INVOCATION_NOT_ALLOWED_HERE);

        } else if (argExpr instanceof ArrayInitExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.ARRAY_INIT_NOT_ALLOWED_HERE);

        } else if (argExpr instanceof ConnectorInitExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.CONNECTOR_INIT_NOT_ALLOWED_HERE);

        } else if (argExpr instanceof RefTypeInitExpr) {
            errMsg = BLangExceptionHelper.constructSemanticError(location,
                    SemanticErrors.REF_TYPE_INTI_NOT_ALLOWED_HERE);
        }

        if (errMsg != null) {
            errorMsgs.add(errMsg);
        }
    }


    /**
     * This class represents CallableUnitName used in function and action invocation expressions.
     */
    protected static class CallableUnitName {
        String pkgName;

        // This used in function/action invocation expressions
        String name;

        CallableUnitName(String pkgName, String name) {
            this.name = name;
            this.pkgName = pkgName;
        }
    }
}
