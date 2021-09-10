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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_KEY_VALUE;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_SPREAD_OP;

/**
 * The super class of all the record literal expressions.
 *
 * @see BLangStructLiteral
 * @see BLangMapLiteral
 * @since 0.94
 */
public class BLangRecordLiteral extends BLangExpression implements RecordLiteralNode {

    public List<RecordField> fields;

    public BLangRecordLiteral() {
        fields = new ArrayList<>();
    }

    public BLangRecordLiteral(Location pos) {
        this();
        this.pos = pos;
    }

    public BLangRecordLiteral(Location pos, BType type) {
        this.pos = pos;
        fields = new ArrayList<>();
        this.setBType(type);
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
    public List<RecordField> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return " {" + fields.stream()
                .map(RecordField::toString)
                .collect(Collectors.joining(",")) + "}";
    }

    /**
     * This static inner class represents key/value pair of a record literal.
     *
     * @since 0.94
     */
    public static class BLangRecordKeyValueField extends BLangNode implements RecordKeyValueFieldNode {

        public BLangRecordKey key;
        public BLangExpression valueExpr;
        public boolean readonly;

        public BLangRecordKeyValueField() {
        }

        @Deprecated
        public BLangRecordKeyValueField(BLangRecordKey key, BLangExpression valueExpr) {
            this.key = key;
            this.valueExpr = valueExpr;
        }

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
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return key + ((valueExpr != null) ? ": " + valueExpr : "");
        }

        @Override
        public boolean isKeyValueField() {
            return true;
        }
    }

    /**
     * This static inner class represents a variable name as a field in a mapping constructor.
     *
     * @since 1.2.0
     */
    public static class BLangRecordVarNameField extends BLangSimpleVarRef implements RecordVarNameFieldNode {

        public boolean readonly;

        @Override
        public boolean isKeyValueField() {
            return false;
        }
    }

    /**
     * This static inner class represents a spread operator as a field in a mapping constructor.
     *
     * @since 1.2.0
     */
    public static class BLangRecordSpreadOperatorField extends BLangNode implements RecordSpreadOperatorFieldNode {

        public BLangExpression expr;

        @Override
        public BLangExpression getExpression() {
            return expr;
        }

        @Override
        public void setExpression(ExpressionNode expr) {
            this.expr = (BLangExpression) expr;
        }

        @Override
        public NodeKind getKind() {
            return RECORD_LITERAL_SPREAD_OP;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "..." + expr;
        }

        @Override
        public boolean isKeyValueField() {
            return false;
        }
    }

    /**
     * This class represents a key expression in a key/value pair of a record literal.
     *
     * @since 0.94
     */
    public static class BLangRecordKey extends BLangNode {

        public boolean computedKey = false;

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
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return expr.toString();
        }
    }

    /**
     * This class represents a struct literal expression.
     *
     * @since 0.94
     */
    public static class BLangStructLiteral extends BLangRecordLiteral {
        public BAttachedFunction initializer;
        public TreeMap<Integer, BVarSymbol> enclMapSymbols;

        public BLangStructLiteral(Location pos, BType structType, List<RecordField> fields) {
            super(pos);
            this.setBType(structType);
            BTypeSymbol typeSymbol = structType.getKind() == TypeKind.TYPEREFDESC ?
                    ((BTypeReferenceType) structType).referredType.tsymbol : structType.tsymbol;
            this.initializer = ((BRecordTypeSymbol) typeSymbol).initializerFunc;
            this.fields = fields;
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

        public BLangMapLiteral(Location pos, BType mapType, List<RecordField> fields) {
            super(pos);
            this.setBType(mapType);
            this.fields = fields;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * This class represents a channel type literal expression.
     *
     * @since 0.982.0
     */
    public static class BLangChannelLiteral extends BLangRecordLiteral {

        public String channelName;

        public BLangChannelLiteral(Location pos, BType channelType, String channelName) {
            super(pos);
            this.setBType(channelType);
            this.channelName = channelName;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
