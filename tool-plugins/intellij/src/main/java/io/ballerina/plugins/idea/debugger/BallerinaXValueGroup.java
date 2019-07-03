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
 */

package io.ballerina.plugins.idea.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueGroup;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

/**
 * Represents a variable group in debug window.
 */
public class BallerinaXValueGroup extends XValueGroup {

    private final BallerinaDebugProcess process;
    private final StackFrame stackFrame;
    private final Variable variable;

    protected BallerinaXValueGroup(@NotNull BallerinaDebugProcess myProcess, @NotNull StackFrame myFrame,
                                   @NotNull String name, @NotNull Variable myVariable) {
        super(name);
        this.variable = myVariable;
        this.process = myProcess;
        this.stackFrame = myFrame;
    }

    @Override
    public boolean isRestoreExpansion() {
        return true;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.Debugger.Value;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        // Todo - Add child variable fetching.
        List<Variable> children = new ArrayList<>();
        if (children == null || children.isEmpty()) {
            super.computeChildren(node);
        } else {
            XValueChildrenList list = new XValueChildrenList();
            for (Variable child : children) {
                list.add(child.getName(), new BallerinaXValue(process, stackFrame.getName(), child,
                        AllIcons.Nodes.Field));
            }
            node.addChildren(list, true);
        }
    }
}
