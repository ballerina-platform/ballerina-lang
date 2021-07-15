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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;

import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link RecordFieldNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RecordFieldNodeContext extends AbstractCompletionProvider<RecordFieldNode> {

    public RecordFieldNodeContext() {
        super(RecordFieldNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, RecordFieldNode node)
            throws LSCompletionException {
        if (this.onFieldNameContext(context, node)) {
            /*
            Handles the following use-case
            eg:
            (1) type test record {
                    int a<cursor>
                }
                At the cursor position, no completions are provided
             */
            return Collections.emptyList();
        }
        if (this.onTypeNameContext(context, node)) {
            /*
            If the type name has a particular resolver implemented, then the completion will be handled by the type
            descriptor resolver.
            Ex: Table type descriptor implementation.
            type TableRecord record {
                string name;
                int age;
                table<Country> <cursor>
            } 
             */
            return CompletionUtil.route(context, node.typeName());
        }

        return CompletionUtil.route(context, node.parent());
    }

    private boolean onTypeNameContext(BallerinaCompletionContext context, RecordFieldNode node) {
        int cursor = context.getCursorPositionInTree();
        int typeEnd = node.typeName().textRange().endOffset();
        int typeStart = node.typeName().textRange().startOffset();

        return cursor >= typeStart && cursor <= typeEnd;
    }
    
    private boolean onFieldNameContext(BallerinaCompletionContext context, RecordFieldNode node) {
        int cursor = context.getCursorPositionInTree();
        Node typeName = node.typeName();
        
        return !typeName.isMissing() && cursor > typeName.textRange().endOffset();
    }
}
