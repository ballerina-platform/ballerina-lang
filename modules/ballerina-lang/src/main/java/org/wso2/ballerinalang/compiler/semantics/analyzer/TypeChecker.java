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

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
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

    public List<BType> checkExprs(List<BLangExpression> exprs, SymbolEnv env, List<BType> expTypes) {
        List<BType> resTypes = new ArrayList<>(exprs.size());
        for (BLangExpression expr : exprs) {
            resTypes.add(checkExpr(expr, env, expTypes).get(0));
        }
        return resTypes;
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes, DiagnosticCode diagCode) {
        checkAssignmentMismatch(expr, expTypes);

        SymbolEnv prevEnv = this.env;
        List<BType> preExpTypes = this.expTypes;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        // Set the default type of expressions
        this.resultTypes = Lists.of(symTable.errType);
        this.env = env;
        this.expTypes = expTypes;
        this.diagCode = diagCode;
        expr.accept(this);
        this.env = prevEnv;
        this.expTypes = preExpTypes;
        this.diagCode = preDiagCode;

        assignExprType(expr);
        return resultTypes;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        resultTypes = Lists.of(checkType(literalExpr,
                symTable.getTypeFromTag(literalExpr.typeTag),
                expTypes.get(0), diagCode));
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BArrayType expArrayType;
        if (expTypes.get(0).tag == TypeTags.NONE) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.ARRAY_LITERAL_NOT_ALLOWED);
            return;
        } else if (expTypes.get(0).tag != TypeTags.ARRAY) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expTypes.get(0));
            return;
        } else {
            expArrayType = (BArrayType) expTypes.get(0);
        }

        BType expElemType = expArrayType.eType;
        checkExprs(arrayLiteral.exprs, this.env, Lists.of(expElemType));
        resultTypes = Lists.of(expArrayType);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        Name varName = names.fromIdNode(varRefExpr.variableName);
        BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);

        if (symbol == symTable.notFoundSymbol) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            return;
        }

        BVarSymbol varSym = (BVarSymbol) symbol;
        checkSefReferences(varRefExpr.pos, env, varSym);

        // Check type compatibility
        resultTypes = Lists.of(checkType(varRefExpr, varSym.type, expTypes.get(0)));
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation invocationExpr) {

    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env).get(0);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env).get(0);

        // Look up operator symbol
        BSymbol symbol = symResolver.resolveBinaryOperator(binaryExpr.pos,
                binaryExpr.opKind, lhsType, rhsType);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(binaryExpr.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                    binaryExpr.opKind, lhsType, rhsType);
            return;
        }

        // type check return type with the exp type
        BOperatorSymbol opSymbol = (BOperatorSymbol) symbol;
        resultTypes = Lists.of(checkType(binaryExpr, opSymbol.type.getReturnTypes().get(0), expTypes.get(0)));
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeCastExpr castExpr) {
        BType targetType = symResolver.resolveTypeNode(castExpr.typeNode, env);
        BType sourceType = checkExpr(castExpr.expr, env, Lists.of(symTable.noType)).get(0);

        // Lookup type explicit cast operator symbol
        BSymbol symbol = symResolver.resolveExplicitCastOperator(castExpr.pos, sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            return;
        }

        BCastOperatorSymbol castSym = (BCastOperatorSymbol) symbol;
        // If this cast is an unsafe cast, then there MUST to be two l variables
        // resulting two expected types
        // If this is an safe cast, then the error variable is optional
        if (!castSym.safe && expTypes.size() < 2) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CAST_ATTEMPT, sourceType, targetType);
            return;
        } else if (castSym.safe && expTypes.size() < 2) {
            // CastSym always contain two return parameters.
            expTypes.add(symTable.errStructType);
        }

        resultTypes = checkTypes(castExpr, castSym.type.getReturnTypes(), expTypes);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }


    // Private methods

    private List<BType> checkTypes(BLangExpression node, List<BType> types, List<BType> expTypes) {
        checkAssignmentMismatch(node, expTypes);
        if (types.size() != expTypes.size()) {
            dlog.error(node.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expTypes.size(), types.size());
            // Fill the result type with the error type
            resultTypes = new ArrayList<>(expTypes.size());
            for (int i = 0; i < expTypes.size(); i++) {
                resultTypes.add(symTable.errType);
            }
        }

        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            resTypes.add(checkType(node, types.get(i), expTypes.get(i)));
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

    private void assignExprType(BLangExpression expr) {
        if (expr.isMultiReturnExpr()) {
            MultiReturnExpr multiReturnExpr = (MultiReturnExpr) expr;
            multiReturnExpr.setTypes(resultTypes);
        } else {
            expr.type = resultTypes.get(0);
        }
    }

    private void checkAssignmentMismatch(BLangExpression expr, List<BType> expTypes) {
        if (!expr.isMultiReturnExpr() && expTypes.size() > 1) {
            dlog.error(expr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expTypes.size(), 1);

            // Fill the result type with the error type
            resultTypes = new ArrayList<>(expTypes.size());
            for (int i = 0; i < expTypes.size(); i++) {
                resultTypes.add(symTable.errType);
            }
        }
    }
}
