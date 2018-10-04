package com.github.gtache.lsp.editor.listeners

import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer

/**
  * This class is used to reject save requests
  * It is used for willSaveWaitUntil to allow time to apply the edits
  */
//TODO check called before willSave
class LSPFileDocumentSynchronizationVetoer extends FileDocumentSynchronizationVetoer {
  override def maySaveDocument(document: Document, isSaveExplicit: Boolean): Boolean = {
    EditorEventManager.forUri(FileUtils.documentToUri(document)) match {
      case Some(m) =>
        if (m.needSave) {
          m.needSave = false
          super.maySaveDocument(document, isSaveExplicit)
        }
        else if (m.wrapper.isWillSaveWaitUntil) false
        else super.maySaveDocument(document, isSaveExplicit)
      case None =>
        super.maySaveDocument(document, isSaveExplicit)
    }
  }
}
