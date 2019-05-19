package io.ballerina.plugins.idea.webview.diagram.preview.javafx;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.impl.http.HttpVirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.javafx.JavaFxHtmlPanel;
import com.intellij.util.messages.MessageBusConnection;
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

import java.awt.Point;
import java.util.Objects;

import javax.swing.JFrame;


/**
 *  JavaFx based diagram panel implementation.
 */
public class DiagramJavaFxHtmlPanel extends JavaFxHtmlPanel implements DiagramHtmlPanel {

    @NotNull
    private String myCSP = "";
    @NotNull
    private String myLastRawHtml = "";
    @NotNull
    private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
    @NotNull
    private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();

    DiagramJavaFxHtmlPanel() {
        super();
        runInPlatformWhenAvailable(() -> {
            if (myWebView != null) {
                updateFontSmoothingType(myWebView, true);
            }
        });
        subscribeForGrayscaleSetting();
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
                "<head>" + "<meta http-equiv=\"Content-Security-Policy\" content=\"" + myCSP + "\"/>"));
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
     * JavaPanelBridge.
     */
    @SuppressWarnings("unused")
    public static class JavaPanelBridge {
        static final JavaPanelBridge INSTANCE = new JavaPanelBridge();
        private static final NotificationGroup DIAGRAM_NOTIFICATION_GROUP = NotificationGroup
                .toolWindowGroup("Diagram headers group", ToolWindowId.MESSAGES_WINDOW);

        public void openInExternalBrowser(@NotNull String link) {
            String fileURI = link;
            String anchor = null;
            if (link.contains("#")) {
                fileURI = Objects.requireNonNull(StringUtil.substringBefore(link, "#"));
                anchor = Objects.requireNonNull(StringUtil.substringAfter(link, "#"));
            }

            VirtualFile targetFile = VirtualFileManager.getInstance().findFileByUrl(fileURI);
            if (targetFile == null || targetFile instanceof HttpVirtualFile) {
                SafeOpener.openLink(link);
            } else {
                openLocalFile(targetFile, anchor);
            }
        }

        private static void openLocalFile(@NotNull VirtualFile targetFile, @Nullable String anchor) {
            Project project = ProjectUtil.guessProjectForFile(targetFile);
            if (project == null) {
                return;
            }
            if (anchor == null) {
                FileEditorManager.getInstance(project).openFile(targetFile, true);
                return;
            }

            final JFrame frame = WindowManager.getInstance().getFrame(project);
            final Point mousePosition = Objects.requireNonNull(frame).getMousePosition();
            if (mousePosition == null) {
                return;
            }
            RelativePoint point = new RelativePoint(frame, mousePosition);
        }

        public void log(@Nullable String text) {
            Logger.getInstance(JavaPanelBridge.class).warn(text);
        }
    }

    private class BridgeSettingListener implements ChangeListener<State> {
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            JSObject win = (JSObject) getWebViewGuaranteed().getEngine().executeScript("window");
            win.setMember("JavaPanelBridge", JavaPanelBridge.INSTANCE);
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
