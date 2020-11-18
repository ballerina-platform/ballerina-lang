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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Completion provider for {@link ImportOrgNameNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ImportOrgNameNodeContext extends AbstractCompletionProvider<ImportOrgNameNode> {

    public ImportOrgNameNodeContext() {
        super(ImportOrgNameNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx, ImportOrgNameNode node) {
        /*
        Following use cases are addressed.
        Eg: (1) import org/<cursor>
         */
        String orgName = node.orgName().text();

        if (orgName.isEmpty()) {
            throw new AssertionError("ModuleName cannot be empty");
        }

        List<BallerinaPackage> packagesList = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(packagesList::addAll);

        return new ArrayList<>(moduleNameContextCompletions(ctx, orgName, packagesList));
    }

    // TODO: Remove the duplicate code with the ImportDeclarationNodeContext
    private String getLangLibModuleNameInsertText(String pkgName) {
        return pkgName.replace(".", ".'") + ";";
    }

    private ArrayList<LSCompletionItem> moduleNameContextCompletions(LSContext context, String orgName,
                                                                     List<BallerinaPackage> packagesList) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        packagesList.forEach(ballerinaPackage -> {
            if (isAnnotationLangLib(ballerinaPackage)) {
                return;
            }
            String packageName = ballerinaPackage.getPackageName();
            String insertText;
            if (orgName.equals(ballerinaPackage.getOrgName()) && !pkgNameLabels.contains(packageName)) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.value + ".")) {
                    insertText = getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                pkgNameLabels.add(packageName);
                // Do not add the semi colon at the end of the insert text since the user might type the as keyword
                completionItems.add(getImportCompletion(context, packageName, insertText));
            }
        });

        return completionItems;
    }

    private static LSCompletionItem getImportCompletion(LSContext context, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.Module);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.MODULE);
    }

    private boolean isAnnotationLangLib(BallerinaPackage ballerinaPackage) {
        return "ballerina".equals(ballerinaPackage.getOrgName())
                && "lang.annotations".equals(ballerinaPackage.getPackageName());
    }
}
