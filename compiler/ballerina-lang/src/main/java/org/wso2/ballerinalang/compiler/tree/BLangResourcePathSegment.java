/*
*  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;


/**
 * Represent resource path segment.
 *
 * @since 2201.3.0
 */
public class BLangResourcePathSegment extends BLangNode {
    
    public BLangIdentifier name;
    public BResourcePathSegmentSymbol symbol;
    public NodeKind kind;
    public BLangType typeNode;

    public BLangResourcePathSegment(NodeKind kind) {
        this.kind = kind;
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
    public String toString() {
        switch (this.kind) {
            case RESOURCE_PATH_PARAM_SEGMENT:
                return "[" + type + "]";
            case RESOURCE_PATH_REST_PARAM_SEGMENT:
                return "[" + type + "...]";
            default:
                return name.value;
        }
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RESOURCE_FUNC;
    }
}
