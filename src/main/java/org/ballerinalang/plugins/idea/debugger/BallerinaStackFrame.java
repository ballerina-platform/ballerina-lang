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

import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.debugger.protocol.BallerinaAPI;
import org.ballerinalang.plugins.idea.debugger.protocol.BallerinaRequest;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;

public class BallerinaStackFrame extends XStackFrame {

    private final BallerinaDebugProcess myProcess;
    private final BallerinaAPI.Location myLocation;
    private final BallerinaCommandProcessor myProcessor;
    private final int myId;

    public BallerinaStackFrame(@NotNull BallerinaDebugProcess process, @NotNull BallerinaAPI.Location location,
                               @NotNull BallerinaCommandProcessor processor, int id) {
        myProcess = process;
        myLocation = location;
        myProcessor = processor;
        myId = id;
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new XDebuggerEvaluator() {
            @Override
            public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback,
                                 @Nullable XSourcePosition expressionPosition) {
                myProcessor.send(new BallerinaRequest.EvalSymbol(expression, myId))
                        .done(variable -> callback.evaluated(createXValue(variable, AllIcons.Debugger.Watch)))
                        .rejected(throwable -> callback.errorOccurred(throwable.getMessage()));
            }

            @Nullable
            private PsiElement findElementAt(@Nullable PsiFile file, int offset) {
                return file != null ? file.findElementAt(offset) : null;
            }

            @Nullable
            @Override
            public TextRange getExpressionRangeAtOffset(@NotNull Project project, @NotNull Document document,
                                                        int offset, boolean sideEffectsAllowed) {
                Ref<TextRange> currentRange = Ref.create(null);
                PsiDocumentManager.getInstance(project).commitAndRunReadAction(() -> {
                    try {
                        PsiElement elementAtCursor = findElementAt(PsiDocumentManager.getInstance(project).getPsiFile
                                (document), offset);
                        //                        GoTypeOwner e = PsiTreeUtil.getParentOfType(elementAtCursor,
                        //                                GoExpression.class,
                        //                                GoVarDefinition.class,
                        //                                GoConstDefinition.class,
                        //                                GoParamDefinition.class);
                        //                        if (e != null) {
                        //                            currentRange.set(e.getTextRange());
                        //                        }
                    } catch (IndexNotReadyException ignored) {
                    }
                });
                return currentRange.get();
            }
        };
    }

    @NotNull
    private XValue createXValue(@NotNull BallerinaAPI.Variable variable, @Nullable Icon icon) {
        return new BallerinaXValue(myProcess, variable, myProcessor, myId, icon);
    }

    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        VirtualFile file = findFile();
        return file == null ? null : XDebuggerUtil.getInstance().createPosition(file, myLocation.line - 1);
    }

    @Nullable
    private VirtualFile findFile() {
        String url = myLocation.file;
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(url);
        if (file == null && SystemInfo.isWindows) {
            Project project = myProcess.getSession().getProject();
            RunProfile profile = myProcess.getSession().getRunProfile();
            Module module = profile instanceof ModuleBasedConfiguration ? ((ModuleBasedConfiguration) profile)
                    .getConfigurationModule().getModule() : null;
            String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(module);
            if (sdkHomePath == null) return null;
            String newUrl = StringUtil.replaceIgnoreCase(url, "c:/go", sdkHomePath);
            return LocalFileSystem.getInstance().findFileByPath(newUrl);
        }
        return file;
    }

    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        super.customizePresentation(component);
        component.append(" at " + myLocation.function.name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        component.setIcon(AllIcons.Debugger.StackFrame);
    }

    @NotNull
    private <T> Promise<T> send(@NotNull BallerinaRequest<T> request) {
        return BallerinaDebugProcess.send(request, myProcessor);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        send(new BallerinaRequest.ListLocalVars(myId)).done(variables -> {
            XValueChildrenList xVars = new XValueChildrenList(variables.size());
            for (BallerinaAPI.Variable v : variables) xVars.add(v.name, createXValue(v, BallerinaIcons.VARIABLE));
            send(new BallerinaRequest.ListFunctionArgs(myId)).done(args -> {
                for (BallerinaAPI.Variable v : args) xVars.add(v.name, createXValue(v, BallerinaIcons.PARAMETER));
                node.addChildren(xVars, true);
            });
        });
    }
}
