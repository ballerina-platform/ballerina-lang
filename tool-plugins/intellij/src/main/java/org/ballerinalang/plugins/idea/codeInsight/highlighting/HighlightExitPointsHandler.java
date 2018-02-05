/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.codeInsight.highlighting;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.controlFlow.AnalysisCanceledException;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ReturnStatementNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.ThrowStatementNode;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {

    private final PsiElement myTarget;

    HighlightExitPointsHandler(final Editor editor, final PsiFile file, final PsiElement target) {
        super(editor, file);
        myTarget = target;
    }

    @Override
    public List<PsiElement> getTargets() {
        return Collections.singletonList(myTarget);
    }

    @Override
    protected void selectTargets(final List<PsiElement> targets, final Consumer<List<PsiElement>> selectionConsumer) {
        selectionConsumer.consume(targets);
    }

    @Override
    public void computeUsages(final List<PsiElement> targets) {
        PsiElement parent = PsiTreeUtil.getParentOfType(myTarget, StatementNode.class);
        if (!(parent instanceof ReturnStatementNode) && !(parent instanceof ThrowStatementNode)) {
            return;
        }
        final PsiElement definitionNode = PsiTreeUtil.getParentOfType(parent, FunctionDefinitionNode.class,
                ResourceDefinitionNode.class, ActionDefinitionNode.class);
        if (definitionNode == null) {
            return;
        }
        try {
            highlightExitPoints(definitionNode);
        } catch (AnalysisCanceledException e) {
            // ignore
        }
    }

    private void highlightExitPoints(final PsiElement body) throws AnalysisCanceledException {
        Collection<StatementNode> statementNodes = PsiTreeUtil.findChildrenOfAnyType(body, ReturnStatementNode.class,
                ThrowStatementNode.class);
        for (PsiElement e : statementNodes) {
            addOccurrence(e);
        }
        myStatusText = CodeInsightBundle.message("status.bar.exit.points.highlighted.message", statementNodes.size(),
                HighlightUsagesHandler.getShortcutText());
    }

    @Nullable
    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }
}
