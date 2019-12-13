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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    public boolean functionPointerInvocation;
    public boolean actionInvocation;
    public boolean langLibInvocation;
    public boolean async;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments = new ArrayList<>();

    /*
     * Below expressions are used by typechecker, desugar and codegen phases.
     * Above phases must rely on below expr lists, rather than {@link #argExprs}
     */
    public List<BLangExpression> requiredArgs = new ArrayList<>();
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
    public List<? extends ExpressionNode> getRequiredArgs() {
        return this.requiredArgs;
    }

    @Override
    public BLangExpression getExpression() {
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
        br.append(name == null ? String.valueOf(symbol.name) : String.valueOf(name));
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

        return false;
    }

    @Override
    public boolean isActionInvocation() {
        return this.actionInvocation;
    }
    
    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public Set<Flag> getFlags() {
        return flagSet;
    }

    @Override
    public void addFlag(Flag flag) {
        this.getFlags().add(flag);
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }

    /**
     * @since 0.94
     */
    public static class BFunctionPointerInvocation extends BLangInvocation {

        public BFunctionPointerInvocation(DiagnosticPos pos, BLangExpression varRef, BSymbol bSymbol, BType retType) {
            this.pos = pos;
            this.expr = varRef;
            this.symbol = bSymbol;
            this.type = retType;
        }

        public BFunctionPointerInvocation(BLangInvocation parent, BLangExpression varRef) {
            this.pos = parent.pos;
            this.name = parent.name;
            this.requiredArgs = parent.requiredArgs;
            this.restArgs = parent.restArgs;
            this.symbol = parent.symbol;
            this.async = parent.async;
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
                                               List<BLangExpression> restArgs,
                                               BSymbol symbol,
                                               BType type,
                                               BLangExpression expr,
                                               boolean async) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
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
     * @since 0.94
     */
    public static class BLangActionInvocation extends BLangInvocation {

        public BLangActionInvocation(DiagnosticPos pos,
                                     List<BLangExpression> requiredArgs,
                                     List<BLangExpression> restArgs,
                                     BSymbol symbol,
                                     BType type,
                                     boolean async) {
            this.pos = pos;
            this.requiredArgs = requiredArgs;
            this.restArgs = restArgs;
            this.symbol = symbol;
            this.type = type;
            this.async = async;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
