/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.WaitForAllExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the await for all expression implementation.
 *
 * @since 0.985
 */
public class BLangWaitForAllExpr extends BLangExpression implements WaitForAllExpressionNode {

    private static final String WAIT_KEYWORD = "wait";

    public List<BLangWaitKeyValue> keyValuePairs = new ArrayList<>();

    public List<BLangWaitKeyValue> getKeyValuePairs() {
        return keyValuePairs;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WAIT_EXPR;
    }

    @Override
    public String toString() {
        return WAIT_KEYWORD + " {" + String.join(",", keyValPairs()) + "}";
    }

    private List<String> keyValPairs() {
        List<String> keyValList = new ArrayList<>();
        for (BLangWaitKeyValue keyVal : keyValuePairs) {
            if (keyVal.valueExpr != null) {
                keyValList.add(keyVal.key.value + ":" + keyVal.valueExpr.toString());
            } else {
                keyValList.add(keyVal.key.value);
            }
        }
        return keyValList;
    }
    /**
     * This static inner class represents key/value pair of a wait collection.
     *
     * @since 0.985
     */
    public static class BLangWaitKeyValue extends BLangNode implements WaitKeyValueNode {
        public BLangIdentifier key;
        public BLangExpression valueExpr;
        public BLangExpression keyExpr;

        @Override
        public BLangIdentifier getKey() {
            return key;
        }

        @Override
        public BLangExpression getValue() {
            return valueExpr;
        }

        @Override
        public NodeKind getKind() {
            return NodeKind.WAIT_LITERAL_KEY_VALUE;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * This class represents a wait literal expression.
     *
     * @since 0.985
     */
    public static class BLangWaitLiteral extends BLangWaitForAllExpr {
        public BAttachedFunction initializer;

        public BLangWaitLiteral(List<BLangWaitKeyValue> keyValuePairs, BType structType) {
            this.keyValuePairs = keyValuePairs;
            this.type = structType;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
