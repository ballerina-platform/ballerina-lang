/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context.util;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * Utilities for the import declaration context.
 *
 * @since 2.0.0
 */
public class ImportDeclarationContextUtil {
    private ImportDeclarationContextUtil() {
    }

    /**
     * Get the insert text for the langlib module name.
     * If the langlib is pre-declared, then append the ' for the langlib name.
     *
     * @param pkgName to be modified
     * @return {@link String} modified package name
     */
    public static String getLangLibModuleNameInsertText(String pkgName) {
        return (CommonUtil.PRE_DECLARED_LANG_LIBS.contains(pkgName) ? pkgName.replace(".", ".'") : pkgName) + ";";
    }

    /**
     * Get the completion item for the import declaration.
     *
     * @param context    completion operation context
     * @param label      for the completion item
     * @param insertText for the completion item
     * @return {@link LSCompletionItem}
     */
    public static LSCompletionItem getImportCompletion(BallerinaCompletionContext context, String label,
                                                       String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.Module);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.MODULE);
    }
}
