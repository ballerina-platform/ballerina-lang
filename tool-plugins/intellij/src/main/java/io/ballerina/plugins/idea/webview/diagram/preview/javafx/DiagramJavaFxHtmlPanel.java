package io.ballerina.plugins.idea.webview.diagram.preview.javafx;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.javafx.JavaFxHtmlPanel;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.webview.diagram.preview.DiagramHtmlPanel;
import io.ballerina.plugins.idea.webview.diagram.preview.DiagramPanelBridge;
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


/**
 * JavaFx based diagram panel implementation.
 */
public class DiagramJavaFxHtmlPanel extends JavaFxHtmlPanel implements DiagramHtmlPanel {

    private static final Logger LOG = Logger.getInstance(DiagramJavaFxHtmlPanel.class);

    @NotNull
    private String myCSP = "";
    @NotNull
    private final ScrollPreservingListener myScrollPreservingListener = new ScrollPreservingListener();
    @NotNull
    private final BridgeSettingListener myBridgeSettingListener = new BridgeSettingListener();
    private DiagramPanelBridge myPanelBridge;

    DiagramJavaFxHtmlPanel(@NotNull Project project, @NotNull VirtualFile file) {
        super();
        myPanelBridge = new DiagramPanelBridge(project, file);

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

    public DiagramPanelBridge getPanelBridge() {
        return myPanelBridge;
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

    private class BridgeSettingListener implements ChangeListener<State> {
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            JSObject win = (JSObject) getWebViewGuaranteed().getEngine().executeScript("window");
            win.setMember("DiagramPanelBridge", myPanelBridge);
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
