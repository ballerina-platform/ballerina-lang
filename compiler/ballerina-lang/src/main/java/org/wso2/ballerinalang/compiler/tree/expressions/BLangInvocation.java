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
import org.wso2.ballerinalang.programfile.Instruction.RegIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link org.ballerinalang.model.tree.expressions.InvocationNode}.
 *
 * @since 0.94
 */
public class BLangInvocation extends BLangVariableReference implements InvocationNode, MultiReturnExpr {

    public BLangIdentifier pkgAlias;
    public BLangIdentifier name;
    public List<BLangExpression> argExprs = new ArrayList<>();
    public BLangVariableReference expr;
    public List<BType> types = new ArrayList<>(0);
    public BSymbol symbol;
    public boolean functionPointerInvocation;
    /* Variables Required for Iterable Operation */
    public boolean iterableOperationInvocation;
    public IterableContext iContext;
    protected RegIndex[] regIndexes;
    public boolean actionInvocation;

    /*
     * Below expressions are used by typechecker, desugar and codegen phases.
     * Above phases must rely on below expr lists, rather than {@link #argExprs}
     */
    public List<BLangExpression> requiredArgs = new ArrayList<>();
    public List<BLangExpression> namedArgs = new ArrayList<>();
    public List<BLangExpression> restArgs = new ArrayList<>();

    public boolean isMultiReturnExpr() {
        return true;
    }

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
    public List<BType> getTypes() {
        return types;
    }

    @Override
    public void setTypes(List<BType> types) {
        this.types = types;
    }

    public RegIndex[] getRegIndexes() {
        return regIndexes;
    }

    public void setRegIndexes(RegIndex[] regIndexes) {
        this.regIndexes = regIndexes;
        this.regIndex = regIndexes != null && regIndexes.length > 0 ? regIndexes[0] : null;
    }

    @Override
    public boolean isIterableOperation() {
        return iterableOperationInvocation;
    }

    @Override
    public boolean isActionInvocation() {
        return this.actionInvocation;
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
            this.types = parent.types;
            this.regIndexes = parent.regIndexes;
            this.symbol = parent.symbol;
            this.expr = varRef;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
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
                                               List<BType> types,
                                               BLangExpression expr) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.namedArgs = namedArgs;
            this.restArgs = restArgs;
            this.symbol = symbol;
            this.types = types;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
            this.expr = expr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangActionInvocation extends BLangInvocation {

        public BLangActionInvocation(DiagnosticPos pos,
                                     List<BLangExpression> requiredArgs,
                                     List<BLangExpression> namedArgs,
                                     List<BLangExpression> restArgs,
                                     BSymbol symbol,
                                     List<BType> types) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.namedArgs = namedArgs;
            this.restArgs = restArgs;
            this.symbol = symbol;
            this.types = types;
            if (types.size() > 0) {
                this.type = types.get(0);
            }
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
                                          List<BType> types) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.symbol = symbol;
            this.types = types;
            if (!types.isEmpty()) {
                this.type = types.get(0);
            }
        }

        public BLangTransformerInvocation(DiagnosticPos pos,
                                          List<BLangExpression> requiredArgs,
                                          List<BLangExpression> namedArgs,
                                          List<BLangExpression> restArgs,
                                          BSymbol symbol,
                                          List<BType> types) {
            this(pos, requiredArgs, symbol, types);
            this.namedArgs = namedArgs;
            this.restArgs = restArgs;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
