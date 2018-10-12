package com.github.gtache.lsp.utils

import java.awt.Point

import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import com.github.gtache.lsp.contributors.icon.{LSPDefaultIconProvider, LSPIconProvider}
import com.intellij.codeInsight.hint.{HintManager, HintManagerImpl}
import com.intellij.openapi.editor.Editor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.{Hint, LightweightHint}
import javax.swing.{JComponent, JLabel}

/**
  * Various utility methods related to the interface
  */
object GUIUtils {

  /**
    * Shows a hint in the editor
    *
    * @param editor     The editor
    * @param string     The message / text of the hint
    * @param point      The position of the hint
    * @param constraint The constraint (under/above)
    * @param flags      The flags (when the hint will disappear)
    * @return The hint
    */
  def createAndShowEditorHint(editor: Editor, string: String, point: Point, constraint: Short = HintManager.ABOVE,
                              flags: Int = HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING): Hint = {
    val hint = new LightweightHint(new JLabel(string))
    val p = HintManagerImpl.getHintPosition(hint, editor, editor.xyToLogicalPosition(point), constraint)
    HintManagerImpl.getInstanceImpl.showEditorHint(hint, editor, p, flags, 0, false, HintManagerImpl.createHintHint(editor, p, hint, constraint).setContentActive(false))
    hint
  }

  def createAndShowHint(component: JComponent, string: String, point: RelativePoint,
                        flags: Int = HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_OTHER_HINT | HintManager.HIDE_BY_SCROLLING): Unit = {
    val hint = new LightweightHint(new JLabel(string))
    HintManagerImpl.getInstanceImpl.showHint(component, point, flags, 0)
  }

  /**
    * Returns a suitable LSPIconProvider given a ServerDefinition
    *
    * @param serverDefinition The serverDefinition
    * @return The LSPIconProvider, or LSPDefaultIconProvider if none are found
    */
  def getIconProviderFor(serverDefinition: LanguageServerDefinition): LSPIconProvider = {
    if (serverDefinition != null) {
      try {
        val providers = LSPIconProvider.EP_NAME.getExtensions.filter(provider => provider.isSpecificFor(serverDefinition))
        if (providers.nonEmpty) providers.head else LSPDefaultIconProvider
      } catch {
        case e: IllegalArgumentException => LSPDefaultIconProvider
      }
    } else LSPDefaultIconProvider
  }
}
