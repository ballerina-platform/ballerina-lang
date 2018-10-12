package com.github.gtache.lsp.contributors

import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.DocumentUtils
import com.intellij.codeInsight.completion.{CompletionContributor, CompletionParameters, CompletionResultSet}
import com.intellij.openapi.diagnostic.Logger

/**
  * The completion contributor for the LSP
  */
class LSPCompletionContributor extends CompletionContributor {
  private val LOG: Logger = Logger.getInstance(this.getClass)

  override def fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet): Unit = {
    import scala.collection.JavaConverters._
    val editor = parameters.getEditor
    val offset = parameters.getOffset
    val serverPos = DocumentUtils.offsetToLSPPos(editor, offset)
    val toAdd = EditorEventManager.forEditor(editor).map(e => e.completion(serverPos)).getOrElse(Iterable()).asJava

    result.addAllElements(toAdd)
    super.fillCompletionVariants(parameters, result)
  }
}
