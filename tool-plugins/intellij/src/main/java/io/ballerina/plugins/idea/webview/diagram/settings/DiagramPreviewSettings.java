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
package io.ballerina.plugins.idea.webview.diagram.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import io.ballerina.plugins.idea.webview.diagram.preview.HtmlPanelProvider;
import io.ballerina.plugins.idea.webview.diagram.preview.javafx.JavaFxHtmlPanelProvider;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

/**
 * Diagram preview settings for ballerina diagram viewer.
 */
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

    private DiagramPreviewSettings() {
    }

    public DiagramPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
            @NotNull HtmlPanelProvider.ProviderInfo htmlPanelProviderInfo, boolean useGrayscaleRendering,
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiagramPreviewSettings settings = (DiagramPreviewSettings) o;

        if (myUseGrayscaleRendering != settings.myUseGrayscaleRendering) {
            return false;
        }
        if (myIsAutoScrollPreview != settings.myIsAutoScrollPreview) {
            return false;
        }
        if (mySplitEditorLayout != settings.mySplitEditorLayout) {
            return false;
        }
        return myHtmlPanelProviderInfo.equals(settings.myHtmlPanelProviderInfo);
    }

    @Override
    public int hashCode() {
        int result = mySplitEditorLayout.hashCode();
        result = 31 * result + myHtmlPanelProviderInfo.hashCode();
        result = 31 * result + (myUseGrayscaleRendering ? 1 : 0);
        result = 31 * result + (myIsAutoScrollPreview ? 1 : 0);
        return result;
    }

    /**
     * Ballerina diagram editor preview settings holder.
     */
    public interface Holder {
        void setDiagramPreviewSettings(@NotNull DiagramPreviewSettings settings);

        @NotNull
        DiagramPreviewSettings getDiagramPreviewSettings();
    }
}
