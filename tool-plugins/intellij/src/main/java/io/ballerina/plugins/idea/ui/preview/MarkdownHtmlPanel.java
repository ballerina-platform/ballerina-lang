package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

//import org.intellij.markdown.html.HtmlGenerator;
//import org.intellij.plugins.markdown.settings.MarkdownCssSettings;

public interface MarkdownHtmlPanel extends Disposable {

    @NotNull
    JComponent getComponent();

    void setHtml(@NotNull String html);

    void render();

    void setCSS(@Nullable String inlineCss, @NotNull String... fileUris);

}
