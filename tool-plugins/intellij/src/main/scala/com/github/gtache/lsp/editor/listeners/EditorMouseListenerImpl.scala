package com.github.gtache.lsp.editor.listeners

import com.intellij.openapi.editor.event.{EditorMouseEvent, EditorMouseListener}

/**
  * An EditorMouseListener implementation which just listens to mouseExited and mouseEntered
  */
class EditorMouseListenerImpl extends EditorMouseListener with LSPListener {

  override def mouseExited(e: EditorMouseEvent): Unit = {
    if (checkManager()) manager.mouseExited()
  }

  override def mousePressed(e: EditorMouseEvent): Unit = {
  }

  override def mouseReleased(e: EditorMouseEvent): Unit = {
  }

  override def mouseEntered(e: EditorMouseEvent): Unit = {
    if (checkManager()) manager.mouseEntered()
  }

  override def mouseClicked(e: EditorMouseEvent): Unit = {
    if (checkManager()) manager.mouseClicked(e)
  }
}
