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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_KEY_VALUE;

/**
 * The super class of all the record literal expressions.
 *
 * @see BLangStructLiteral
 * @see BLangMapLiteral
 * @see BLangTableLiteral
 * @since 0.94
 */
public class BLangRecordLiteral extends BLangExpression implements RecordLiteralNode {

    /**
     * The identifier of this node.
     */
    public BLangIdentifier name;

    public List<BLangRecordKeyValue> keyValuePairs;

    public BLangRecordLiteral() {
        keyValuePairs = new ArrayList<>();
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RECORD_LITERAL_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<BLangRecordKeyValue> getKeyValuePairs() {
        return keyValuePairs;
    }

    @Override
    public String toString() {
        return keyValuePairs.toString();
    }

    /**
     * This static inner class represents key/value pair of a record literal.
     *
     * @since 0.94
     */
    public static class BLangRecordKeyValue extends BLangNode implements RecordKeyValueNode {

        public BLangRecordKey key;
        public BLangExpression valueExpr;

        @Override
        public BLangExpression getKey() {
            return key.expr;
        }

        @Override
        public BLangExpression getValue() {
            return valueExpr;
        }

        @Override
        public NodeKind getKind() {
            return RECORD_LITERAL_KEY_VALUE;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {

        }
    }

    /**
     * This class represents a key expression in a key/value pair of a record literal.
     *
     * @since 0.94
     */
    public static class BLangRecordKey extends BLangNode {

        public BLangExpression expr;

        // This field is set only if the record type is struct.
        public BVarSymbol fieldSymbol;

        public BLangRecordKey(BLangExpression expr) {
            this.expr = expr;
        }

        @Override
        public NodeKind getKind() {
            return null;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {

        }
    }

    /**
     * This class represents a struct literal expression.
     *
     * @since 0.94
     */
    public static class BLangStructLiteral extends BLangRecordLiteral {
        public BStructSymbol.BAttachedFunction initializer;

        public BLangStructLiteral(List<BLangRecordKeyValue> keyValuePairs, BType structType) {
            this.keyValuePairs = keyValuePairs;
            this.type = structType;
            this.initializer = ((BStructSymbol) structType.tsymbol).initializerFunc;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * This class represents a map literal expression.
     *
     * @since 0.94
     */
    public static class BLangMapLiteral extends BLangRecordLiteral {

        public BLangMapLiteral(List<BLangRecordKeyValue> keyValuePairs, BType mapType) {
            this.keyValuePairs = keyValuePairs;
            this.type = mapType;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * This class represents a JSON type literal expression.
     *
     * @since 0.94
     */
    public static class BLangJSONLiteral extends BLangRecordLiteral {

        public BLangJSONLiteral(List<BLangRecordKeyValue> keyValuePairs, BType jsonType) {
            this.keyValuePairs = keyValuePairs;
            this.type = jsonType;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * This class represents a stream type literal expression.
     *
     * @since 0.964.0
     */
    public static class BLangStreamLiteral extends BLangRecordLiteral {

        public BLangStreamLiteral(BType streamType, BLangIdentifier name) {
            this.type = streamType;
            this.name = name;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
