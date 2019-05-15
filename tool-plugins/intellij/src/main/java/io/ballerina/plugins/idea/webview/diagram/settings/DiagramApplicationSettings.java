package io.ballerina.plugins.idea.webview.diagram.settings;

import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.messages.Topic;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.ballerina.plugins.idea.webview.diagram.settings.DiagramCssSettings.DARCULA;
import static io.ballerina.plugins.idea.webview.diagram.settings.DiagramCssSettings.DEFAULT;

@State(
  name = "DiagramApplicationSettings",
  storages = @Storage("diagram.xml")
)
public class DiagramApplicationSettings implements PersistentStateComponent<DiagramApplicationSettings.State>,
                                                    DiagramCssSettings.Holder,
                                                    DiagramPreviewSettings.Holder {

  private final State myState = new State();

  public DiagramApplicationSettings() {
    final DiagramLAFListener lafListener = new DiagramLAFListener();
    LafManager.getInstance().addLafManagerListener(lafListener);

    // Todo - Reuse if possible
    // Let's init proper CSS scheme
    // ApplicationManager.getApplication().invokeLater(() -> lafListener.updateCssSettingsForced(UIUtil.isUnderDarcula
    // ()));
  }

  @NotNull
  public static DiagramApplicationSettings getInstance() {
    return ServiceManager.getService(DiagramApplicationSettings.class);
  }

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    XmlSerializerUtil.copyBean(state, myState);
  }

  @Override
  public void setDiagramCssSettings(@NotNull DiagramCssSettings settings) {
    ApplicationManager.getApplication().getMessageBus().syncPublisher(SettingsChangedListener.TOPIC).beforeSettingsChanged(this);
    myState.myCssSettings = settings;
  }

  @NotNull
  @Override
  public DiagramCssSettings getDiagramCssSettings() {
    if (DARCULA.getStylesheetUri().equals(myState.myCssSettings.getStylesheetUri())
        || DEFAULT.getStylesheetUri().equals(myState.myCssSettings.getStylesheetUri())) {
      return new DiagramCssSettings(false,
                                     "",
                                     myState.myCssSettings.isTextEnabled(),
                                     myState.myCssSettings.getStylesheetText());
    }

    return myState.myCssSettings;
  }

  @Override
  public void setDiagramPreviewSettings(@NotNull DiagramPreviewSettings settings) {
    ApplicationManager.getApplication().getMessageBus().syncPublisher(SettingsChangedListener.TOPIC).beforeSettingsChanged(this);
    myState.myPreviewSettings = settings;
  }

  @NotNull
  @Override
  public DiagramPreviewSettings getDiagramPreviewSettings() {
    return myState.myPreviewSettings;
  }


  public static class State {
    @Property(surroundWithTag = false)
    @NotNull
    private DiagramCssSettings myCssSettings = DiagramCssSettings.DEFAULT;

    @Property(surroundWithTag = false)
    @NotNull
    private DiagramPreviewSettings myPreviewSettings = DiagramPreviewSettings.DEFAULT;
  }

  public interface SettingsChangedListener {
    Topic<SettingsChangedListener> TOPIC = Topic.create("DiagramApplicationSettingsChanged", SettingsChangedListener.class);

    default void beforeSettingsChanged(@NotNull DiagramApplicationSettings settings) { }

    default void settingsChanged(@NotNull DiagramApplicationSettings settings) { }
  }
}
