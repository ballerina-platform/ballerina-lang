/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.XMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;

/**
 * @since 1.2.0
 */
public class BLangXMLElementAccess extends BLangExpression implements XMLElementAccess {

    // BLangNodes
    public BLangExpression expr;
    public List<BLangXMLElementFilter> filters;

    public BLangXMLElementAccess(Location pos, BLangExpression expr,
                                 List<BLangXMLElementFilter> filters) {
        this.expr = expr;
        this.filters = filters;
        this.pos = pos;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_ELEMENT_ACCESS;
    }

    @Override
    public List<BLangXMLElementFilter> getFilters() {
        return this.filters;
    }

    @Override
    public BLangExpression getExpression() {
        return this.expr;
    }
}
