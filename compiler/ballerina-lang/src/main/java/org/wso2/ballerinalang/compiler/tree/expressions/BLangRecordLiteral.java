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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_KEY_VALUE;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_VAR_NAME;

/**
 * The super class of all the record literal expressions.
 *
 * @see BLangStructLiteral
 * @see BLangMapLiteral
 * @see BLangTableLiteral
 * @since 0.94
 */
public class BLangRecordLiteral extends BLangExpression implements RecordLiteralNode {

    public List<RecordField> fields;

    public BLangRecordLiteral() {
        fields = new ArrayList<>();
    }

    public BLangRecordLiteral(DiagnosticPos pos) {
        this();
        this.pos = pos;
    }

    public BLangRecordLiteral(DiagnosticPos pos, BType type) {
        this.pos = pos;
        fields = new ArrayList<>();
        this.type = type;
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

        public BLangRecordKeyValueField() {
        }

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

        @Override
        public NodeKind getKind() {
            return RECORD_LITERAL_VAR_NAME;
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

        public BLangStructLiteral(DiagnosticPos pos, List<RecordField> fields, BType structType) {
            super(pos);
            this.fields = fields;
            this.type = structType;
            this.initializer = ((BRecordTypeSymbol) structType.tsymbol).initializerFunc;
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

        public BLangMapLiteral(DiagnosticPos pos, List<RecordField> fields, BType mapType) {
            super(pos);
            this.fields = fields;
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

        public BLangJSONLiteral(DiagnosticPos pos, List<RecordField> fields, BType jsonType) {
            super(pos);
            this.fields = fields;
            this.type = jsonType;
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

        public BLangChannelLiteral(DiagnosticPos pos, BType channelType, String channelName) {
            super(pos);
            this.type = channelType;
            this.channelName = channelName;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
