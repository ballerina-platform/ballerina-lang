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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

/**
 * @since 1.3.0
 */
public class STComputedNameField extends STMappingField {

    public final STNode leadingComma;
    public final STNode openBracket;
    public final STNode fieldNameExpr;
    public final STNode closeBracket;
    public final STNode colon;
    public final STNode valueExpr;

    STComputedNameField(STNode leadingComma,
                        STNode openBracket,
                        STNode fieldNameExpr,
                        STNode closeBracket,
                        STNode colon,
                        STNode valueExpr) {
        super(SyntaxKind.COMPUTED_NAME_FIELD);
        this.leadingComma = leadingComma;
        this.openBracket = openBracket;
        this.fieldNameExpr = fieldNameExpr;
        this.closeBracket = closeBracket;
        this.colon = colon;
        this.valueExpr = valueExpr;

        this.bucketCount = 6;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(leadingComma, 0);
        this.addChildNode(openBracket, 1);
        this.addChildNode(fieldNameExpr, 2);
        this.addChildNode(closeBracket, 3);
        this.addChildNode(colon, 4);
        this.addChildNode(valueExpr, 5);
    }

    public NonTerminalNode createFacade(int position, NonTerminalNode parent) {
        return null;
    }
}
