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

package io.ballerina.plugins.idea.codeinspection;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.impl.DaemonListeners;
import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.BallerinaIcons;
import io.ballerina.plugins.idea.codeinsight.imports.BallerinaCodeInsightSettings;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import static io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil.LOCAL_PACKAGE_PLACEHOLDER;

/**
 * Quick fix which adds imports.
 */
public class BallerinaImportPackageQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement
        implements HintAction, HighPriorityAction {

    BallerinaImportPackageQuickFix(@NotNull BallerinaIdentifier identifier) {
        super(identifier);
    }

    @Override
    public boolean showHint(@NotNull Editor editor) {
        return doAutoImportOrShowHint(editor, true);
    }

    @NotNull
    @Override
    public String getText() {
        PsiElement element = getStartElement();
        if (element != null) {
            return "Import " + getText(getImportPathVariantsToImport(element));
        }
        return "Import package";
    }

    @NotNull
    private static String getText(@NotNull Collection<String> packagesToImport) {
        return ContainerUtil.getFirstItem(packagesToImport, "") + "? " + (packagesToImport.size() > 1 ?
                "(multiple choices...) " : "");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Import package";
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file,
                       @Nullable("is null when called from inspection") Editor editor,
                       @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
            return;
        }
        if (!(file instanceof BallerinaFile)) {
            return;
        }

        BallerinaFile ballerinaFile = ((BallerinaFile) file);

        PsiReference reference = startElement.getReference();
        if (reference != null && reference.resolve() != null) {
            return;
        }

        List<String> importPathVariantsToImport = getImportPathVariantsToImport(startElement);
        if (importPathVariantsToImport.size() == 1) {
            Runnable addImport = () -> BallerinaFile.addImport(ballerinaFile, importPathVariantsToImport.get(0), null);
            CommandProcessor.getInstance().runUndoTransparentAction(
                    () -> ApplicationManager.getApplication().runWriteAction(addImport)
            );
        } else {
            performImport(importPathVariantsToImport, ballerinaFile, editor);
        }
    }

    private List<String> getImportPathVariantsToImport(@NotNull PsiElement element) {
        List<String> results = new LinkedList<>();

        Project project = element.getProject();
        Module module = ModuleUtilCore.findModuleForPsiElement(element);

        Map<String, List<String>> allPackages = BallerinaPsiImplUtil.getAllPackagesInResolvableScopes(project, module);
        for (String packageName : allPackages.keySet()) {
            if (packageName.equals(element.getText())) {
                List<String> organizations = allPackages.get(packageName);
                for (String organization : organizations) {
                    String importPath;
                    if (organization.equals(LOCAL_PACKAGE_PLACEHOLDER)) {
                        importPath = packageName;
                    } else {
                        importPath = organization + "/" + packageName;
                    }
                    results.add(importPath);
                }
            }
        }
        return results;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement,
                               @NotNull PsiElement endElement) {
        // We only perform this action on Ballerina modules since this might cause issues in other modules.
        Module module = ModuleUtil.findModuleForFile(file.getVirtualFile(), file.getProject());
        boolean isBallerinaModule = BallerinaSdkService.getInstance(project).isBallerinaModule(module);
        if (!isBallerinaModule) {
            return false;
        }
        // If the file contain error elements, do not perform auto import. Since this might affect badly to the user
        // experience.
        Collection<PsiErrorElement> errorElements = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        if (!errorElements.isEmpty()) {
            return false;
        }

        PsiReference reference = startElement.getReference();
        return file instanceof BallerinaFile && file.getManager().isInProject(file) &&
                (reference == null || reference.resolve() == null);
    }

    private boolean doAutoImportOrShowHint(@NotNull Editor editor, boolean showHint) {
        PsiElement element = getStartElement();
        if (element == null || !element.isValid()) {
            return false;
        }

        PsiReference reference = element.getReference();
        if (reference != null && reference.resolve() != null) {
            return false;
        }
        List<String> packagesToImport = getImportPathVariantsToImport(element);
        if (packagesToImport.isEmpty()) {
            return false;
        }

        PsiFile file = element.getContainingFile();
        if (!(file instanceof BallerinaFile)) {
            return false;
        }

        BallerinaFile ballerinaFile = ((BallerinaFile) file);

        String firstPackageToImport = ContainerUtil.getFirstItem(packagesToImport);

        // autoimport on trying to fix
        if (packagesToImport.size() == 1) {
            if (BallerinaCodeInsightSettings.getInstance().isAddUnambiguousImportsOnTheFly()) {
                if (!LaterInvocator.isInModalContext() && (ApplicationManager.getApplication().isUnitTestMode() ||
                        DaemonListeners.canChangeFileSilently(file))) {
                    if (reference == null || reference.resolve() == null) {
                        performImport(((BallerinaFile) file), firstPackageToImport);
                    }
                    return false;
                }
            }
        }

        // show hint on failed autoimport
        if (showHint) {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                return false;
            }
            if (HintManager.getInstance().hasShownHintsThatWillHideByOtherHint(true)) {
                return false;
            }
            if (!BallerinaCodeInsightSettings.getInstance().isShowImportPopup()) {
                return false;
            }
            TextRange referenceRange = element.getTextRange();
            String message = ShowAutoImportPass.getMessage(packagesToImport.size() > 1,
                    ContainerUtil.getFirstItem(packagesToImport)
            );
            HintManager.getInstance().showQuestionHint(editor, message, referenceRange.getStartOffset(),
                    referenceRange.getEndOffset(), () -> {
                        if (ballerinaFile.isValid() && !editor.isDisposed()) {
                            performImport(packagesToImport, ballerinaFile, editor);
                        }
                        return true;
                    }
            );
            return true;
        }
        return false;
    }

    private void performImport(@NotNull List<String> packagesToImport, @NotNull BallerinaFile file,
                               @Nullable Editor editor) {
        if (packagesToImport.size() > 1 && editor != null) {
            JBList<String> list = new JBList<>(packagesToImport);
            list.installCellRenderer(o -> {
                JBLabel label = new JBLabel(o.toString(), BallerinaIcons.PACKAGE, SwingConstants.LEFT);
                label.setBorder(IdeBorderFactory.createEmptyBorder(2, 4, 2, 4));
                return label;
            });
            PopupChooserBuilder popupChooserBuilder = JBPopupFactory.getInstance().createListPopupBuilder(list)
                    .setRequestFocus(true)
                    .setTitle("Package to import")
                    .setItemChoosenCallback(() -> {
                        int i = list.getSelectedIndex();
                        if (i < 0) {
                            return;
                        }
                        performImport(file, packagesToImport.get(i));
                    })
                    .setFilteringEnabled(item -> item instanceof String ? (String) item : item.toString());
            JBPopup popup = popupChooserBuilder.createPopup();
            popupChooserBuilder.getScrollPane().setBorder(null);
            popupChooserBuilder.getScrollPane().setViewportBorder(null);
            popup.showInBestPositionFor(editor);
        }
    }

    private void performImport(BallerinaFile file, String importPath) {
        // Important: Need to call runWriteAction since this is called from the swing thread.
        Runnable addImport = () -> BallerinaFile.addImport(file, importPath, null);
        CommandProcessor.getInstance().runUndoTransparentAction(
                () -> ApplicationManager.getApplication().runWriteAction(addImport)
        );
    }
}
