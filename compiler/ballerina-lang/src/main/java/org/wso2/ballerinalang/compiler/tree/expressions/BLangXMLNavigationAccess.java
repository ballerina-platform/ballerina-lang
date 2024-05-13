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
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;
import java.util.StringJoiner;

/**
 * @since 1.2.0
 */
public class BLangXMLNavigationAccess extends BLangExpression implements XMLNavigationAccess {

    // BLangNodes
    public BLangExpression expr;
    public BLangExpression childIndex;
    public final List<BLangXMLElementFilter> filters;
    public final List<BLangXMLStepExtend> extensions;

    // Parser Flags and Data
    public final NavAccessType navAccessType;

    // Semantic Data
    // Hack to avoid duplicate error messages in CodeAnalyzer, the reason for this flag is since we are adding
    // the 'receiver' of a method invocation as the first parameter to langlib invocations, XMLNavigationAccess
    // could be checked multiple times when used with langlib functions producing multiple error messages.
    public boolean methodInvocationAnalyzed;

    public BLangXMLNavigationAccess(Location pos, BLangExpression expr,
                                    List<BLangXMLElementFilter> filters,
                                    NavAccessType navAccessType,
                                    BLangExpression childIndex,
                                    List<BLangXMLStepExtend> extensions) {
        this.pos = pos;
        this.expr = expr;
        this.filters = filters;
        this.navAccessType = navAccessType;
        this.childIndex = childIndex;
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
        return NodeKind.XML_NAVIGATION;
    }

    @Override
    public NavAccessType getNavAccessType() {
        return navAccessType;
    }

    @Override
    public List<BLangXMLElementFilter> getFilters() {
        return this.filters;
    }

    @Override
    public BLangExpression getExpression() {
        return this.expr;
    }

    @Override
    public BLangExpression getChildIndex() {
        return this.childIndex;
    }

    @Override
    public String toString() {
        switch (navAccessType) {
            case CHILDREN:
                return String.valueOf(expr) + "/*";
            case CHILD_ELEMS:
                StringJoiner filters = new StringJoiner(" |");
                this.filters.forEach(f -> filters.toString());
                return String.valueOf(expr) + "/<" + filters.toString() + ">" +
                        (childIndex != null ? "[" + String.valueOf(childIndex) + "]" : "");
        }
        return null;
    }
}
