package com.github.gtache.lsp.requests

import java.io.File
import java.net.{URI, URL}
import java.util

import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.{DocumentUtils, FileUtils}
import com.intellij.openapi.command.{CommandProcessor, UndoConfirmationPolicy}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor}
import com.intellij.openapi.project.{Project, ProjectManager, ProjectUtil}
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.usageView.UsageInfo
import org.eclipse.lsp4j.{Range, TextEdit, WorkspaceEdit}

import scala.collection.mutable

/**
  * An Object handling WorkspaceEdits
  */
object WorkspaceEditHandler {

  import com.github.gtache.lsp.utils.ApplicationUtils._

  private val LOG: Logger = Logger.getInstance(WorkspaceEditHandler.getClass)

  def applyEdit(elem: PsiElement, newName: String, infos: Array[UsageInfo], listener: RefactoringElementListener, openedEditors: Iterable[VirtualFile]): Unit = {
    val edits = mutable.Map[String, mutable.ListBuffer[TextEdit]]()
    elem match {
      case lspElem: LSPPsiElement =>
        if (infos.forall(info => info.getElement.isInstanceOf[LSPPsiElement])) {
          infos.foreach(ui => {
            val editor = FileUtils.editorFromVirtualFile(ui.getVirtualFile, ui.getProject)
            val range = ui.getElement.getTextRange
            val lspRange = new Range(DocumentUtils.offsetToLSPPos(editor, range.getStartOffset), DocumentUtils.offsetToLSPPos(editor, range.getEndOffset))
            val edit = new TextEdit(lspRange, newName)
            val uri = FileUtils.sanitizeURI(new URL(ui.getVirtualFile.getUrl.replace(" ", FileUtils.SPACE_ENCODED)).toURI.toString)
            if (edits.contains(uri)) {
              edits(uri) += edit
            } else {
              edits.put(uri, mutable.ListBuffer(edit))
            }
          })
          import scala.collection.JavaConverters._
          val javaMap = new util.HashMap[String, java.util.List[TextEdit]]()
          edits.foreach(edit => {
            javaMap.put(edit._1, edit._2.asJava)
          })
          val workspaceEdit = new WorkspaceEdit(javaMap)
          applyEdit(workspaceEdit, "Rename " + lspElem.getName + " to " + newName, openedEditors)
        }
      case _ =>
    }
  }

  /**
    * Applies a WorkspaceEdit
    *
    * @param edit The edit
    * @return True if everything was applied, false otherwise
    */
  def applyEdit(edit: WorkspaceEdit, name: String = "LSP edits", toClose: Iterable[VirtualFile] = Seq()): Boolean = {
    import scala.collection.JavaConverters._
    if (edit != null) {
      val changes = if (edit.getChanges != null) edit.getChanges.asScala else null
      val dChanges = if (edit.getDocumentChanges != null) edit.getDocumentChanges.asScala else null
      var didApply: Boolean = true

      invokeLater(() => {
        var curProject: Project = null
        val openedEditors: scala.collection.mutable.ListBuffer[VirtualFile] = scala.collection.mutable.ListBuffer()

        /**
          * Opens an editor when needed and gets the Runnable
          *
          * @param edits   The text edits
          * @param uri     The uri of the file
          * @param version The version of the file
          * @return The runnable containing the edits
          */
        def manageUnopenedEditor(edits: Iterable[TextEdit], uri: String, version: Int = Int.MaxValue): Runnable = {
          val projects = ProjectManager.getInstance().getOpenProjects
          val project = projects //Infer the project from the uri
            .map(p => (FileUtils.VFSToURI(ProjectUtil.guessProjectDir(p)), p))
            .filter(p => uri.startsWith(p._1))
            .sortBy(s => s._1.length).reverse
            .map(p => p._2)
            .headOption
            .getOrElse(projects(0))
          val file = LocalFileSystem.getInstance().findFileByIoFile(new File(new URI(FileUtils.sanitizeURI(uri))))
          val fileEditorManager = FileEditorManager.getInstance(project)
          val descriptor = new OpenFileDescriptor(project, file)
          val editor: Editor = computableWriteAction(() => {
            fileEditorManager.openTextEditor(descriptor, false)
          })
          openedEditors += file
          curProject = editor.getProject
          var runnable: Runnable = null
          EditorEventManager.forEditor(editor) match {
            case Some(manager) => runnable = manager.getEditsRunnable(version, edits, name)
            case None =>
          }
          runnable
        }

        //Get the runnable of edits for each editor to apply them all in one command
        val toApply: scala.collection.mutable.ListBuffer[Runnable] = scala.collection.mutable.ListBuffer()
        if (dChanges != null) {
          dChanges.foreach(edit => {
            if (edit.isLeft) {
              val textEdit = edit.getLeft
              val doc = textEdit.getTextDocument
              val version = doc.getVersion
              val uri = FileUtils.sanitizeURI(doc.getUri)
              toApply += (EditorEventManager.forUri(uri) match {
                case Some(manager) =>
                  curProject = manager.editor.getProject
                  manager.getEditsRunnable(version, textEdit.getEdits.asScala.toList, name)
                case None => manageUnopenedEditor(textEdit.getEdits.asScala, uri, version)
              })
            } else if (edit.isRight) {
              val resourceOp = edit.getRight
              //TODO
            } else {
              LOG.warn("Null edit")
            }

          })
        } else if (changes != null) {
          changes.foreach(edit => {
            val uri = FileUtils.sanitizeURI(edit._1)
            val changes = edit._2.asScala
            toApply += (EditorEventManager.forUri(uri) match {
              case Some(manager) =>
                curProject = manager.editor.getProject
                manager.getEditsRunnable(edits = changes.toList, name = name)
              case None =>
                manageUnopenedEditor(changes, uri)
            })
          })
        }
        if (toApply.contains(null)) {
          LOG.warn("Didn't apply, null runnable")
          didApply = false
        } else {
          val runnable = new Runnable {
            override def run(): Unit = toApply.foreach(r => r.run())
          }
          invokeLater(() => writeAction(() => {
            CommandProcessor.getInstance().executeCommand(curProject, runnable, name, "LSPPlugin", UndoConfirmationPolicy.DEFAULT, false)
            (openedEditors ++ toClose).foreach(f => FileEditorManager.getInstance(curProject).closeFile(f))
          }))
        }
      })
      didApply
    } else false
  }


}
