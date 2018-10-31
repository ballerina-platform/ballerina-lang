package com.github.gtache.lsp.contributors.inspection

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.contributors.LSPQuickFix
import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.{DiagnosticRangeHighlighter, EditorEventManager}
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.codeInspection._
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import io.ballerina.plugins.idea.codeinsight.semanticanalyzer.BallerinaSemanticAnalyzerSettings
import javax.swing.JComponent
import org.eclipse.lsp4j.DiagnosticSeverity

/**
  * The inspection tool for LSP
  */
class LSPInspection extends LocalInspectionTool {

  override def checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    //Todo - Remove after fixing Diagnostics
    if (!BallerinaSemanticAnalyzerSettings.getInstance.useSemanticAnalyzer) return null

    val virtualFile = file.getVirtualFile
    if (PluginMain.isExtensionSupported(virtualFile.getExtension)) {
      val uri = FileUtils.VFSToURI(virtualFile)

      /**
        * Get all the ProblemDescriptor given an EditorEventManager
        * Look at the DiagnosticHighlights, create dummy PsiElement for each, create descriptor using it
        *
        * @param m The manager
        * @return The ProblemDescriptors
        */
      def descriptorsForManager(m: EditorEventManager): Array[ProblemDescriptor] = {
        val diagnostics = m.getDiagnostics
        diagnostics.collect { case DiagnosticRangeHighlighter(rangeHighlighter, diagnostic) =>
          val start = rangeHighlighter.getStartOffset
          val end = rangeHighlighter.getEndOffset
          if (start < end) {
            val name = m.editor.getDocument.getText(new TextRange(start, end))
            val severity = diagnostic.getSeverity match {
              case DiagnosticSeverity.Error => ProblemHighlightType.ERROR
              case DiagnosticSeverity.Warning => ProblemHighlightType.GENERIC_ERROR_OR_WARNING
              case DiagnosticSeverity.Information => ProblemHighlightType.INFORMATION
              case DiagnosticSeverity.Hint => ProblemHighlightType.INFORMATION
              case _ => null
            }
            val element = LSPPsiElement(name, m.editor.getProject, start, end, file)
            val commands = m.codeAction(element)
            manager.createProblemDescriptor(element, null.asInstanceOf[TextRange], diagnostic.getMessage, severity, isOnTheFly,
              (if (commands != null) commands.map(c => new LSPQuickFix(uri, c)).toArray else null): _*)
          } else null
        }.toArray.filter(d => d != null)
      }

      EditorEventManager.forUri(uri) match {
        case Some(m) =>
          descriptorsForManager(m)
        case None =>
          if (isOnTheFly) {
            super.checkFile(file, manager, isOnTheFly)
          } else {
            /*val descriptor = new OpenFileDescriptor(manager.getProject, virtualFile)
            ApplicationUtils.writeAction(() => FileEditorManager.getInstance(manager.getProject).openTextEditor(descriptor, false))
            EditorEventManager.forUri(uri) match {
              case Some(m) => descriptorsForManager(m)
              case None => super.checkFile(file, manager, isOnTheFly)
            }*/
            //TODO need dispatch thread
            super.checkFile(file, manager, isOnTheFly)
          }
      }
    } else super.checkFile(file, manager, isOnTheFly)
  }

  override def getDisplayName: String = getShortName

  override def createOptionsPanel(): JComponent = {
    new LSPInspectionPanel(getShortName, this)
  }

  override def getShortName: String = "LSP"

  override def getID: String = "LSP"

  override def getGroupDisplayName: String = "LSP"

  override def getStaticDescription: String = "Reports errors by the LSP server"

  override def isEnabledByDefault: Boolean = true

}
