package io.ballerina.plugins.idea.ui.preview;

import com.intellij.CommonBundle;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import io.ballerina.plugins.idea.ui.settings.MarkdownApplicationSettings;
import io.ballerina.plugins.idea.ui.settings.MarkdownPreviewSettings;
import io.ballerina.plugins.idea.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

//import org.owasp.html.HtmlPolicyBuilder;
//import org.owasp.html.PolicyFactory;
//import org.owasp.html.Sanitizers;

public class BallerinaDiagramVisualizer extends UserDataHolderBase implements FileEditor {
    private final static long PARSING_CALL_TIMEOUT_MS = 50L;

    private final static long RENDERING_DELAY_MS = 20L;

    @NotNull
    private final JPanel myHtmlPanelWrapper;
    @Nullable
    private MarkdownHtmlPanelProvider.ProviderInfo myLastPanelProviderInfo = null;
    @Nullable
    private MarkdownHtmlPanel myPanel;
    @NotNull
    private final VirtualFile myFile;
    @Nullable
    private final Document myDocument;
    @NotNull
    private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    @NotNull
    private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    private final Object REQUESTS_LOCK = new Object();
    //    @Nullable
    //    private Runnable myLastScrollRequest = null;
    @Nullable
    private Runnable myLastHtmlOrRefreshRequest = null;

    //private volatile int myLastScrollOffset;
    @NotNull
    private String myLastRenderedHtml =
            "<html>\n" + "<header><title>This is title</title></header>\n" + "<body>\n" + "Hello world\n" + "</body>\n"
                    + "</html>\n";
    ;

    public BallerinaDiagramVisualizer(@NotNull Project project, @NotNull VirtualFile file) {
        myFile = file;
        myDocument = FileDocumentManager.getInstance().getDocument(myFile);

        if (myDocument != null) {
            myDocument.addDocumentListener(new DocumentListener() {

                @Override
                public void beforeDocumentChange(DocumentEvent e) {
                    myPooledAlarm.cancelAllRequests();
                }

                @Override
                public void documentChanged(final DocumentEvent e) {
                    myPooledAlarm.addRequest(() -> {
                        //myLastScrollOffset = e.getOffset();
                        updateHtml(true);
                    }, PARSING_CALL_TIMEOUT_MS);
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
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                mySwingAlarm.addRequest(() -> {
                    if (myPanel == null) {
                        return;
                    }

                    detachHtmlPanel();
                }, 0, ModalityState.stateForComponent(getComponent()));
            }
        });

        if (isPreviewShown(project, file)) {
            attachHtmlPanel();
        }

        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        MarkdownApplicationSettings.SettingsChangedListener settingsChangedListener = new MyUpdatePanelOnSettingsChangedListener();
        settingsConnection
                .subscribe(MarkdownApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
    }

    private static boolean isPreviewShown(@NotNull Project project, @NotNull VirtualFile file) {
        BallerinaSplitEditorProvider provider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER
                .findExtension(BallerinaSplitEditorProvider.class);
        if (provider == null) {
            return true;
        }

        FileEditorState state = EditorHistoryManager.getInstance(project).getState(file, provider);
        if (!(state instanceof SplitFileEditor.MyFileEditorState)) {
            return true;
        }

        return SplitFileEditor.SplitEditorLayout.valueOf(((SplitFileEditor.MyFileEditorState) state).getSplitLayout())
                != SplitFileEditor.SplitEditorLayout.FIRST;
    }

    /**
     * Is always run from pooled thread
     */
    private void updateHtml(final boolean preserveScrollOffset) {
        if (myPanel == null) {
            return;
        }

        if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
            return;
        }

        final String html =
                "<html>\n" + "<header><title>This is title</title></header>\n" + "<body>\n" + "Hello world\n"
                        + "</body>\n" + "</html>\n";

        //MarkdownUtil.generateMarkdownHtml(myFile, myDocument.getText());

        // EA-75860: The lines to the top may be processed slowly; Since we're in pooled thread, we can be disposed already.
        if (!myFile.isValid() || Disposer.isDisposed(this)) {
            return;
        }

        synchronized (REQUESTS_LOCK) {
            if (myLastHtmlOrRefreshRequest != null) {
                mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest);
            }
            myLastHtmlOrRefreshRequest = () -> {
                if (myPanel == null) {
                    return;
                }

                final String currentHtml = "<html><head></head>" + html + "</html>";
                if (!currentHtml.equals(myLastRenderedHtml)) {
                    myLastRenderedHtml = currentHtml;
                    myPanel.setHtml(myLastRenderedHtml);
                }

                myPanel.render();
                synchronized (REQUESTS_LOCK) {
                    myLastHtmlOrRefreshRequest = null;
                }
            };
            mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest, RENDERING_DELAY_MS,
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
        return "Ballerina Diagram HTML Preview";
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
        myPooledAlarm.addRequest(() -> updateHtml(true), 0);
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
    private MarkdownHtmlPanelProvider retrievePanelProvider(@NotNull MarkdownApplicationSettings settings) {
        final MarkdownHtmlPanelProvider.ProviderInfo providerInfo = settings.getMarkdownPreviewSettings()
                .getHtmlPanelProviderInfo();

        MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(providerInfo);

        if (provider.isAvailable() != MarkdownHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            settings.setMarkdownPreviewSettings(
                    new MarkdownPreviewSettings(settings.getMarkdownPreviewSettings().getSplitEditorLayout(),
                            MarkdownPreviewSettings.DEFAULT.getHtmlPanelProviderInfo(), true, true));

            Messages.showMessageDialog(myHtmlPanelWrapper,
                    "Tried to use preview panel provider (" + providerInfo.getName()
                            + "), but it is unavailable. Reverting to default.", CommonBundle.getErrorTitle(),
                    Messages.getErrorIcon());

            provider = MarkdownHtmlPanelProvider.getProviders()[0];
        }

        myLastPanelProviderInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
        return provider;
    }

    private void detachHtmlPanel() {
        if (myPanel != null) {
            myHtmlPanelWrapper.remove(myPanel.getComponent());
            Disposer.dispose(myPanel);
            myPanel = null;
        }
    }

    private void attachHtmlPanel() {
        MarkdownApplicationSettings settings = new MarkdownApplicationSettings();
        myPanel = retrievePanelProvider(settings).createHtmlPanel();
        myHtmlPanelWrapper.add(myPanel.getComponent(), BorderLayout.CENTER);
        myHtmlPanelWrapper.repaint();
    }

    private class MyUpdatePanelOnSettingsChangedListener
            implements MarkdownApplicationSettings.SettingsChangedListener {
        @Override
        public void settingsChanged(@NotNull MarkdownApplicationSettings settings) {
            mySwingAlarm.addRequest(() -> {
                if (settings.getMarkdownPreviewSettings().getSplitEditorLayout()
                        != SplitFileEditor.SplitEditorLayout.FIRST) {
                    if (myPanel == null) {
                        attachHtmlPanel();
                    } else if (myLastPanelProviderInfo == null || MarkdownHtmlPanelProvider
                            .createFromInfo(myLastPanelProviderInfo).equals(retrievePanelProvider(settings))) {
                        detachHtmlPanel();
                        attachHtmlPanel();
                    }
                    myPanel.setHtml(myLastRenderedHtml);
                }
            }, 0, ModalityState.stateForComponent(getComponent()));
        }
    }
}
