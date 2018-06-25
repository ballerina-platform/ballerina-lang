/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TableLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link TableLiteralNode}.
 *
 * @since 0.970.0
 */
public class BLangTableLiteral extends BLangExpression implements TableLiteralNode {

    public List<BLangTableColumn> columns;

    public List<BLangExpression> tableDataRows;

    public BLangArrayLiteral indexColumnsArrayLiteral;

    public BLangArrayLiteral keyColumnsArrayLiteral;

    public BLangTableLiteral() {
        columns = new ArrayList<>();
        tableDataRows = new ArrayList<>();
    }

    public BLangTableColumn getColumn(String columnName) {
        for (BLangTableColumn column : columns) {
            if (column.columnName.equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public List<? extends ExpressionNode> getDataRows() {
        return this.tableDataRows;
    }

    @Override
    public List<? extends Node> getTableColumns() {
        return this.columns;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TABLE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }


    /**
     * This class represents a column of a table.
     *
     * @since 0.980.0
     */
    public static class BLangTableColumn extends BLangNode {

        public String columnName;
        public Set<TableColumnFlag> flagSet;

        public BLangTableColumn(String columnName) {
            this.columnName = columnName;
            this.flagSet = EnumSet.noneOf(TableColumnFlag.class);
        }

        @Override
        public NodeKind getKind() {
            return null;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            /* ignore */
        }
    }
}
