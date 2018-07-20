package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import javax.swing.*;

//import org.intellij.markdown.html.HtmlGenerator;
//import org.intellij.plugins.markdown.settings.MarkdownCssSettings;

public interface MarkdownHtmlPanel extends Disposable {
    List<String> SCRIPTS = Arrays.asList("processLinks.js", "scrollToElement.js");

    List<String> STYLES = Arrays.asList("default.css", "darcula.css", "inline.css");

    @NotNull
    JComponent getComponent();

    void setHtml(@NotNull String html);

    void render();

}
