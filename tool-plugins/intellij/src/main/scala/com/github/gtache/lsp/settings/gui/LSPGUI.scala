package com.github.gtache.lsp.settings.gui

import com.github.gtache.lsp.settings.LSPState
import javax.swing.JPanel

trait LSPGUI {

  import LSPGUI.lspState

  def state: LSPState = lspState

  def isModified: Boolean

  def reset(): Unit

  def apply(): Unit

  def getRootPanel: JPanel

}

object LSPGUI {
  val lspState: LSPState = LSPState.getInstance()
}
