package com.github.gtache.lsp.editor.listeners

import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}

/**
  * Implementation of a DocumentListener
  */
class DocumentListenerImpl extends DocumentListener with LSPListener {

  /**
    * Called before the text of the document is changed.
    *
    * @param event the event containing the information about the change.
    */
  override def beforeDocumentChange(event: DocumentEvent): Unit = {
  }


  /**
    * Called after the text of the document has been changed.
    *
    * @param event the event containing the information about the change.
    */
  override def documentChanged(event: DocumentEvent): Unit = {
    if (checkManager()) {
      manager.documentChanged(event)
    }
  }

}
