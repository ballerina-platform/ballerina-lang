package com.github.gtache.lsp.editor.listeners

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.openapi.diagnostic.Logger

/**
  * Interface for all the LSP listeners depending on a manager
  */
trait LSPListener {
  private val LOG: Logger = Logger.getInstance(this.getClass)
  protected var manager: EditorEventManager = _


  /**
    * Sets the manager for this listener
    *
    * @param manager The manager
    */
  def setManager(manager: EditorEventManager): Unit = {
    this.manager = manager
  }

  /**
    * Checks if a manager is set, and logs and error if not the case
    *
    * @return true or false depending on if the manager is set
    */
  protected def checkManager(): Boolean = {
    if (manager == null) {
      LOG.error("Manager is null")
      false
    } else true
  }

}
