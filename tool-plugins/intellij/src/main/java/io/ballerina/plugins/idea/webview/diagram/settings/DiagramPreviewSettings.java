package io.ballerina.plugins.idea.webview.diagram.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import io.ballerina.plugins.idea.webview.diagram.preview.HtmlPanelProvider;
import io.ballerina.plugins.idea.webview.diagram.preview.javafx.JavaFxHtmlPanelProvider;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

import org.jetbrains.annotations.NotNull;

public final class DiagramPreviewSettings {
  public static final DiagramPreviewSettings DEFAULT = new DiagramPreviewSettings();

  @Attribute("DefaultSplitLayout")
  @NotNull
  private SplitFileEditor.SplitEditorLayout mySplitEditorLayout = SplitFileEditor.SplitEditorLayout.FIRST;

  @Tag("HtmlPanelProviderInfo")
  @Property(surroundWithTag = false)
  @NotNull
  private HtmlPanelProvider.ProviderInfo myHtmlPanelProviderInfo = new JavaFxHtmlPanelProvider().getProviderInfo();

  @Attribute("UseGrayscaleRendering")
  private boolean myUseGrayscaleRendering = false;

  @Attribute("AutoScrollPreview")
  private boolean myIsAutoScrollPreview = true;

  public DiagramPreviewSettings() {
  }

  public DiagramPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
                                 @NotNull HtmlPanelProvider.ProviderInfo htmlPanelProviderInfo,
                                 boolean useGrayscaleRendering,
                                 boolean isAutoScrollPreview) {
    mySplitEditorLayout = splitEditorLayout;
    myHtmlPanelProviderInfo = htmlPanelProviderInfo;
    myUseGrayscaleRendering = useGrayscaleRendering;
    myIsAutoScrollPreview = isAutoScrollPreview;
  }

  @NotNull
  public SplitFileEditor.SplitEditorLayout getSplitEditorLayout() {
    return mySplitEditorLayout;
  }

  @NotNull
  public HtmlPanelProvider.ProviderInfo getHtmlPanelProviderInfo() {
    return myHtmlPanelProviderInfo;
  }

  public boolean isUseGrayscaleRendering() {
    return myUseGrayscaleRendering;
  }

  public boolean isAutoScrollPreview() {
    return myIsAutoScrollPreview;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiagramPreviewSettings settings = (DiagramPreviewSettings)o;

    if (myUseGrayscaleRendering != settings.myUseGrayscaleRendering) return false;
    if (myIsAutoScrollPreview != settings.myIsAutoScrollPreview) return false;
    if (mySplitEditorLayout != settings.mySplitEditorLayout) return false;
    if (!myHtmlPanelProviderInfo.equals(settings.myHtmlPanelProviderInfo)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySplitEditorLayout.hashCode();
    result = 31 * result + myHtmlPanelProviderInfo.hashCode();
    result = 31 * result + (myUseGrayscaleRendering ? 1 : 0);
    result = 31 * result + (myIsAutoScrollPreview ? 1 : 0);
    return result;
  }

  public interface Holder {
    void setDiagramPreviewSettings(@NotNull DiagramPreviewSettings settings);

    @NotNull
    DiagramPreviewSettings getDiagramPreviewSettings();
  }
}
