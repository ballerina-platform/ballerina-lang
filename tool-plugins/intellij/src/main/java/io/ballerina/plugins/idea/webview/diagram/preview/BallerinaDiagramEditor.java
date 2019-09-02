/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.webview.diagram.preview;

import com.google.common.base.Strings;
import com.intellij.CommonBundle;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import io.ballerina.plugins.idea.webview.diagram.preview.javafx.DiagramJavaFxHtmlPanel;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramApplicationSettings;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramCssSettings;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramPreviewSettings;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import netscape.javascript.JSException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Ballerina diagram editor implementation.
 */
public class BallerinaDiagramEditor extends UserDataHolderBase implements FileEditor {

    private static final Logger LOG = Logger.getInstance(BallerinaDiagramEditor.class);

    private static final long DEBOUNCE_DELAY_MS = 400L;
    private static final long RENDERING_DELAY_MS = 20L;

    @NotNull
    private final JPanel myHtmlPanelWrapper;
    @Nullable
    private DiagramJavaFxHtmlPanel myPanel;
    @Nullable
    private HtmlPanelProvider.ProviderInfo myLastPanelProviderInfo = null;
    @NotNull
    private final VirtualFile myFile;
    @Nullable
    private final Document myDocument;
    @NotNull
    private final Project myProject;
    @NotNull
    private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    @NotNull
    private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
    @NotNull
    private final Object requestsLock = new Object();
    @Nullable
    private Runnable myLastScrollRequest = null;
    @Nullable
    private Runnable myLastHtmlOrRefreshRequest = null;
    @NotNull
    private String myLastRenderedHtml = "";

    private boolean isHidden = true;
    private volatile int myLastScrollOffset;

    BallerinaDiagramEditor(@NotNull Project project, @NotNull VirtualFile file) {
        myFile = file;
        myDocument = FileDocumentManager.getInstance().getDocument(myFile);
        myProject = project;

        if (myDocument != null) {
            myDocument.addDocumentListener(new DocumentListener() {
                @Override
                public void beforeDocumentChange(@NotNull DocumentEvent e) {
                    myPooledAlarm.cancelAllRequests();
                }

                @Override
                public void documentChanged(@NotNull final DocumentEvent e) {
                    if (!isHidden) {
                        myPooledAlarm.addRequest(() -> updateHtml(false), DEBOUNCE_DELAY_MS);
                    }
                }
            }, this);
        }

        myHtmlPanelWrapper = new JPanel(new BorderLayout());
        myHtmlPanelWrapper.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                mySwingAlarm.addRequest(() -> {
                    if (myPanel != null) {
                        return;
                    }
                    attachHtmlPanel();
                }, 0, ModalityState.stateForComponent(getComponent()));
                myPooledAlarm.addRequest(() -> updateHtml(false), DEBOUNCE_DELAY_MS);
                isHidden = false;
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                if (mySwingAlarm.isDisposed()) {
                    LOG.warn("Swing Alarm is already disposed. Unable to detach Ballerina Diagram HTML panel");
                    return;
                }
                mySwingAlarm.addRequest(() -> {
                    if (myPanel == null) {
                        return;
                    }
                    detachHtmlPanel();
                }, 0, ModalityState.stateForComponent(getComponent()));
                isHidden = true;
                myLastRenderedHtml = "";
            }
        });

        if (isPreviewShown(project, file)) {
            attachHtmlPanel();
        }

        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        DiagramApplicationSettings.SettingsChangedListener settingsChangedListener =
                new MyUpdatePanelOnSettingsChangedListener();
        settingsConnection.subscribe(DiagramApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
    }

    private void scrollToSrcOffset(final int offset) {
        if (myPanel == null) {
            return;
        }

        // Do not scroll if html update request is online
        // This will restrain preview from glitches on editing
        if (!myPooledAlarm.isEmpty()) {
            myLastScrollOffset = offset;
            return;
        }

        synchronized (requestsLock) {
            if (myLastScrollRequest != null) {
                mySwingAlarm.cancelRequest(myLastScrollRequest);
            }
            myLastScrollRequest = () -> {
                if (myPanel == null) {
                    return;
                }

                myLastScrollOffset = offset;
                synchronized (requestsLock) {
                    myLastScrollRequest = null;
                }
            };
            mySwingAlarm.addRequest(myLastScrollRequest, RENDERING_DELAY_MS,
                    ModalityState.stateForComponent(getComponent()));
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myHtmlPanelWrapper;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        if (myPanel == null) {
            return null;
        }
        return myPanel.getComponent();
    }

    @NotNull
    @Override
    public String getName() {
        return "Diagram HTML Preview";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {
        if (myPanel == null) {
            return;
        }
        myPooledAlarm.cancelAllRequests();
        myPooledAlarm.addRequest(() -> updateHtml(true), DEBOUNCE_DELAY_MS);
    }

    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        if (myPanel == null) {
            return;
        }
        Disposer.dispose(myPanel);
    }

    @NotNull
    private HtmlPanelProvider retrievePanelProvider(@NotNull DiagramApplicationSettings settings) {
        final HtmlPanelProvider.ProviderInfo providerInfo = settings.getDiagramPreviewSettings()
                .getHtmlPanelProviderInfo();

        HtmlPanelProvider provider = HtmlPanelProvider.createFromInfo(providerInfo);

        if (provider.isAvailable() != HtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            settings.setDiagramPreviewSettings(
                    new DiagramPreviewSettings(settings.getDiagramPreviewSettings().getSplitEditorLayout(),
                            DiagramPreviewSettings.DEFAULT.getHtmlPanelProviderInfo(),
                            settings.getDiagramPreviewSettings().isUseGrayscaleRendering(),
                            settings.getDiagramPreviewSettings().isAutoScrollPreview()));

            Messages.showMessageDialog(myHtmlPanelWrapper,
                    "Tried to use preview panel provider (" + providerInfo.getName()
                            + "), but it is unavailable. Reverting to default.", CommonBundle.getErrorTitle(),
                    Messages.getErrorIcon());

            provider = HtmlPanelProvider.getProviders()[0];
        }

        myLastPanelProviderInfo = settings.getDiagramPreviewSettings().getHtmlPanelProviderInfo();
        return provider;
    }

    /**
     * Is always run from pooled thread.
     */
    private void updateHtml(boolean preserveScrollOffset) {
        if (myPanel == null) {
            return;
        }

        if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
            return;
        }

        String html = "";
        if (Strings.isNullOrEmpty(myLastRenderedHtml)) {
            html = BallerinaDiagramUtils.generateDiagramHtml(myFile, myPanel, myProject);
        }

        // EA-75860: The lines to the top may be processed slowly;
        // Since we're in a pooled thread, we can be disposed already.
        if (!myFile.isValid() || Disposer.isDisposed(this)) {
            return;
        }

        synchronized (requestsLock) {
            if (myLastHtmlOrRefreshRequest != null) {
                mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest);
            }

            // If the diagram HTML is already loaded, just invoke "drawDiagram()" function to update the webview.
            if (!Strings.isNullOrEmpty(myLastRenderedHtml)) {
                myLastHtmlOrRefreshRequest = () -> {
                    if (myPanel == null) {
                        return;
                    }
                    try {
                        myPanel.runInPlatformWhenAvailable(() ->
                                myPanel.getWebview().getEngine().executeScript("window.updateAST();")
                        );
                    } catch (JSException e) {
                        LOG.warn("Javascript error Occurred.", e);
                    } finally {
                        synchronized (requestsLock) {
                            myLastHtmlOrRefreshRequest = null;
                        }
                    }
                };
            } else {
                String finalHtml = html;
                myLastHtmlOrRefreshRequest = () -> {
                    if (myPanel == null) {
                        return;
                    }
                    try {
                        if (!finalHtml.equals(myLastRenderedHtml) && !finalHtml.isEmpty()) {
                            myLastRenderedHtml = finalHtml;
                            myPanel.setHtml(myLastRenderedHtml);
                            if (preserveScrollOffset) {
                                scrollToSrcOffset(myLastScrollOffset);
                            }
                            myPanel.render();
                        }
                    } catch (Exception e) {
                        LOG.warn("Error occurred when HTML rendering.", e);
                    } finally {
                        synchronized (requestsLock) {
                            myLastHtmlOrRefreshRequest = null;
                        }
                    }
                };
            }
            mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest, RENDERING_DELAY_MS,
                    ModalityState.stateForComponent(getComponent()));
        }
    }

    private void detachHtmlPanel() {
        if (myPanel != null) {
            myHtmlPanelWrapper.remove(myPanel.getComponent());
            Disposer.dispose(myPanel);
            myPanel = null;
        }
    }

    private void attachHtmlPanel() {
        DiagramApplicationSettings settings = DiagramApplicationSettings.getInstance();
        myPanel = (DiagramJavaFxHtmlPanel) retrievePanelProvider(settings).createHtmlPanel(myProject, myFile);
        myHtmlPanelWrapper.add(myPanel.getComponent(), BorderLayout.CENTER);
        myHtmlPanelWrapper.repaint();
    }

    private static void updatePanelCssSettings(@NotNull DiagramHtmlPanel panel,
                                               @NotNull final DiagramCssSettings cssSettings) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        final String inlineCss = cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() : null;
        final String customCssURI = cssSettings.isUriEnabled() ?
                cssSettings.getStylesheetUri() :
                DiagramCssSettings.getDefaultCssSettings(UIUtil.isUnderDarcula()).getStylesheetUri();

        panel.setCSS(inlineCss, customCssURI);
        panel.render();
    }

    private static boolean isPreviewShown(@NotNull Project project, @NotNull VirtualFile file) {
        BallerinaSplitEditorProvider provider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER
                .findExtension(BallerinaSplitEditorProvider.class);
        if (provider == null) {
            return true;
        }

        FileEditorState state = EditorHistoryManager.getInstance(project).getState(file, provider);
        if (state == null) {
            return false;
        }
        if (!(state instanceof SplitFileEditor.MyFileEditorState)) {
            return true;
        }
        return SplitFileEditor.SplitEditorLayout.valueOf(((SplitFileEditor.MyFileEditorState) state).getSplitLayout())
                != SplitFileEditor.SplitEditorLayout.FIRST;
    }

    private class MyUpdatePanelOnSettingsChangedListener implements DiagramApplicationSettings.SettingsChangedListener {
        @Override
        public void settingsChanged(@NotNull DiagramApplicationSettings settings) {
            mySwingAlarm.addRequest(() -> {
                if (settings.getDiagramPreviewSettings().getSplitEditorLayout()
                        != SplitFileEditor.SplitEditorLayout.FIRST) {
                    if (myPanel == null) {
                        attachHtmlPanel();
                    } else if (myLastPanelProviderInfo == null || HtmlPanelProvider
                            .createFromInfo(myLastPanelProviderInfo).equals(retrievePanelProvider(settings))) {
                        detachHtmlPanel();
                        attachHtmlPanel();
                    }

                    myPanel.setHtml(myLastRenderedHtml);
                    updatePanelCssSettings(myPanel, settings.getDiagramCssSettings());
                }
            }, 0, ModalityState.stateForComponent(getComponent()));
        }
    }
}
