package com.github.gtache.lsp.settings.gui

import com.github.gtache.lsp.settings.{BallerinaLSPState, LSPState}
import javax.swing.JPanel

trait LSPGUI {

  import LSPGUI.lspState

  def state: BallerinaLSPState = lspState

  def isModified: Boolean

  def reset(): Unit

  def apply(): Unit

  def getRootPanel: JPanel

}

object LSPGUI {
  val lspState: BallerinaLSPState = BallerinaLSPState.getInstance()
}
