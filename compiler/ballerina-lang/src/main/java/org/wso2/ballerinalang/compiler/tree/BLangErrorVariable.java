/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.ErrorVariableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an error variable node.
 * <p>
 * error (var reason, message = msgVar, ...var restOfIt) =
 *                          error ("Error Code", message = "Error Message", moreStuff = "some more");
 *
 * @since 0.985.0
 */
public class BLangErrorVariable extends BLangVariable implements ErrorVariableNode {
    public BLangSimpleVariable message;
    public List<BLangErrorDetailEntry> detail;
    public BLangSimpleVariable restDetail;
    public BLangInvocation detailExpr;
    public boolean reasonVarPrefixAvailable;
    public BLangLiteral reasonMatchConst; // todo: no longer needed remove
    public boolean isInMatchStmt;
    public BLangVariable cause;

    public BLangErrorVariable() {
        this.annAttachments = new ArrayList<>();
        this.detail = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
    }

    @Override
    public BLangSimpleVariable getMessage() {
        return message;
    }

    @Override
    public List<BLangErrorDetailEntry> getDetail() {
        return detail;
    }

    @Override
    public BLangSimpleVariable getRestDetail() {
        return this.restDetail;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ERROR_VARIABLE;
    }

    @Override
    public String toString() {
        StringJoiner details = new StringJoiner(", ");
        detail.forEach(d -> details.add(d.key.toString() + "=" + d.valueBindingPattern.toString()));
        return "error (" + message + ", " + details.toString() +
                (restDetail != null ? ", ...var " + restDetail.name.toString() : "") + ")";
    }

    /**
     * {@code BLangErrorDetailEntry} represent error detail entry in error binding pattern.
     *
     * @since 0.995.0
     */
    public static class BLangErrorDetailEntry implements ErrorVariableNode.ErrorDetailEntry {

        public BLangIdentifier key;

        public BLangErrorDetailEntry(BLangIdentifier key, BLangVariable valueBindingPattern) {
            this.key = key;
            this.valueBindingPattern = valueBindingPattern;
        }

        public BLangVariable valueBindingPattern;

        @Override
        public BLangIdentifier getKey() {
            return key;
        }

        @Override
        public BLangVariable getValue() {
            return valueBindingPattern;
        }

        @Override
        public String toString() {
            return key + ": " + valueBindingPattern;
        }
    }
}
