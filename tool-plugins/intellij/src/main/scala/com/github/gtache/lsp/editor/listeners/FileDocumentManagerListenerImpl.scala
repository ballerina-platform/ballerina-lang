package com.github.gtache.lsp.editor.listeners

import com.github.gtache.lsp.requests.FileEventManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.vfs.VirtualFile

/**
  * A FileDocumentManagerListener implementation which listens to beforeDocumentSaving / beforeAllDocumentsSaving
  */
object FileDocumentManagerListenerImpl extends FileDocumentManagerListener {
  override def beforeDocumentSaving(document: Document): Unit = {
    FileEventManager.willSave(document)
  }

  override def unsavedDocumentsDropped(): Unit = {}

  override def beforeAllDocumentsSaving(): Unit = FileEventManager.willSaveAllDocuments()

  override def beforeFileContentReload(virtualFile: VirtualFile, document: Document): Unit = {}

  override def fileWithNoDocumentChanged(virtualFile: VirtualFile): Unit = {}

  override def fileContentReloaded(virtualFile: VirtualFile, document: Document): Unit = {}

  override def fileContentLoaded(virtualFile: VirtualFile, document: Document): Unit = {}
}
