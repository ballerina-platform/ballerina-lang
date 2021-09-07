/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link NamedArgumentNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class NamedArgumentNodeContext extends AbstractCompletionProvider<NamedArgumentNode> {

    public NamedArgumentNodeContext() {
        super(NamedArgumentNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, NamedArgumentNode node)
            throws LSCompletionException {
        return Collections.emptyList();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, NamedArgumentNode node) {
        /*
        Following cases are considered to be the named arg context.s
        (1) arg1=<cursor>
        (2) arg2= m<cursor>
         */
        int cursor = context.getCursorPositionInTree();
        TextRange textRange = node.expression().textRange();
        return cursor > node.equalsToken().textRange().endOffset()
                && cursor >= textRange.startOffset()
                && cursor <= textRange.endOffset();
    }
}
