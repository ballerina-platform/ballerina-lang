package com.github.gtache.lsp.editor.listeners

import com.intellij.openapi.editor.event.{EditorMouseEvent, EditorMouseMotionListener}

/**
  * Class listening for mouse movement in an editor (used for hover)
  */
class EditorMouseMotionListenerImpl extends EditorMouseMotionListener with LSPListener {


  override def mouseDragged(e: EditorMouseEvent): Unit = {}

  override def mouseMoved(e: EditorMouseEvent): Unit = {
    if (checkManager()) {
      manager.mouseMoved(e)
    }
  }
}
