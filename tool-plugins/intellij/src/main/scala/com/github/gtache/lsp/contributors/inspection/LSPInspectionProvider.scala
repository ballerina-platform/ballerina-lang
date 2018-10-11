package com.github.gtache.lsp.contributors.inspection

import com.intellij.codeInspection.InspectionToolProvider

/**
  * The provider for the LSP Inspection
  * Returns a single class, LSPInspection
  */
class LSPInspectionProvider extends InspectionToolProvider {

  override def getInspectionClasses: Array[Class[_]] = {
    Array(classOf[LSPInspection])
  }
}
