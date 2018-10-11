package com.github.gtache.lsp.editor.listeners

import com.github.gtache.lsp.PluginMain
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.event.{EditorFactoryEvent, EditorFactoryListener}

/**
  * An EditorListener implementation
  */
class EditorListener extends EditorFactoryListener {

  private val LOG: Logger = Logger.getInstance(classOf[EditorListener])

  override def editorReleased(editorFactoryEvent: EditorFactoryEvent): Unit = {
    PluginMain.editorClosed(editorFactoryEvent.getEditor)
  }

  override def editorCreated(editorFactoryEvent: EditorFactoryEvent): Unit = {
    PluginMain.editorOpened(editorFactoryEvent.getEditor)
  }
}
