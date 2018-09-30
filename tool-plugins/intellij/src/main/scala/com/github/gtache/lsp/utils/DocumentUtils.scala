package com.github.gtache.lsp.utils

import com.github.gtache.lsp.utils.ApplicationUtils.computableReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.{Editor, LogicalPosition}
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.DocumentUtil
import org.eclipse.lsp4j.Position

/**
  * Various methods to convert offsets / logical position / server position
  */
object DocumentUtils {

  private val LOG: Logger = Logger.getInstance(this.getClass)

  /**
    * Gets the line at the given offset given an editor and bolds the text between the given offsets
    *
    * @param editor      The editor
    * @param startOffset The starting offset
    * @param endOffset   The ending offset
    * @return The document line
    */
  def getLineText(editor: Editor, startOffset: Int, endOffset: Int): String = {
    computableReadAction(() => {
      val doc = editor.getDocument
      val lineIdx = doc.getLineNumber(startOffset)
      val lineStartOff = doc.getLineStartOffset(lineIdx)
      val lineEndOff = doc.getLineEndOffset(lineIdx)
      val line = doc.getText(new TextRange(lineStartOff, lineEndOff))
      val startOffsetInLine = startOffset - lineStartOff
      val endOffsetInLine = endOffset - lineStartOff
      line.substring(0, startOffsetInLine) + "<b>" + line.substring(startOffsetInLine, endOffsetInLine) + "</b>" + line.substring(endOffsetInLine)
    })
  }

  /**
    * Transforms a LogicalPosition (IntelliJ) to an LSP Position
    *
    * @param position the LogicalPosition
    * @param editor   The editor
    * @return the Position
    */
  def logicalToLSPPos(position: LogicalPosition, editor: Editor): Position = {
    offsetToLSPPos(editor, editor.logicalPositionToOffset(position))
  }

  /**
    * Calculates a Position given an editor and an offset
    *
    * @param editor The editor
    * @param offset The offset
    * @return an LSP position
    */
  def offsetToLSPPos(editor: Editor, offset: Int): Position = {
    computableReadAction(() => {
      val doc = editor.getDocument
      val line = doc.getLineNumber(offset)
      val lineStart = doc.getLineStartOffset(line)
      val lineTextBeforeOffset = doc.getText(TextRange.create(lineStart, offset))
      val column = lineTextBeforeOffset.length
      new Position(line, column)
    })
  }

  /**
    * Transforms an LSP position to an editor offset
    *
    * @param editor The editor
    * @param pos    The LSPPos
    * @return The offset
    */
  def LSPPosToOffset(editor: Editor, pos: Position): Int = {
    computableReadAction(() => {
      val line = pos.getLine
      val doc = editor.getDocument
      val lineTextForPosition = doc.getText(DocumentUtil.getLineTextRange(doc, line)).substring(0, pos.getCharacter)
      val tabs = StringUtil.countChars(lineTextForPosition, '\t')
      val tabSize = editor.getSettings.getTabSize(editor.getProject)
      val column = tabs * tabSize + lineTextForPosition.length - tabs
      val offset = editor.logicalPositionToOffset(new LogicalPosition(line, column))
      val docLength = doc.getTextLength
      if (offset > docLength) {
        LOG.warn("Offset greater than text length : " + offset + " > " + docLength)
      }
      math.min(math.max(offset, 0), docLength)
    })
  }

}
