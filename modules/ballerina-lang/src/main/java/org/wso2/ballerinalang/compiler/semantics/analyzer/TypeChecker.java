/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.MultiReturnExpr;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class TypeChecker extends BLangNodeVisitor {

    private static final CompilerContext.Key<TypeChecker> TYPE_CHECKER_KEY =
            new CompilerContext.Key<>();

    private Names names;
    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private DiagnosticLog dlog;

    private SymbolEnv env;

    /**
     * Expected types or inherited types
     */
    private List<BType> expTypes;

    private DiagnosticCode diagCode;
    private List<BType> resultTypes;


    public static TypeChecker getInstance(CompilerContext context) {
        TypeChecker typeChecker = context.get(TYPE_CHECKER_KEY);
        if (typeChecker == null) {
            typeChecker = new TypeChecker(context);
        }

        return typeChecker;
    }

    public TypeChecker(CompilerContext context) {
        context.put(TYPE_CHECKER_KEY, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, Lists.of(symTable.noType));
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes) {
        return checkExpr(expr, env, expTypes, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    /**
     * Check the given list of expressions against the given expected types.
     *
     * @param exprs   list of expressions to be analyzed
     * @param env     current symbol environment
     * @param expType expected type
     * @return the actual types of the given list of expressions
     */
    public List<BType> checkExprs(List<BLangExpression> exprs, SymbolEnv env, BType expType) {
        List<BType> resTypes = new ArrayList<>(exprs.size());
        for (BLangExpression expr : exprs) {
            resTypes.add(checkExpr(expr, env, Lists.of(expType)).get(0));
        }
        return resTypes;
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes, DiagnosticCode diagCode) {
        // TODO Check the possibility of using a try/finally here
        SymbolEnv prevEnv = this.env;
        List<BType> preExpTypes = this.expTypes;
        DiagnosticCode preDiagCode = this.diagCode;
        this.env = env;
        this.diagCode = diagCode;
        this.expTypes = verifyAndGetExpectedTypes(expr, expTypes);

        expr.accept(this);

        setExprType(expr, expTypes);
        this.env = prevEnv;
        this.expTypes = preExpTypes;
        this.diagCode = preDiagCode;
        return resultTypes;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        BType literalType = symTable.getTypeFromTag(literalExpr.typeTag);
        resultTypes = checkTypes(literalExpr, Lists.of(literalType), expTypes);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BType actualType = symTable.errType;

        int expTypeTag = expTypes.get(0).tag;
        if (expTypeTag == TypeTags.NONE) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.ARRAY_LITERAL_NOT_ALLOWED);

        } else if (expTypeTag != TypeTags.ARRAY && expTypeTag != TypeTags.ERROR) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expTypes.get(0));

        } else if (expTypeTag != TypeTags.ERROR) {
            BArrayType arrayType = (BArrayType) expTypes.get(0);
            checkExprs(arrayLiteral.exprs, this.env, arrayType.eType);
            actualType = new BArrayType(arrayType.eType);
        }

        resultTypes = checkTypes(arrayLiteral, Lists.of(actualType), expTypes);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        BType actualType = symTable.errType;
        int expTypeTag = expTypes.get(0).tag;
        if (expTypeTag == TypeTags.NONE) {
            // var a = {}
            // Change the expected type to map
            expTypes = Lists.of(symTable.mapType);
        }

//        if (expTypeTag == TypeTags.MAP ||
//                expTypeTag == TypeTags.JSON) {
//            recordLiteral.keyValuePairs.forEach(keyValuePair ->
//                    checkStructLiteralKeyValuePair(keyValuePair, expTypes.get(0)));
//            actualType = expTypes.get(0);
//        }

        if (expTypeTag == TypeTags.STRUCT) {
            recordLiteral.keyValuePairs.forEach(keyValuePair ->
                    checkStructLiteralKeyValuePair(keyValuePair, (BStructType) expTypes.get(0)));
            actualType = expTypes.get(0);
        }

        resultTypes = checkTypes(recordLiteral, Lists.of(actualType), expTypes);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        // Set error type as the actual type.
        BType actualType = symTable.errType;

        Name varName = names.fromIdNode(varRefExpr.variableName);
        BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
        } else {
            BVarSymbol varSym = (BVarSymbol) symbol;
            checkSefReferences(varRefExpr.pos, env, varSym);
            actualType = varSym.type;
        }

        // Check type compatibility
        resultTypes = checkTypes(varRefExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // TODO
        checkExpr(fieldAccessExpr.expr, this.env, Lists.of(symTable.noType));
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // TODO
        // First analyze the variable reference expression.
        checkExpr(indexAccessExpr.expr, this.env, Lists.of(symTable.noType));

        // Validate the index expression
        // Expected type == int, if the variable is an array.
        checkExpr(indexAccessExpr.indexExpr, this.env, Lists.of(symTable.intType));
    }

    public void visit(BLangInvocation iExpr) {
        // Variable ref expression null means this is the leaf node of the variable ref expression tree
        // e.g. foo();, foo(), foo().k;
        if (iExpr.expr == null) {
            // This is a function invocation expression. e.g. foo()
            checkFunctionInvocationExpr(iExpr);
            return;
        }

        // TODO other types of invocation expressions
        //TODO pkg alias should be null or empty here.

//        checkExpr(iExpr.expr, this.env, Lists.of(symTable.noType));
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env).get(0);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env).get(0);

        // Set error type as the actual type.
        BType actualType = symTable.errType;

        // Look up operator symbol if both rhs and lhs types are error types
        if (lhsType != symTable.errType && rhsType != symTable.errType) {
            BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(binaryExpr.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                        binaryExpr.opKind, lhsType, rhsType);
            } else {
                actualType = opSymbol.type.getReturnTypes().get(0);
            }
        }

        resultTypes = checkTypes(binaryExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeCastExpr castExpr) {
        // Set error type as the actual type.
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());

        BType targetType = symResolver.resolveTypeNode(castExpr.typeNode, env);
        BType sourceType = checkExpr(castExpr.expr, env, Lists.of(symTable.noType)).get(0);

        // Lookup type explicit cast operator symbol
        BSymbol symbol = symResolver.resolveExplicitCastOperator(sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(castExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CAST, sourceType, targetType);
        } else {
            BCastOperatorSymbol castSym = (BCastOperatorSymbol) symbol;
            actualTypes = getActualTypesOfCastExpr(castExpr, targetType, sourceType, castSym);
        }

        resultTypes = checkTypes(castExpr, actualTypes, expTypes);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }


    // Private methods

    private List<BType> checkTypes(BLangExpression node, List<BType> actualTypes, List<BType> expTypes) {
        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < actualTypes.size(); i++) {
            resTypes.add(checkType(node, actualTypes.get(i), expTypes.get(i)));
        }
        return resTypes;
    }

    private BType checkType(BLangExpression node, BType type, BType expType) {
        return checkType(node, type, expType, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    private BType checkType(BLangExpression node, BType type, BType expType, DiagnosticCode diagCode) {
        node.type = type;
        if (expType.tag == TypeTags.ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return type;
        } else if (type.tag == TypeTags.ERROR) {
            return type;
        } else if (type.tag == expType.tag) {
            return type;
        }

        // TODO Add more logic to check type compatibility assignability etc.

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(node.pos, diagCode, expType, type);
        return node.type = symTable.errType;
    }

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    private List<BType> verifyAndGetExpectedTypes(BLangExpression expr, List<BType> expTypes) {
        if (!expr.isMultiReturnExpr() && expTypes.size() > 1) {
            // This error will be reported after analyzing the expression
            return Lists.of(symTable.errType);
        }

        return expTypes;
    }

    private void setExprType(BLangExpression expr, List<BType> expTypes) {
        int expected = expTypes.size();
        int actual = resultTypes.size();
        if (expr.isMultiReturnExpr()) {
            MultiReturnExpr multiReturnExpr = (MultiReturnExpr) expr;
            multiReturnExpr.setTypes(resultTypes);
        } else {
            if (expected > 1) {
                dlog.error(expr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, actual);
                resultTypes = getListWithErrorTypes(expected);
            }
            expr.type = resultTypes.get(0);
        }
    }

    private List<BType> getListWithErrorTypes(int count) {
        List<BType> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(symTable.errType);
        }

        return list;
    }

    private List<BType> getActualTypesOfCastExpr(BLangTypeCastExpr castExpr,
                                                 BType targetType,
                                                 BType sourceType,
                                                 BCastOperatorSymbol castSymbol) {
        // If this cast is an unsafe cast, then there MUST to be two expected types/variables
        // If this is an safe cast, then the error variable is optional
        int expected = expTypes.size();
        List<BType> actualTypes = getListWithErrorTypes(expected);
        if (castSymbol.safe && expected == 1) {
            actualTypes = Lists.of(castSymbol.type.getReturnTypes().get(0));

        } else if (!castSymbol.safe && expected == 1) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CAST_ATTEMPT, sourceType, targetType);

        } else if (expected == 2) {
            actualTypes = castSymbol.type.getReturnTypes();

        } else if (expected == 0 || expected > 2) {
            dlog.error(castExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, 2);
        }

        return actualTypes;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        Name funcName = names.fromIdNode(iExpr.name);
        BSymbol funcSymbol = symResolver.resolveInvokable(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION,
                this.env, names.fromIdNode(iExpr.pkgAlias), funcName);
        if (funcSymbol == symTable.errSymbol) {
            resultTypes = actualTypes;
            return;
        }

        List<BType> paramTypes = ((BInvokableType) funcSymbol.type).getParameterTypes();
        if (iExpr.argExprs.size() == 1 && iExpr.argExprs.get(0).getKind() == NodeKind.INVOCATION) {
            checkExpr(iExpr.argExprs.get(0), this.env, paramTypes);

        } else if (paramTypes.size() > iExpr.argExprs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, funcName);

        } else if (paramTypes.size() < iExpr.argExprs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, funcName);

        } else {
            for (int i = 0; i < iExpr.argExprs.size(); i++) {
                checkExpr(iExpr.argExprs.get(i), this.env, Lists.of(paramTypes.get(i)));
            }
            actualTypes = funcSymbol.type.getReturnTypes();
        }

        checkInvocationReturnTypes(iExpr, actualTypes, funcName);
    }

    private void checkInvocationReturnTypes(BLangInvocation iExpr, List<BType> actualTypes, Name funcName) {
        int expected = expTypes.size();
        int actual = actualTypes.size();
        if (expected == 1 && actual > 1) {
            dlog.error(iExpr.pos, DiagnosticCode.MULTI_VAL_IN_SINGLE_VAL_CONTEXT, funcName);
            actualTypes = getListWithErrorTypes(expected);
        } else if (expected == 0) {
            // This could be from a expression statement. e.g foo();
            if (this.env.node.getKind() != NodeKind.EXPRESSION_STATEMENT) {
                dlog.error(iExpr.pos, DiagnosticCode.DOES_NOT_RETURN_VALUE, funcName);
            }
            actualTypes = new ArrayList<>(0);
        } else if (expected != actual) {
            // Special case actual == 0 scenario.. VOID Function
            dlog.error(iExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, actual);
            actualTypes = getListWithErrorTypes(expected);
        }

        resultTypes = checkTypes(iExpr, actualTypes, expTypes);
    }

    private void checkStructLiteralKeyValuePair(BLangRecordKeyValue keyValuePair, BStructType structType) {
        BLangExpression keyExpr = keyValuePair.keyExpr;
        Name fieldName;
        if (keyExpr.getKind() != NodeKind.LITERAL &&
                keyExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_FIELD_NAME_STRUCT_LITERAL);
            return;

        } else if (keyExpr.getKind() == NodeKind.LITERAL) {
            BType keyExprType = checkExpr(keyExpr, this.env, Lists.of(symTable.stringType)).get(0);
            if (keyExprType == symTable.errType) {
                return;
            }

            Object literalValue = ((BLangLiteral) keyExpr).value;
            fieldName = names.fromString((String) literalValue);

        } else {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        }

        // Check weather the struct field exists
        BSymbol symbol = symResolver.resolveStructField(keyExpr.pos, fieldName, structType.tsymbol);
        if (symbol == symTable.notFoundSymbol) {
            return;
        }

        BVarSymbol fieldSymbol = (BVarSymbol) symbol;
        BLangExpression valueExpr = keyValuePair.valueExpr;
        checkExpr(valueExpr, this.env, Lists.of(fieldSymbol.type));
    }
}
