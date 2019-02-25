package com.github.gtache.lsp.editor

import java.awt.Cursor

import com.github.gtache.lsp.utils.DocumentUtils
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.RangeHighlighter
import org.eclipse.lsp4j.Location

/**
  * Class used to store a specific range corresponding to the element under the mouse when Ctrl is pressed
  *
  * @param loc    The location of the definition of the element under the mouse
  * @param editor The current editor
  * @param range  The range of the element under the mouse (represented as an hyperlink)
  */
case class CtrlRangeMarker(loc: Location, editor: Editor, range: RangeHighlighter) {

  def highlightContainsOffset(offset: Int): Boolean = {
    if (!isDefinition) range.getStartOffset <= offset && range.getEndOffset >= offset else definitionContainsOffset(offset)
  }

  def definitionContainsOffset(offset: Int): Boolean = {
    DocumentUtils.LSPPosToOffset(editor, loc.getRange.getStart) <= offset && offset <= DocumentUtils.LSPPosToOffset(editor, loc.getRange.getEnd)
  }

  /**
    * Removes the highlighter and restores the default cursor
    */
  def dispose(): Unit = {
    if (!isDefinition) {
      editor.getMarkupModel.removeHighlighter(range)
      editor.getContentComponent.setCursor(Cursor.getDefaultCursor)
    }
  }

  /**
    * If the marker points to the definition itself
    */
  def isDefinition: Boolean = range == null

}
