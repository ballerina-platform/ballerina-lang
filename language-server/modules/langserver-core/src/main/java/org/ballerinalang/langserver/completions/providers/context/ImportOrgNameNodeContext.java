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
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ImportDeclarationContextUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ImportOrgNameNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImportOrgNameNodeContext extends AbstractCompletionProvider<ImportOrgNameNode> {

    public ImportOrgNameNodeContext() {
        super(ImportOrgNameNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, ImportOrgNameNode node) {
        /*
        Following use cases are addressed.
        Eg: (1) import org/<cursor>
         */
        String orgName = node.orgName().text();

        if (orgName.isEmpty()) {
            throw new AssertionError("ModuleName cannot be empty");
        }

        List<LSPackageLoader.ModuleInfo> moduleList =
                new ArrayList<>(LSPackageLoader.getInstance(ctx.languageServercontext()).getAllVisiblePackages(ctx));
        ArrayList<LSCompletionItem> completionItems = moduleNameContextCompletions(ctx, orgName, moduleList);
        this.sort(ctx, node, completionItems);

        return completionItems;
    }

    private ArrayList<LSCompletionItem> moduleNameContextCompletions(BallerinaCompletionContext context, String orgName,
                                                                     List<LSPackageLoader.ModuleInfo> moduleList) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        moduleList.forEach(ballerinaPackage -> {
            String packageName = ballerinaPackage.packageName().value();
            String insertText;
            if (orgName.equals(ballerinaPackage.packageOrg().value()) && !pkgNameLabels.contains(packageName)
                    && ModuleUtil.matchingImportedModule(context, ballerinaPackage).isEmpty()) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.value + ".")) {
                    insertText = ImportDeclarationContextUtil.getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                pkgNameLabels.add(packageName);
                // Do not add the semi colon at the end of the insert text since the user might type the as keyword
                completionItems.add(ImportDeclarationContextUtil.getImportCompletion(context, packageName, insertText));
            }
        });

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ImportOrgNameNode node, List<LSCompletionItem> compItems) {
        String orgName = node.orgName().text();
        if (!"ballerina".equals(orgName)) {
            return;
        }
        compItems.forEach(item -> {
            int rank = item.getCompletionItem().getLabel().startsWith("lang.") ? 2 : 1;
            item.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        });
    }
}
