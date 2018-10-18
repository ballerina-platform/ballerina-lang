/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Annotation Attachment Resolver to resolve the corresponding annotation attachments.
 */
public class ParserRuleAnnotationAttachmentResolver extends AbstractItemResolver {
    
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        String attachmentPointType = ctx.get(CompletionKeys.NEXT_NODE_KEY) != null ?
                ctx.get(CompletionKeys.NEXT_NODE_KEY) : "";

        if (attachmentPointType.isEmpty()) {
            return new ArrayList<>();
        }
        return filterAnnotations(attachmentPointType, ctx);
    }

    /**
     * Filter the annotations from the data model.
     * 
     * @return {@link List}
     */
    private ArrayList<CompletionItem> filterAnnotations(String attachmentPoint, LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        
        LSAnnotationCache.getInstance().getAnnotationMapForType(attachmentPoint, ctx)
                .entrySet()
                .forEach(annotationLists -> annotationLists.getValue().forEach(bLangAnnotation -> {
                    completionItems.add(CommonUtil.getAnnotationCompletionItem(annotationLists.getKey(),
                            bLangAnnotation, ctx));
                }));
        
        return completionItems;
    }
}
