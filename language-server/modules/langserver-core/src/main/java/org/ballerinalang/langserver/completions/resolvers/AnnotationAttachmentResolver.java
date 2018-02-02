/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * AnnotationAttachmentResolver.
 */
public class AnnotationAttachmentResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        return filterAnnotations(completionContext);
    }

    /**
     * Filter the annotations from the data model.
     * @param completionContext - Completion operation context
     * @return {@link List}
     */
    ArrayList<CompletionItem> filterAnnotations(TextDocumentServiceContext completionContext) {

//        Set<Map.Entry<String, ModelPackage>> packages = dataModel.getPackages();
//        if (packages == null) {
//            packages = Utils.getAllPackages().entrySet();
//        }
//        Stream<AnnotationDef> annotationDefStream = packages.stream().map(i -> i.getValue().getAnnotations())
//                .flatMap(Collection::stream);
//        List<CompletionItem> collect = annotationDefStream.map(i -> {
//            CompletionItem importItem = new CompletionItem();
//
//            String insertText = lastPart(i.getPackagePath()) + ":" + i.getName();
//            importItem.setLabel("@" + insertText + " (" + i.getPackagePath() + ")");
//            if (dataModel.getParserRuleContext() == null) {
//                insertText = "@" + insertText;
//            } else {
//                if (findPreviousToken(dataModel, "@", 3) < 0) {
//                    insertText = "@" + insertText;
//                }
//            }
//            if (i.getAnnotationAttributeDefs().size() == 0) {
//                importItem.setInsertText(insertText + "{}");
//            } else {
//                importItem.setInsertText(insertText + "{${1}}");
//            }
//            importItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
//            importItem.setSortText(ItemResolverConstants.PRIORITY_6);
//            return importItem;
//        }).collect(Collectors.toList());

        ArrayList<CompletionItem> list = new ArrayList<>();
//        collect.sort(Comparator.comparing(CompletionItem::getLabel));
//        list.addAll(collect);
        return list;
    }

    /**
     * Get the last string part to append.
     *
     * @param packagePath - package path
     * @return {@link String}
     */
    public String lastPart(String packagePath) {
        int i = packagePath.lastIndexOf('.');
        if (i >= 0) {
            return packagePath.substring(i + 1);
        } else {
            return packagePath;
        }
    }
}
