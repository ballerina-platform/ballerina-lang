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
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDcl;
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
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.ReferenceExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.StructInitExpr;
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
import org.wso2.ballerina.core.model.types.BStructType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
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
    private Queue<SimpleTypeName> typeNameQueue = new LinkedList<>();

    private Queue<BType> typeQueue = new LinkedList<>();
    private Stack<String> pkgPathStack = new Stack<>();
    private Stack<CallableUnitName> callableUnitNameStack = new Stack<>();
    private Stack<SymbolName> symbolNameStack = new Stack<>();

    private Stack<Expression> exprStack = new Stack<>();
    private Stack<KeyValueExpression> keyValueStack = new Stack<>();

    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    private Stack<List<Expression>> exprListStack = new Stack<>();
    private Stack<List<Annotation>> annotationListStack = new Stack<>();
    private Stack<List<KeyValueExpression>> mapInitKeyValueListStack = new Stack<>();

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    private Map<String, ImportPackage> importPkgMap = new HashMap<>();

    private List<String> errorMessageList = new ArrayList<>();

    public BLangModelBuilder() {
    }

    public BLangModelBuilder(SymbolScope packageScope) {
        this.currentScope = packageScope;
    }

    public BallerinaFile build() {
        if (!errorMessageList.isEmpty()) {
            BallerinaException e = new BallerinaException(
                    errorMessageList.toArray(new String[errorMessageList.size()]));
            throw e;
        }

        importPkgMap.values()
                .stream()
                .filter(importPkg -> !importPkg.isUsed())
                .findFirst()
                .ifPresent(importPkg -> {
                    NodeLocation location = importPkg.getNodeLocation();
                    String pkgPathStr = "\"" + importPkg.getPath() + "\"";
                    String importPkgErrStr = (importPkg.getAsName() == null) ? pkgPathStr : pkgPathStr + " as '" +
                            importPkg.getAsName() + "'";

                    throw new BallerinaException(location.getFileName() + ":" + location.getLineNumber() +
                            ": unused import package " + importPkgErrStr + "");
                });


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
            String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                    ": '" + importPkg.getName() + "' redeclared as imported package name";
            // throw new SemanticException(errMsg);
            errorMessageList.add(errMsg);
            return;
        }

        bFileBuilder.addImportPackage(importPkg);
        importPkgMap.put(importPkg.getName(), importPkg);
    }


    // Add types. SimpleTypes, Types with full scheme, schema URL or schema ID

    public void addSimpleTypeName(NodeLocation location, String name, String pkgName, boolean isArrayType) {
        SimpleTypeName typeName;
        if (pkgName != null) {
            ImportPackage importPkg = importPkgMap.get(pkgName);
            if (importPkg == null) {
                String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                        ": undefined package name '" + pkgName + "' in '" + pkgName + ":" + name + "'";
                errorMessageList.add(errMsg);
                // throw new SemanticException(errMsg);
                return;
            }

            importPkg.markUsed();
            typeName = new SimpleTypeName(name, pkgName, importPkg.getPath());
        } else {
            typeName = new SimpleTypeName(name);
        }


        typeName.setArrayType(isArrayType);
        typeNameQueue.add(typeName);
    }


    // Add constant definitions;

    public void addConstantDef(NodeLocation location, String name, boolean isPublic) {
        SymbolName symbolName = new SymbolName(name, currentPackagePath);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                    ": redeclared constant '" + name + "'";
            errorMessageList.add(errMsg);
            //throw new BallerinaException(errMsg);
            return;
        }

        SimpleTypeName typeName = typeNameQueue.remove();
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
    public void startStructDef() {
        currentStructBuilder = new StructDef.StructBuilder(currentScope);
        currentScope = currentStructBuilder;
    }

    /**
     * Add an field of the {@link StructDef}.
     *
     * @param location  Location of the field in the source file
     * @param fieldName Name of the field in the {@link StructDef}
     */
    public void addStructField(NodeLocation location, String fieldName) {
        SymbolName symbolName = new SymbolName(fieldName, currentPackagePath);

        SimpleTypeName typeName = typeNameQueue.remove();
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
     * @param isPublic Flag indicating whether the {@link StructDef} is public
     */
    public void addStructDef(NodeLocation location, String name, boolean isPublic) {
        currentStructBuilder.setNodeLocation(location);
        currentStructBuilder.setName(name);
        currentStructBuilder.setPackagePath(currentPackagePath);
        currentStructBuilder.setPublic(isPublic);
        StructDef structDef = currentStructBuilder.build();

        // Close Struct scope
        currentScope = currentStructBuilder.getEnclosingScope();
        currentStructBuilder = null;

        // Define StructDef Symbol in the package scope..
        SymbolName symbolName = new SymbolName(name, currentPackagePath);
        currentScope.define(symbolName, structDef);
        bFileBuilder.addStruct(structDef);
    }


    // Identifiers

    public void createSymbolName(String name) {
        if (pkgPathStack.isEmpty()) {
            symbolNameStack.push(new SymbolName(name));
        } else {
            symbolNameStack.push(new SymbolName(name, pkgPathStack.pop()));
        }
    }

    public void createSymbolName(String connectorName, String actionName) {
        SymbolName symbolName;
        if (pkgPathStack.isEmpty()) {
            symbolName = new SymbolName(actionName);
        } else {
            symbolName = new SymbolName(actionName, pkgPathStack.pop());
        }

        symbolName.setConnectorName(connectorName);
        symbolNameStack.push(symbolName);
    }


    // Annotations

    public void createInstanceCreaterExpr(String typeName, boolean exprListAvailable, NodeLocation location) {
        BType type = BTypes.getType(typeName);
        if (type == null || type instanceof BStructType) {
            // if the type is undefined or of struct type, treat it as a user defined struct
            createStructInitExpr(location, typeName);
            return;
        }

        if (exprListAvailable) {
            // This is not yet supported. Therefore ignoring for the moment.
            exprListStack.pop();
        }

        InstanceCreationExpr expression = new InstanceCreationExpr(location, null);
        expression.setType(type);
        exprStack.push(expression);
    }

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
            if (expr instanceof BasicLiteral && expr.getType() == BTypes.typeString) {
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


    // Function parameters

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
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                    ": redeclared parameter '" + paramName + "'";
            errorMessageList.add(errMsg);
            //throw new BallerinaException(errMsg);
            return;
        }


        SimpleTypeName typeName = typeNameQueue.remove();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);

        if (currentCUBuilder != null) {
            // Add the parameter to callableUnitBuilder.
            currentCUBuilder.addParameter(paramDef);
        } else {
            currentCUGroupBuilder.addParameter(paramDef);
        }

        currentScope.define(symbolName, paramDef);
    }

    public void registerConnectorType(String typeName) {
        //TODO: We might have to do this through a symbol table in the future
        //BTypes.addConnectorType(typeName);
    }

    public void createReturnTypes(NodeLocation location) {
        while (!typeNameQueue.isEmpty()) {
            SimpleTypeName typeName = typeNameQueue.remove();
            ParameterDef paramDef = new ParameterDef(location, null, typeName, null, currentScope);
            currentCUBuilder.addReturnParameter(paramDef);
        }
    }

    public void createNamedReturnParams(String paramName, NodeLocation location) {
        SymbolName symbolName = new SymbolName(paramName);

        // Check whether this constant is already defined.
        if (currentScope.resolve(symbolName) != null) {
            String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                    ": redeclared parameter '" + paramName + "'";
            errorMessageList.add(errMsg);
            //throw new BallerinaException(errMsg);
            return;
        }

        SimpleTypeName typeName = typeNameQueue.remove();
        ParameterDef paramDef = new ParameterDef(location, paramName, typeName, symbolName, currentScope);
        currentCUBuilder.addReturnParameter(paramDef);

        currentScope.define(symbolName, paramDef);
    }


    // Constants, Variable definitions, reference expressions

    public void createVariableDcl(String varName, NodeLocation location) {
        // Create a variable declaration
//        SymbolName localVarId = new SymbolName(varName);
//        BType localVarType = typeQueue.remove();

//        VariableDef variableDef = new VariableDef(location, localVarType, localVarId);

        // Add this variable declaration to the current callable unit or callable unit group
//        if (currentCUBuilder != null) {
        // This connector declaration should added to the relevant function/action or resource
//            currentCUBuilder.addVariableDcl(variableDcl);
//        } else {
//            currentCUGroupBuilder.addVariableDcl(variableDef);
//        }

    }

    public void createConnectorDcl(String varName, NodeLocation location) {
        // Here we build the object model for the following line

        // Here we need to pop the symbolName stack twice as the connector name appears twice in the declaration.
        if (symbolNameStack.size() < 2) {
            IllegalStateException ex = new IllegalStateException("symbol stack size should be " +
                    "greater than or equal to two");
            throw new ParserException("Failed to parse connector declaration" + varName + " in " +
                    location.getFileName() + ":" + location.getLineNumber(), ex);
        }

        symbolNameStack.pop();
        SymbolName cSymName = symbolNameStack.pop();
        List<Expression> exprList = exprListStack.pop();

        ConnectorDcl.ConnectorDclBuilder builder = new ConnectorDcl.ConnectorDclBuilder();
        builder.setConnectorName(cSymName);
        builder.setVarName(new SymbolName(varName));
        builder.setExprList(exprList);
        builder.setNodeLocation(location);

        ConnectorDcl connectorDcl = builder.build();
        if (currentCUBuilder != null) {
            // This connector declaration should added to the relevant function/action or resource
//            currentCUBuilder.addConnectorDcl(connectorDcl);
        } else {
            currentCUGroupBuilder.addConnectorDcl(connectorDcl);
        }
    }

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
    public void createVarRefExpr(String varName, NodeLocation location) {
        SymbolName symName = new SymbolName(varName);
        VariableRefExpr variableRefExpr = new VariableRefExpr(location, symName);
        exprStack.push(variableRefExpr);
    }

    public void createMapArrayVarRefExpr(String varName, NodeLocation location) {
        SymbolName symName = new SymbolName(varName);

        Expression indexExpr = exprStack.pop();
        VariableRefExpr arrayVarRefExpr = new VariableRefExpr(location, symName);

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(symName);
        builder.setIndexExpr(indexExpr);
        builder.setArrayMapVarRefExpr(arrayVarRefExpr);
        builder.setNodeLocation(location);

        ArrayMapAccessExpr accessExpr = builder.build();
        exprStack.push(accessExpr);
    }


    // Expressions

    public void createBinaryExpr(String opStr, NodeLocation location) {
        Expression rExpr = exprStack.pop();
        Expression lExpr = exprStack.pop();

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

            default:
                throw new ParserException("Unsupported operator '" + opStr + "' in " +
                        location.getFileName() + ":" + location.getLineNumber());
        }

        exprStack.push(expr);
    }

    public void createUnaryExpr(String op, NodeLocation location) {
        Expression rExpr = exprStack.pop();

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
                throw new ParserException("Unsupported operator '" + op + "' in " +
                        location.getFileName() + ":" + location.getLineNumber());
        }

        exprStack.push(expr);
    }

    public void createBacktickExpr(String stringContent, NodeLocation location) {
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
        cIExprBuilder.setExpressionList(exprListStack.pop());

        CallableUnitName callableUnitName = callableUnitNameStack.pop();

        if (callableUnitName.pkgName != null) {
            ImportPackage importPkg = importPkgMap.get(callableUnitName.pkgName);
            if (importPkg == null) {
                String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                        ": undefined package name '" + callableUnitName.pkgName + "' in '" +
                        callableUnitName.pkgName + ":" + callableUnitName.name + "'";
                errorMessageList.add(errMsg);
                // throw new SemanticException(errMsg);
                return;
            }

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
        cIExprBuilder.setExpressionList(exprListStack.pop());

        CallableUnitName callableUnitName = callableUnitNameStack.pop();

        if (callableUnitName.pkgName != null) {
            ImportPackage importPkg = importPkgMap.get(callableUnitName.pkgName);
            if (importPkg == null) {
                String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                        ": undefined package name '" + callableUnitName.pkgName + "' in '" +
                        callableUnitName.pkgName + ":" + callableUnitName.name + "." + actionName + "'";
                errorMessageList.add(errMsg);
                //throw new SemanticException(errMsg);
                return;
            }

            importPkg.markUsed();
            cIExprBuilder.setPkgPath(importPkg.getPath());
        }

        cIExprBuilder.setName(actionName);
        cIExprBuilder.setPkgName(callableUnitName.pkgName);
        cIExprBuilder.setConnectorName(callableUnitName.name);

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        exprStack.push(invocationExpr);
    }

    public void createTypeCastExpr(String targetTypeName, NodeLocation location) {
        TypeCastExpression typeCastExpression = new TypeCastExpression(location, exprStack.pop(),
                BTypes.getType(targetTypeName));
        //Remove the type added to type queue
        typeQueue.remove();
        exprStack.push(typeCastExpression);
    }

    public void createArrayInitExpr(NodeLocation location) {
        List<Expression> argList;
        if (!exprListStack.isEmpty()) {
            argList = exprListStack.pop();
        } else {
            argList = new ArrayList<>(0);
        }

        ArrayInitExpr arrayInitExpr = new ArrayInitExpr(location, argList.toArray(new Expression[argList.size()]));
        exprStack.push(arrayInitExpr);
    }

    public void createMapInitExpr(NodeLocation location) {
        List<KeyValueExpression> argList;
        if (!mapInitKeyValueListStack.isEmpty()) {
            argList = mapInitKeyValueListStack.pop();
        } else {
            argList = new ArrayList<>(0);
        }

        MapInitExpr mapInitExpr = new MapInitExpr(location, argList.toArray(new Expression[argList.size()]));
        exprStack.push(mapInitExpr);
    }

    public void startMapInitKeyValue() {
        mapInitKeyValueListStack.push(new ArrayList<>());
    }

    public void endMapInitKeyValue(int exprCount) {
        List<KeyValueExpression> keyValueList = mapInitKeyValueListStack.peek();
        addKeyValueToList(keyValueList, exprCount);
    }

    public void createMapInitKeyValue(String key, NodeLocation location) {
        if (!exprStack.isEmpty()) {
            Expression currentExpression = exprStack.pop();
            keyValueStack.push(new KeyValueExpression(location, key, currentExpression));
        } else {
            keyValueStack.push(new KeyValueExpression(location, key, null));
        }


    }

    public void addCallableUnitName(String pkgName, String name) {
        CallableUnitName callableUnitName = new CallableUnitName(pkgName, name);
        callableUnitNameStack.push(callableUnitName);
    }


    // Functions, Actions and Resources

    public void startCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder;
    }

    public void endCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();
        currentCUBuilder.setBody(blockStmt);
        currentScope = blockStmtBuilder.getEnclosingScope();
    }

    public void startCallableUnit() {
        currentCUBuilder = new CallableUnitBuilder(currentScope);
        currentScope = currentCUBuilder;
        annotationListStack.push(new ArrayList<>());
    }

    public void addFunction(NodeLocation location, String name, boolean isPublic) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setPublic(isPublic);

        List<Annotation> annotationList = annotationListStack.pop();
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaFunction function = currentCUBuilder.buildFunction();
        bFileBuilder.addFunction(function);

        // Define function is delayed due to missing type info of Parameters.

        currentScope = currentCUBuilder.getEnclosingScope();
        currentCUBuilder = null;
    }

    public void addTypeConverter(NodeLocation location, String name, boolean isPublic) {
        currentCUBuilder.setNodeLocation(location);
        currentCUBuilder.setName(name);
        currentCUBuilder.setPkgPath(currentPackagePath);
        currentCUBuilder.setPublic(isPublic);

        BTypeConvertor typeConvertor = currentCUBuilder.buildTypeConverter();
        bFileBuilder.addTypeConverter(typeConvertor);

        // Define type converter is delayed due to missing type info of Parameters.

        currentScope = currentCUBuilder.getEnclosingScope();
        currentCUBuilder = null;
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

        currentScope = currentCUBuilder.getEnclosingScope();
        currentCUBuilder = null;
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

        currentScope = currentCUBuilder.getEnclosingScope();
        currentCUBuilder = null;
    }


    // Services and Connectors

    public void startCallableUnitGroup() {
        currentCUGroupBuilder = new CallableUnitGroupBuilder(currentScope);
        currentScope = currentCUGroupBuilder;
        annotationListStack.push(new ArrayList<>());
    }

    public void createService(NodeLocation location, String name) {
        currentCUGroupBuilder.setNodeLocation(location);
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        Service service = currentCUGroupBuilder.buildService();
        bFileBuilder.addService(service);

        currentCUGroupBuilder = null;
    }

    public void createConnector(NodeLocation location, String name) {
        currentCUGroupBuilder.setNodeLocation(location);
        currentCUGroupBuilder.setName(name);
        currentCUGroupBuilder.setPkgPath(currentPackagePath);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        BallerinaConnector connector = currentCUGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

        currentCUGroupBuilder = null;
    }

    // Statements

    public void addVariableDefinitionStmt(NodeLocation location, String varName, boolean exprAvailable) {
        SimpleTypeName typeName = typeNameQueue.remove();
        SymbolName symbolName = new SymbolName(varName);

        VariableDef variableDef = new VariableDef(location, varName, typeName, symbolName, currentScope);
        currentScope.define(symbolName, variableDef);

        Expression rhsExpr = exprAvailable ? exprStack.pop() : null;
        VariableDefStmt variableDefStmt = new VariableDefStmt(location, variableDef, rhsExpr);
        addToBlockStmt(variableDefStmt);
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
            exprs = exprList.toArray(new Expression[exprList.size()]);
        } else {
            exprs = new Expression[0];
        }

        ReturnStmt returnStmt = new ReturnStmt(location, exprs);
        addToBlockStmt(returnStmt);
    }

    public void createReplyStmt(NodeLocation location) {
        ReplyStmt replyStmt = new ReplyStmt(location, exprStack.pop());
        addToBlockStmt(replyStmt);
    }

    public void startWhileStmt() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);
        currentScope = blockStmtBuilder;
    }

    public void addWhileStmt(NodeLocation location) {
        // Create a while statement builder
        WhileStmt.WhileStmtBuilder whileStmtBuilder = new WhileStmt.WhileStmtBuilder();
        whileStmtBuilder.setNodeLocation(location);

        // Get the expression at the top of the expression stack and set it as the while condition
        whileStmtBuilder.setCondition(exprStack.pop());

        // Get the statement block at the top of the block statement stack and set as the while body.
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        whileStmtBuilder.setWhileBody(blockStmtBuilder.build());

        // Close the current scope and open the enclosing scope
        currentScope = blockStmtBuilder.getEnclosingScope();

        // Add the while statement to the statement block which is at the top of the stack.
        WhileStmt whileStmt = whileStmtBuilder.build();
        blockStmtBuilderStack.peek().addStmt(whileStmt);
    }

    public void startIfElseStmt(NodeLocation location) {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = new IfElseStmt.IfElseStmtBuilder();
        ifElseStmtBuilder.setNodeLocation(location);
        ifElseStmtBuilderStack.push(ifElseStmtBuilder);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(currentScope);
        blockStmtBuilder.setNodeLocation(location);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder;
    }

    public void startElseIfClause(NodeLocation location) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(currentScope);
        blockStmtBuilder.setNodeLocation(location);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder;
    }

    public void addElseIfClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseIfStmtBlock = blockStmtBuilder.build();
        ifElseStmtBuilder.addElseIfBlock(elseIfStmtBlock.getNodeLocation(), exprStack.pop(), elseIfStmtBlock);

        currentScope = blockStmtBuilder.getEnclosingScope();
    }

    public void startElseClause() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(currentScope);
        blockStmtBuilderStack.push(blockStmtBuilder);

        currentScope = blockStmtBuilder;
    }

    public void addElseClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt elseStmt = blockStmtBuilder.build();
        ifElseStmtBuilder.setElseBody(elseStmt);

        currentScope = blockStmtBuilder.getEnclosingScope();
    }

    public void addIfElseStmt() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.pop();
        ifElseStmtBuilder.setIfCondition(exprStack.pop());

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        ifElseStmtBuilder.setThenBody(blockStmtBuilder.build());

        IfElseStmt ifElseStmt = ifElseStmtBuilder.build();
        addToBlockStmt(ifElseStmt);

        currentScope = blockStmtBuilder.getEnclosingScope();
    }

    public void createFunctionInvocationStmt(NodeLocation location) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setNodeLocation(location);
        cIExprBuilder.setExpressionList(exprListStack.pop());

        CallableUnitName callableUnitName = callableUnitNameStack.pop();

        if (callableUnitName.pkgName != null) {
            ImportPackage importPkg = importPkgMap.get(callableUnitName.pkgName);
            if (importPkg == null) {
                String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                        ": undefined package name '" + callableUnitName.pkgName + "' in '" +
                        callableUnitName.pkgName + ":" + callableUnitName.name + "'";
                errorMessageList.add(errMsg);
                //throw new SemanticException(errMsg);
                return;
            }

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
        cIExprBuilder.setExpressionList(exprListStack.pop());

        CallableUnitName callableUnitName = callableUnitNameStack.pop();

        if (callableUnitName.pkgName != null) {
            ImportPackage importPkg = importPkgMap.get(callableUnitName.pkgName);
            if (importPkg == null) {
                String errMsg = location.getFileName() + ":" + location.getLineNumber() +
                        ": undefined package name '" + callableUnitName.pkgName + "' in '" +
                        callableUnitName.pkgName + ":" + callableUnitName.name + "'";
                errorMessageList.add(errMsg);
                // throw new SemanticException(errMsg);
                return;
            }

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
        createLiteral(bValue, BTypes.typeInt, location);
    }

    public void createLongLiteral(String value, NodeLocation location) {
        BValueType bValue = new BLong(Long.parseLong(value));
        createLiteral(bValue, BTypes.typeLong, location);
    }

    public void createFloatLiteral(String value, NodeLocation location) {
        BValueType bValue = new BFloat(Float.parseFloat(value));
        createLiteral(bValue, BTypes.typeFloat, location);
    }

    public void createDoubleLiteral(String value, NodeLocation location) {
        BValueType bValue = new BDouble(Double.parseDouble(value));
        createLiteral(bValue, BTypes.typeDouble, location);
    }

    public void createStringLiteral(String value, NodeLocation location) {
        BValueType bValue = new BString(value);
        createLiteral(bValue, BTypes.typeString, location);
    }

    public void createBooleanLiteral(String value, NodeLocation location) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(bValue, BTypes.typeBoolean, location);
    }

    public void createNullLiteral(String value, NodeLocation location) {
        throw new RuntimeException("Null values are not yet supported in Ballerina in " + location.getFileName()
                + ":" + location.getLineNumber());
    }

    // Private methods

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }

    private void createLiteral(BValueType bValueType, BType type, NodeLocation location) {
        BasicLiteral basicLiteral = new BasicLiteral(location, bValueType);
        basicLiteral.setType(type);
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
    private void addKeyValueToList(List<KeyValueExpression> keyValueDataHolderList, int n) {

        if (keyValueStack.isEmpty()) {
            throw new IllegalStateException("KeyValue stack cannot be empty in processing a KeyValueList");
        }

        if (n == 1) {
            KeyValueExpression keyValue = keyValueStack.pop();
            keyValueDataHolderList.add(keyValue);
        } else {
            KeyValueExpression keyValue = keyValueStack.pop();
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
     * Create a Struct initializing expression
     *
     * @param location   Location of the initialization in the source bal file
     * @param structName Name of the struct type
     */
    public void createStructInitExpr(NodeLocation location, String structName) {
        // Create the Struct declaration
        SymbolName structSymName = new SymbolName(structName);
        StructDcl structDcl = new StructDcl(location, structSymName);

        // Create the RHS of the expression
        StructInitExpr structInitExpr = new StructInitExpr(location, structDcl);
        // structInitExpr.setType(new BStructType(structName));
        exprStack.push(structInitExpr);
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
        StructFieldAccessExpr parentExpr = new StructFieldAccessExpr(location, parent.getSymbolName(), parent);

        parentExpr.setFieldExpr(fieldExpr);
        fieldExpr.setParent(parentExpr);
        exprStack.push(parentExpr);
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
