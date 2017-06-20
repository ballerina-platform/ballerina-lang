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

package org.ballerinalang.plugins.idea.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.ballerinalang.plugins.idea.debugger.dto.Frame;
import org.ballerinalang.plugins.idea.debugger.dto.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BallerinaStackFrame extends XStackFrame {

    private final BallerinaDebugProcess myProcess;
    private final Frame myFrame;

    BallerinaStackFrame(@NotNull BallerinaDebugProcess process, @NotNull Frame frame) {
        myProcess = process;
        myFrame = frame;
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        // Todo - Add evaluator support
        return null;
    }

    /**
     * Returns the source position. This is used to show the debug hit in the file.
     */
    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        VirtualFile file = findFile();
        return file == null ? null : XDebuggerUtil.getInstance().createPosition(file, myFrame.getLineID() - 1);
    }

    @Nullable
    private VirtualFile findFile() {
        String relativePath = myFrame.getFileName();
        Project project = myProcess.getSession().getProject();
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        VirtualFile file = null;
        for (VirtualFile contentRoot : contentRoots) {
            String absolutePath = contentRoot.getPath() + "/" + relativePath;
            file = LocalFileSystem.getInstance().findFileByPath(absolutePath);
            if (file != null) {
                break;
            }
        }

        // Todo - Temp fix. Might show the wrong file if more than one file have the same name.
        //        if (file == null) {
        // If a file is not found, that can mean the file did not had any package declaration, so it was ran as a
        // file, not a package.
        //            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        //            file = getVirtualFile(relativePath, myFrame.getPackageName(), scope);
        //        }
        // Todo - Check files in the SDK sources
        return file;
    }

    //    private VirtualFile getVirtualFile(String relativePath, String packageName, GlobalSearchScope scope) {
    //        List<VirtualFile> matchingFiles = new LinkedList<>();
    //        ApplicationManager.getApplication().executeOnPooledThread(
    //                () -> ApplicationManager.getApplication().runReadAction(
    //                        () -> {
    //                            for (VirtualFile virtualFile : FileTypeIndex.getFiles(BallerinaFileType.INSTANCE,
    // scope)) {
    //                                if (virtualFile.getName().equals(relativePath)) {
    //                                    matchingFiles.add(virtualFile);
    //                                }
    //                            }
    //
    //
    //                        }
    //                )
    //        );
    //        if (matchingFiles.isEmpty()) {
    //            return null;
    //        }
    //        if (matchingFiles.size() == 1) {
    //            return matchingFiles.get(0);
    //        }
    //        for (VirtualFile matchingFile : matchingFiles) {
    //
    //            PsiFile psiFile = PsiManager.getInstance(myProcess.getSession().getProject()).findFile(matchingFile);
    //            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(psiFile,
    //                    PackageDeclarationNode.class);
    //            if (packageDeclarationNode != null) {
    //                if (".".equals(packageName)) {
    //                    continue;
    //                }
    //
    //                PackagePathNode packagePathNode = PsiTreeUtil.getChildOfType(packageDeclarationNode,
    //                        PackagePathNode.class);
    //                if (packagePathNode != null) {
    //                    if (packageName.equals(packagePathNode.getText())) {
    //                        return matchingFile;
    //                    }
    //                }
    //            }
    //        }
    //        return null;
    //    }

    /**
     * Customizes the stack name in the Frames sub window in Debug window.
     */
    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        super.customizePresentation(component);
        component.append(" at ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        component.append(myFrame.getFrameName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        component.setIcon(AllIcons.Debugger.StackFrame);
    }

    /**
     * Adds variables in the current stack to the node.
     */
    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        // We categorize variables according to the scope. But we get all the variables in the stack. So we need to
        // distinguish values in each scope. In this Map, key will be the scope name. Value will be the list of
        // variables in that scope.
        Map<String, List<Variable>> scopeMap = new HashMap<>();
        // Iterate through each variable.
        List<Variable> variables = myFrame.getVariables();
        for (Variable variable : variables) {
            // Get the scope.
            String scopeName = variable.getScope();
            // Check whether the scope is already available in the map.
            if (scopeMap.containsKey(scopeName)) {
                // If it is already in the map, add the variable to the corresponding list.
                List<Variable> list = scopeMap.get(scopeName);
                list.add(variable);
            } else {
                // If it is not available in the map, add it as a new entry.
                List<Variable> list = new LinkedList<>();
                list.add(variable);
                scopeMap.put(scopeName, list);
            }
        }

        // Iterate through each scope in the map.
        scopeMap.forEach((scopeName, variableList) -> {
            // Create a new XValueChildrenList to hold the XValues.
            XValueChildrenList xValueChildrenList = new XValueChildrenList(variableList.size());
            // Create a new variable to represent the scope.
            Variable scopeVariable = new Variable();
            // Set the variable name.
            scopeVariable.setName(scopeName);
            // Set the children.
            scopeVariable.setChildren(variableList);
            // Create a new XValue.
            BallerinaXValue ballerinaXValue = new BallerinaXValue(myProcess, myFrame.getFrameName(), scopeVariable,
                    AllIcons.Debugger.Value);
            // Add the XValue to the list.
            xValueChildrenList.add(scopeName, ballerinaXValue);
            // Add the list to the node as children.
            node.addChildren(xValueChildrenList, true);
        });
    }
}
