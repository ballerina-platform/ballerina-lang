package io.ballerina.plugins.idea.webview.diagram.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.net.URL;

public final class DiagramCssSettings {
  public static final DiagramCssSettings DEFAULT = new DiagramCssSettings(false);
  public static final DiagramCssSettings DARCULA = new DiagramCssSettings(true);

  @SuppressWarnings("FieldMayBeFinal")
  @Attribute("UriEnabled")
  private boolean myUriEnabled;

  @SuppressWarnings("FieldMayBeFinal")
  @Attribute("StylesheetUri")
  @NotNull
  private String myStylesheetUri;

  @SuppressWarnings("FieldMayBeFinal")
  @Attribute("TextEnabled")
  private boolean myTextEnabled;

  @SuppressWarnings("FieldMayBeFinal")
  @Attribute("StylesheetText")
  @NotNull
  private String myStylesheetText;

  private DiagramCssSettings() {
    this(UIUtil.isUnderDarcula());
  }

  private DiagramCssSettings(boolean isDarcula) {
    this(false, getPredefinedCssURI(isDarcula), false, "");
  }

  public DiagramCssSettings(boolean uriEnabled, @NotNull String stylesheetUri, boolean textEnabled, @NotNull String stylesheetText) {
    myUriEnabled = uriEnabled;
    myStylesheetUri = stylesheetUri;
    myTextEnabled = textEnabled;
    myStylesheetText = stylesheetText;
  }

  public boolean isUriEnabled() {
    return myUriEnabled;
  }

  @NotNull
  public String getStylesheetUri() {
    return myStylesheetUri;
  }

  public boolean isTextEnabled() {
    return myTextEnabled;
  }

  @NotNull
  public String getStylesheetText() {
    return myStylesheetText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiagramCssSettings settings = (DiagramCssSettings)o;

    if (myUriEnabled != settings.myUriEnabled) return false;
    if (myTextEnabled != settings.myTextEnabled) return false;
    if (!myStylesheetUri.equals(settings.myStylesheetUri)) return false;
    if (!myStylesheetText.equals(settings.myStylesheetText)) return false;

    return true;
  }

  @NotNull
  public static DiagramCssSettings getDefaultCssSettings(boolean isDarcula) {
    return isDarcula ? DARCULA : DEFAULT;
  }

  @NotNull
  private static String getPredefinedCssURI(boolean isDarcula) {
    final String fileName = isDarcula ? "darcula.css" : "default.css";
    try {
      final URL resource = DiagramCssSettings.class.getResource(fileName);
      return resource != null ? resource.toURI().toString() : "";
    }
    catch (URISyntaxException e) {
      Logger.getInstance(DiagramCssSettings.class).error(e);
      return "";
    }
  }

  @Override
  public int hashCode() {
    int result = (myUriEnabled ? 1 : 0);
    result = 31 * result + myStylesheetUri.hashCode();
    result = 31 * result + (myTextEnabled ? 1 : 0);
    result = 31 * result + myStylesheetText.hashCode();
    return result;
  }

  public interface Holder {
    void setDiagramCssSettings(@NotNull DiagramCssSettings settings);

    @NotNull
    DiagramCssSettings getDiagramCssSettings();
  }
}
