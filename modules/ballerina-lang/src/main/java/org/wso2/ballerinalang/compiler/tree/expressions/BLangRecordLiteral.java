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
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_KEY_VALUE;

/**
 * Implementation of RecordTypeLiteralNode.
 *
 * @since 0.94
 */
public class BLangRecordLiteral extends BLangExpression implements RecordLiteralNode {

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
     * @since 0.94
     */
    public static class BLangRecordKeyValue extends BLangNode implements RecordKeyValueNode {

        public BLangExpression keyExpr;
        public BLangExpression valueExpr;

        @Override
        public BLangExpression getKey() {
            return keyExpr;
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
}
