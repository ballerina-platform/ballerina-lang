package io.ballerina.plugins.idea.ui.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import io.ballerina.plugins.idea.ui.preview.HtmlPanelProvider;
import io.ballerina.plugins.idea.ui.preview.javafx.JavaFxHtmlPanelProvider;
import io.ballerina.plugins.idea.ui.split.SplitFileEditor;

import org.jetbrains.annotations.NotNull;

public final class MarkdownPreviewSettings {
  public static final MarkdownPreviewSettings DEFAULT = new MarkdownPreviewSettings();

  @Attribute("DefaultSplitLayout")
  @NotNull
  private SplitFileEditor.SplitEditorLayout mySplitEditorLayout = SplitFileEditor.SplitEditorLayout.SPLIT;

  @Tag("HtmlPanelProviderInfo")
  @Property(surroundWithTag = false)
  @NotNull
  private HtmlPanelProvider.ProviderInfo myHtmlPanelProviderInfo = new JavaFxHtmlPanelProvider().getProviderInfo();

  @Attribute("UseGrayscaleRendering")
  private boolean myUseGrayscaleRendering = false;

  @Attribute("AutoScrollPreview")
  private boolean myIsAutoScrollPreview = true;

  public MarkdownPreviewSettings() {
  }

  public MarkdownPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
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

    MarkdownPreviewSettings settings = (MarkdownPreviewSettings)o;

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
    void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings);

    @NotNull
    MarkdownPreviewSettings getMarkdownPreviewSettings();
  }
}
