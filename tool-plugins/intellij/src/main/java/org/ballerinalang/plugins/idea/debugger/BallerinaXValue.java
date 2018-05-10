/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.plugins.idea.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ThreeState;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XInlineDebuggerDataCallback;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XNavigatable;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import org.ballerinalang.plugins.idea.debugger.dto.Variable;
import org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;

/**
 * Represents a value in the debug window.
 */
public class BallerinaXValue extends XNamedValue {

    @NotNull
    private final BallerinaDebugProcess myProcess;
    @NotNull
    private final Variable myVariable;
    @NotNull
    private final String myFrameName;
    @Nullable
    private final Icon myIcon;

    BallerinaXValue(@NotNull BallerinaDebugProcess process, @NotNull String frameName,
                    @NotNull Variable variable, @Nullable Icon icon) {
        super(variable.getName());
        myProcess = process;
        myFrameName = frameName;
        myVariable = variable;
        myIcon = icon;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        XValuePresentation presentation = getPresentation();
        boolean hasChildren = false;
        if (myVariable.getValue() == null && myVariable.getChildren() != null) {
            hasChildren = true;
        }
        node.setPresentation(myIcon, presentation, hasChildren);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        List<Variable> children = myVariable.getChildren();
        if (children == null) {
            super.computeChildren(node);
        } else {
            XValueChildrenList list = new XValueChildrenList();
            for (Variable child : children) {
                list.add(child.getName(), new BallerinaXValue(myProcess, myFrameName, child, AllIcons.Nodes.Field));
            }
            node.addChildren(list, true);
        }
    }

    @Nullable
    @Override
    public XValueModifier getModifier() {
        return null;
    }

    @NotNull
    private XValuePresentation getPresentation() {
        String value = myVariable.getValue();
        if (value == null) {
            return new XRegularValuePresentation(myFrameName, "Scope");
        }
        if (myVariable.isNumber()) {
            return new XNumericValuePresentation(value);
        }
        if (myVariable.isString()) {
            return new XStringValuePresentation(value);
        }
        if (myVariable.isBoolean()) {
            return new XValuePresentation() {
                @Override
                public void renderValue(@NotNull XValueTextRenderer renderer) {
                    renderer.renderValue(value, BallerinaSyntaxHighlightingColors.KEYWORD);
                }
            };
        }

        String type = myVariable.getType();
        String prefix = myVariable.getType() + " ";
        return new XRegularValuePresentation(StringUtil.startsWith(value, prefix) ? value.replaceFirst(Pattern.quote
                (prefix), "") : value, type);
    }

    @Nullable
    private static PsiElement findTargetElement(@NotNull Project project, @NotNull XSourcePosition position,
                                                @NotNull Editor editor, @NotNull String name) {
        // Todo
        return null;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        readActionInPooledThread(new Runnable() {

            @Override
            public void run() {
                navigatable.setSourcePosition(findPosition());
            }

            @Nullable
            private XSourcePosition findPosition() {
                XDebugSession debugSession = myProcess.getSession();
                if (debugSession == null) {
                    return null;
                }
                XStackFrame stackFrame = debugSession.getCurrentStackFrame();
                if (stackFrame == null) {
                    return null;
                }
                Project project = debugSession.getProject();
                XSourcePosition position = debugSession.getCurrentPosition();
                Editor editor = ((FileEditorManagerImpl) FileEditorManager.getInstance(project))
                        .getSelectedTextEditor(true);
                if (editor == null || position == null) {
                    return null;
                }
                String name = myName.startsWith("&") ? myName.replaceFirst("\\&", "") : myName;
                PsiElement resolved = findTargetElement(project, position, editor, name);
                if (resolved == null) {
                    return null;
                }
                VirtualFile virtualFile = resolved.getContainingFile().getVirtualFile();
                return XDebuggerUtil.getInstance().createPositionByOffset(virtualFile, resolved.getTextOffset());
            }
        });
    }

    private static void readActionInPooledThread(@NotNull Runnable runnable) {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                ApplicationManager.getApplication().runReadAction(runnable));
    }

    @NotNull
    @Override
    public ThreeState computeInlineDebuggerData(@NotNull XInlineDebuggerDataCallback callback) {
        computeSourcePosition(callback::computed);
        return ThreeState.YES;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public boolean canNavigateToTypeSource() {
        // Todo
        return false;
    }

    @Override
    public void computeTypeSourcePosition(@NotNull XNavigatable navigatable) {
        // Todo
    }
}
