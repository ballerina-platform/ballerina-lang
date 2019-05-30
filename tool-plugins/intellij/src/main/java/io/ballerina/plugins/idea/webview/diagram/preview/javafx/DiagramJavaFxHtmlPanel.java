package io.ballerina.plugins.idea.webview.diagram.preview.javafx;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.ui.javafx.JavaFxHtmlPanel;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.webview.diagram.preview.BallerinaDiagramUtils;
import io.ballerina.plugins.idea.webview.diagram.preview.DiagramHtmlPanel;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramApplicationSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.input.KeyCode;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;


/**
 * JavaFx based diagram panel implementation.
 */
public class DiagramJavaFxHtmlPanel extends JavaFxHtmlPanel implements DiagramHtmlPanel {

    private static final Logger LOG = Logger.getInstance(DiagramJavaFxHtmlPanel.class);

    @NotNull
    private String myCSP = "";
    @NotNull
    private String myLastRawHtml = "";
    @NotNull
    private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
    @NotNull
    private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();
    private static Project myProject;
    private static VirtualFile myFile;

    DiagramJavaFxHtmlPanel(@NotNull Project project, @NotNull VirtualFile file) {
        super();
        myProject = project;
        myFile = file;
        runInPlatformWhenAvailable(() -> {
            if (myWebView != null) {
                updateFontSmoothingType(myWebView, true);
            }
        });
        subscribeForGrayscaleSetting();
    }

    public WebView getWebview() {
        return super.getWebViewGuaranteed();
    }

    public void runInPlatformWhenAvailable(@NotNull Runnable runnable) {
        super.runInPlatformWhenAvailable(runnable);
    }

    @Override
    protected void registerListeners(@NotNull WebEngine engine) {
        engine.getLoadWorker().stateProperty().addListener(myBridgeSettingListener);
        engine.getLoadWorker().stateProperty().addListener(myScrollPreservingListener);

        // Triggers firebug console when pressing "SHIFT + I".
        if (myWebView != null) {
            myWebView.setOnKeyPressed(event -> {
                if (event.isShiftDown() && event.getCode() == KeyCode.I) {
                    engine.executeScript("enableFireBug()");
                }
            });
        }
    }

    private void subscribeForGrayscaleSetting() {
        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        DiagramApplicationSettings.SettingsChangedListener settingsChangedListener =
                new DiagramApplicationSettings.SettingsChangedListener() {
                    @Override
                    public void beforeSettingsChanged(@NotNull final DiagramApplicationSettings settings) {
                        runInPlatformWhenAvailable(() -> {
                            if (myWebView != null) {
                                updateFontSmoothingType(myWebView, true);
                            }
                        });
                    }
                };
        settingsConnection.subscribe(DiagramApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);
    }

    private static void updateFontSmoothingType(@NotNull WebView view, boolean isGrayscale) {
        final FontSmoothingType typeToSet;
        if (isGrayscale) {
            typeToSet = FontSmoothingType.GRAY;
        } else {
            typeToSet = FontSmoothingType.LCD;
        }
        view.fontSmoothingTypeProperty().setValue(typeToSet);
    }

    @Override
    public void setHtml(@NotNull String html) {
        myLastRawHtml = html;
        super.setHtml(html);
    }

    @Override
    public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {

    }

    @NotNull
    @Override
    protected String prepareHtml(@NotNull String html) {
        return ImageRefreshFix.setStamps(html.replace("<head>",
                String.format("<head><meta http-equiv=\"Content-Security-Policy\" content=\"%s\"/>", myCSP)));
    }

    @Override
    public void dispose() {
        runInPlatformWhenAvailable(() -> {
            getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty()
                    .removeListener(myScrollPreservingListener);
            getWebViewGuaranteed().getEngine().getLoadWorker().stateProperty().removeListener(myBridgeSettingListener);
        });
    }

    /**
     * DiagramPanelBridge.
     */
    @SuppressWarnings("unused")
    public static class DiagramPanelBridge {
        static final DiagramPanelBridge INSTANCE = new DiagramPanelBridge();
        private static final NotificationGroup DIAGRAM_NOTIFICATION_GROUP = NotificationGroup
                .toolWindowGroup("Diagram headers group", ToolWindowId.MESSAGES_WINDOW);

        public void handleDiagramEdit(String eventData) {
            try {
                JsonParser parser = new JsonParser();
                JsonObject didChangeParams = parser.parse(eventData).getAsJsonObject();
                String uri = didChangeParams.get("textDocumentIdentifier").getAsJsonObject().get("uri").getAsString();
                JsonObject ast = didChangeParams.get("ast").getAsJsonObject();

                // Requests the AST from the Ballerina language server.
                Editor editor = BallerinaDiagramUtils.getEditorFor(myFile, myProject);
                EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
                BallerinaEditorEventManager editorManager = (BallerinaEditorEventManager) manager;
                if (editorManager == null) {
                    LOG.debug(String.format("Editor event manager is null for: %s", editor.toString()));
                    return;
                }

                // Notify AST change to the language server.
                BallerinaASTDidChangeResponse astDidChangeResponse = editorManager.astDidChange(ast, uri);
                if (astDidChangeResponse == null) {
                    LOG.debug("Error occurred when fetching astDidChange response.");
                }
            } catch (Exception e) {
                LOG.warn("Error occurred when handling diagram edit event.", e);
            }
        }

        public void log(@Nullable String text) {
            Logger.getInstance(DiagramPanelBridge.class).warn(text);
        }
    }

    private class BridgeSettingListener implements ChangeListener<State> {
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            JSObject win = (JSObject) getWebViewGuaranteed().getEngine().executeScript("window");
            win.setMember("DiagramPanelBridge", DiagramPanelBridge.INSTANCE);
        }
    }

    private class ScrollPreservingListener implements ChangeListener<State> {
        volatile int myScrollY = 0;

        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            if (newValue == State.RUNNING) {
                final Object result = getWebViewGuaranteed().getEngine()
                        .executeScript("document.documentElement.scrollTop || document.body.scrollTop");
                if (result instanceof Number) {
                    myScrollY = ((Number) result).intValue();
                }
            } else if (newValue == State.SUCCEEDED) {
                getWebViewGuaranteed().getEngine().executeScript(
                        "document.documentElement.scrollTop = ({} || document.body).scrollTop = " + myScrollY);
            }
        }
    }
}
