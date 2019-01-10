package com.github.gtache.lsp.requests

import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.openapi.diagnostic.Logger
import org.eclipse.lsp4j.SemanticHighlightingParams

object SemanticHighlightingHandler {

  val LOG: Logger = Logger.getInstance(SemanticHighlightingHandler.getClass)

  def handlePush(params: SemanticHighlightingParams): Unit = {
    import scala.collection.JavaConverters._
    if (params != null) {
      val doc = params.getTextDocument
      val lines = params.getLines
      if (doc != null && doc.getUri != null && lines != null && !lines.isEmpty) {
        EditorEventManager.forUri(FileUtils.sanitizeURI(doc.getUri)).foreach(m => {
          m.semanticHighlighting(lines.asScala)
        })
      } else LOG.warn("Null semanticHighlighting identifier or lines : " + doc + " ; " + lines)
    } else LOG.warn("Null semanticHighlightingParams")
  }

}
