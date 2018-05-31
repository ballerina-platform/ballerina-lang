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
import io.ballerina.plugins.idea.debugger.dto.Frame;
import io.ballerina.plugins.idea.debugger.dto.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.Icon;

/**
 * Represents a variable group in debug window.
 */
public class BallerinaXValueGroup extends XValueGroup {

    private final BallerinaDebugProcess myProcess;
    private final Frame myFrame;
    private final Variable myVariable;

    protected BallerinaXValueGroup(@NotNull BallerinaDebugProcess myProcess, @NotNull Frame myFrame,
                                   @NotNull String name, @NotNull Variable myVariable) {
        super(name);
        this.myVariable = myVariable;
        this.myProcess = myProcess;
        this.myFrame = myFrame;
    }

    // Todo - Uncomment after required IDEA version is changed to 2017.
    //    @Override
    //    public boolean isRestoreExpansion() {
    //        return true;
    //    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.Debugger.Value;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        List<Variable> children = myVariable.getChildren();
        if (children == null) {
            super.computeChildren(node);
        } else {
            XValueChildrenList list = new XValueChildrenList();
            for (Variable child : children) {
                list.add(child.getName(), new BallerinaXValue(myProcess, myFrame.getFrameName(), child,
                        AllIcons.Nodes.Field));
            }
            node.addChildren(list, true);
        }
    }
}
