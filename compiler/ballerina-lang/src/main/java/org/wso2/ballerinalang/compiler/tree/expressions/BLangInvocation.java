/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link org.ballerinalang.model.tree.expressions.InvocationNode}.
 *
 * @since 0.94
 */
public class BLangInvocation extends BLangAccessExpression implements InvocationNode {

    public BLangIdentifier pkgAlias;
    public BLangIdentifier name;
    public List<BLangExpression> argExprs = new ArrayList<>();
    //caching since at desugar level we need to identify whether this is actually attached function or not
    public BSymbol exprSymbol;
    public BSymbol symbol;
    public boolean functionPointerInvocation;
    /* Variables Required for Iterable Operation */
    public boolean iterableOperationInvocation;
    public IterableContext iContext;
    public boolean actionInvocation;
    public boolean async;

    /*
     * Below expressions are used by typechecker, desugar and codegen phases.
     * Above phases must rely on below expr lists, rather than {@link #argExprs}
     */
    public List<BLangExpression> requiredArgs = new ArrayList<>();
    public List<BLangExpression> namedArgs = new ArrayList<>();
    public List<BLangExpression> restArgs = new ArrayList<>();

    @Override
    public IdentifierNode getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public IdentifierNode getName() {
        return name;
    }

    @Override
    public List<? extends ExpressionNode> getArgumentExpressions() {
        return argExprs;
    }

    @Override
    public BLangVariableReference getExpression() {
        return expr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.INVOCATION;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        if (expr != null) {
            // Action invocation or lambda invocation.
            br.append(String.valueOf(expr)).append(".");
        } else if (pkgAlias != null && !pkgAlias.getValue().isEmpty()) {
            br.append(String.valueOf(pkgAlias)).append(":");
        }
        br.append(String.valueOf(name));
        br.append("(");
        if (argExprs.size() > 0) {
            String s = Arrays.toString(argExprs.toArray());
            br.append(s.substring(1, s.length() - 1));
        }
        br.append(")");
        return br.toString();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isIterableOperation() {
        return iterableOperationInvocation;
    }

    @Override
    public boolean isActionInvocation() {
        return this.actionInvocation;
    }
    
    @Override
    public boolean isAsync() {
        return async;
    }

    /**
     * @since 0.94
     */
    public static class BFunctionPointerInvocation extends BLangInvocation {

        public BFunctionPointerInvocation(BLangInvocation parent, BLangVariableReference varRef) {
            this.pos = parent.pos;
            this.name = parent.name;
            this.requiredArgs = parent.requiredArgs;
            this.namedArgs = parent.namedArgs;
            this.restArgs = parent.restArgs;
            this.regIndex = parent.regIndex;
            this.symbol = parent.symbol;
            this.expr = varRef;
            this.type = parent.type;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangAttachedFunctionInvocation extends BLangInvocation {
        public BLangExpression expr;

        public BLangAttachedFunctionInvocation(DiagnosticPos pos,
                                               List<BLangExpression> requiredArgs,
                                               List<BLangExpression> namedArgs,
                                               List<BLangExpression> restArgs,
                                               BSymbol symbol,
                                               BType type,
                                               BLangExpression expr,
                                               boolean async) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.namedArgs = namedArgs;
            this.restArgs = restArgs;
            this.symbol = symbol;
            this.type = type;
            this.expr = expr;
            this.async = async;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94.2
     */
    public static class BLangTransformerInvocation extends BLangInvocation {

        public BLangTransformerInvocation(DiagnosticPos pos,
                                          List<BLangExpression> requiredArgs,
                                          BSymbol symbol,
                                          BType type) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.symbol = symbol;
            this.type = type;
        }

        public BLangTransformerInvocation(DiagnosticPos pos,
                                          List<BLangExpression> requiredArgs,
                                          List<BLangExpression> namedArgs,
                                          List<BLangExpression> restArgs,
                                          BSymbol symbol,
                                          BType type) {
            this(pos, requiredArgs, symbol, type);
            this.namedArgs = namedArgs;
            this.restArgs = restArgs;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

}
