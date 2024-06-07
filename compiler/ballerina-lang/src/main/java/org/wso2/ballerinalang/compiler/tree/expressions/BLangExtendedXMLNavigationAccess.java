/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.tree.expressions;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;
import java.util.StringJoiner;

/**
 * @since 2201.10.0
 */
public class BLangExtendedXMLNavigationAccess extends BLangExpression implements XMLNavigationAccess {

    // BLangNodes
    public BLangXMLNavigationAccess stepExpr;
    public final List<BLangXMLStepExtend> extensions;

    // Parser Flags and Data
    public final NavAccessType navAccessType;

    public BLangExtendedXMLNavigationAccess(Location pos, BLangXMLNavigationAccess stepExpr,
                                            NavAccessType navAccessType, List<BLangXMLStepExtend> extensions) {
        this.pos = pos;
        this.stepExpr = stepExpr;
        this.navAccessType = navAccessType;
        this.extensions = extensions;
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
        return NodeKind.XML_EXTENDED_NAVIGATION;
    }

    @Override
    public NavAccessType getNavAccessType() {
        return navAccessType;
    }

    @Override
    public List<BLangXMLElementFilter> getFilters() {
        return null;
    }

    @Override
    public BLangExpression getExpression() {
        return null;
    }

    @Override
    public String toString() {
        switch (navAccessType) {
            case CHILDREN:
                return  "/*";
            case CHILD_ELEMS:
                StringJoiner filters = new StringJoiner(" |");
                return "/<" + filters.toString() + ">";
        }
        return null;
    }
}
