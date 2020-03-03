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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;

public abstract class NodeAndTokenProvider {

    public abstract STToken getCurrentToken();

    public abstract STToken consumeToken(SyntaxKind expectedKind);

    // 0 will be the current token
    public abstract STToken peekToken(int tokenIndex);

    public abstract STNode getCurrentNode();

    public abstract STNode consumeNode();

    protected STToken createMissingToken(SyntaxKind expectedKind, SyntaxKind foundKind, boolean isError) {
        STToken missingToken = new STMissingToken(expectedKind);
        if (isError) {
            // TODO report error
        }

        return missingToken;
    }
}
