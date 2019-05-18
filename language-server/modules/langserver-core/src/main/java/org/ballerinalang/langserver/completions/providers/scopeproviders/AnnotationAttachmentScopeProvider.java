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
import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;

/**
 * Annotation resolver for BLangAnnotationAttachment node context.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class AnnotationAttachmentScopeProvider extends LSCompletionProvider {

    public AnnotationAttachmentScopeProvider() {
        this.attachmentPoints.add(BLangAnnotationAttachment.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        BLangNode scopeNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);
        if (((BLangAnnotationAttachment) scopeNode).expr instanceof BLangRecordLiteral) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) ((BLangAnnotationAttachment) scopeNode).expr;
            return BLangRecordLiteralUtil.getFieldsForMatchingRecord(recordLiteral, ctx);
        }

        return new ArrayList<>();
    }
}
