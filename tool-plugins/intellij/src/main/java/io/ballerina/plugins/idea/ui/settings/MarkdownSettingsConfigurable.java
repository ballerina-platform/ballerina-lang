package io.ballerina.plugins.idea.ui.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;

import io.ballerina.plugins.idea.ui.preview.HtmlPanelProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import javax.swing.*;

public class MarkdownSettingsConfigurable implements SearchableConfigurable {
  @Nullable
  private MarkdownSettingsForm myForm = null;
  @NotNull
  private final MarkdownApplicationSettings myMarkdownApplicationSettings;

  public MarkdownSettingsConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Markdown";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel("There are no available preview providers."), BorderLayout.NORTH);
      return panel;
    }
    return form.getComponent();
  }

  @Nullable
  public MarkdownSettingsForm getForm() {
    if (!HtmlPanelProvider.hasAvailableProviders()) {
      return null;
    }

    if (myForm == null) {
      myForm = new MarkdownSettingsForm();
    }
    return myForm;
  }

  @Override
  public boolean isModified() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      return false;
    }
    return !form.getMarkdownCssSettings().equals(myMarkdownApplicationSettings.getMarkdownCssSettings()) ||
           !form.getMarkdownPreviewSettings().equals(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    final MarkdownSettingsForm form = getForm();
    if (form == null) {
      return;
    }

    form.validate();

    myMarkdownApplicationSettings.setMarkdownCssSettings(form.getMarkdownCssSettings());
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(form.getMarkdownPreviewSettings());

    ApplicationManager.getApplication().getMessageBus().syncPublisher(MarkdownApplicationSettings.SettingsChangedListener.TOPIC)
                      .settingsChanged(myMarkdownApplicationSettings);
  }

  @Override
  public void reset() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      return;
    }
    form.setMarkdownCssSettings(myMarkdownApplicationSettings.getMarkdownCssSettings());
    form.setMarkdownPreviewSettings(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void disposeUIResources() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
    myForm = null;
  }
}
