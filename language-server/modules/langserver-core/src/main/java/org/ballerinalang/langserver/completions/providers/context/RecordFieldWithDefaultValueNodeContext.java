/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.List;

/**
 * Completion provider for {@link RecordFieldWithDefaultValueNode} context.
 * <p>
 * Only handles the following cases,
 * eg:
 * (1) int field = [cursor]
 * (2) int field = c[cursor]
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RecordFieldWithDefaultValueNodeContext extends
        NodeWithRHSInitializerProvider<RecordFieldWithDefaultValueNode> {

    public RecordFieldWithDefaultValueNodeContext() {
        super(RecordFieldWithDefaultValueNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, RecordFieldWithDefaultValueNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = 
                this.initializerContextCompletions(ctx, node.typeName(), node.expression());
        this.sort(ctx, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, RecordFieldWithDefaultValueNode node) {
        /*
        Only validates the following cases,
        eg: 
        (1) int field = <cursor>
        (2) int field = c<cursor>
         */
        int textPosition = context.getCursorPositionInTree();
        TextRange equalTokenRange = node.equalsToken().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }
}
