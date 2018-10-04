package com.github.gtache.lsp.editor

import com.intellij.openapi.editor.markup.RangeHighlighter
import org.eclipse.lsp4j.Diagnostic

/**
  * A class representing a Diagnostic Range
  * The diagnostic is sent from the server, the rangeHighlighter is created from the severity and range of the diagnostic
  *
  * @param rangeHighlighter The rangeHighlighter of the diagnostic
  * @param diagnostic       The diagnostic
  */
case class DiagnosticRangeHighlighter(rangeHighlighter: RangeHighlighter, diagnostic: Diagnostic) {

}
