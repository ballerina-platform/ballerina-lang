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

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

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
     * Expected type or inherited type
     */
    private BType expType;

    private String errMsgKey;
    private BType resultType;


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

    public BType checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, symTable.noType);
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType) {
        return checkExpr(expr, env, expType, "incompatible.types");
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, String errMsgKey) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        String preErrMsgKey = this.errMsgKey;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.errMsgKey = errMsgKey;
        expr.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.errMsgKey = preErrMsgKey;

        return resultType;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        resultType = checkType(literalExpr,
                symTable.getTypeFromTag(literalExpr.typeTag),
                expType, errMsgKey);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        Name varName = names.fromIdNode(varRefExpr.variableName);
        BSymbol symbol = symResolver.lookupSymbol(env.scope, varName, SymTag.VARIABLE);

        if (symbol == symTable.notFoundSymbol) {
            dlog.error(varRefExpr.pos, "undefined.symbol", varName.toString());
            varRefExpr.type = symTable.errType;
            return;
        }

        BVarSymbol varSym = (BVarSymbol) symbol;
        checkSefReferences(varRefExpr.pos, env, varSym);

        // Check type compatibility
        resultType = checkType(varRefExpr, varSym.type, expType);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env);

        // Look up operator symbol
        BSymbol symbol = symResolver.resolveBinaryOperator(binaryExpr.pos,
                binaryExpr.opKind, lhsType, rhsType);
        if (symbol == symTable.notFoundSymbol) {
            binaryExpr.type = symTable.errType;
            return;
        }

        // type check return type with the exp type
        BOperatorSymbol opSymbol = (BOperatorSymbol) symbol;
        resultType = checkType(binaryExpr, opSymbol.type.getReturnTypes().get(0), expType);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }


    // Private methods

    private BType checkType(BLangNode node, BType type, BType expType) {
        return checkType(node, type, expType, "incompatible.types");
    }

    private BType checkType(BLangNode node, BType type, BType expType, String errKey) {
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
        dlog.error(node.pos, errKey, expType, type);
        return node.type = symTable.errType;
    }

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, "self.reference", varSymbol.name);
        }
    }
}
