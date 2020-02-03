/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.scopeproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.contextproviders.AnnotationAttachmentContextProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceContextResolver.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ServiceScopeProvider extends AbstractCompletionProvider {

    public ServiceScopeProvider() {
        this.attachmentPoints.add(BLangService.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) throws LSCompletionException {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        if (this.isWithinAttachedExpressions(ctx)) {
            // suggest all the visible, defined listeners
            return this.getCompletionItemsAfterOnKeyword(ctx);
        }
        if (this.isAnnotationAttachmentContext(ctx)) {
            return this.getProvider(AnnotationAttachmentContextProvider.class).getCompletions(ctx);
        }

        completionItems.add(Snippet.KW_PUBLIC.get().build(ctx));
        completionItems.addAll(this.getResourceSnippets(ctx));
        completionItems.add(Snippet.DEF_FUNCTION.get().build(ctx));

        ctx.put(CompletionKeys.ITEM_SORTER_KEY, BLangService.class);

        return completionItems;
    }

    private boolean isWithinAttachedExpressions(LSContext lsContext) {
        BLangNode bLangNode = lsContext.get(CompletionKeys.SCOPE_NODE_KEY);
        if (!(bLangNode instanceof BLangService)) {
            return false;
        }
        BLangService service = (BLangService) bLangNode;
        Position cursorPos = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int line = cursorPos.getLine();
        int col = cursorPos.getCharacter();
        List<BLangExpression> attachedExprs = service.attachedExprs;
        if (attachedExprs.isEmpty()) {
            return false;
        }
        BLangExpression firstExpr = attachedExprs.get(0);
        BLangExpression lastExpr = CommonUtil.getLastItem(attachedExprs);
        DiagnosticPos firstExprPos = CommonUtil.toZeroBasedPosition(firstExpr.pos);
        int fSLine = firstExprPos.sLine;
        int fSCol = firstExprPos.sCol;
        DiagnosticPos lastExprPos = CommonUtil.toZeroBasedPosition(lastExpr.pos);
        int lSLine = lastExprPos.sLine;
        int lECol = lastExprPos.eCol;

        return fSLine <= line && lSLine >= line && (fSCol <= col && lECol >= col);
    }
}
