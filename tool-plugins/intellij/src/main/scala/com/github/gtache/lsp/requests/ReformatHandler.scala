package com.github.gtache.lsp.requests

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.{ApplicationUtils, FileUtils}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

/**
  * Object handling reformat events
  */
object ReformatHandler {

  /**
    * Unused
    * Reformats all the files in the project
    *
    * @param project The project
    * @return True if all the files were supported by the language servers, false otherwise
    */
  def reformatAllFiles(project: Project): Boolean = {
    var allFilesSupported = true
    ProjectFileIndex.getInstance(project).iterateContent((fileOrDir: VirtualFile) => {
      if (fileOrDir.isDirectory) {
        true
      } else {
        if (PluginMain.isExtensionSupported(fileOrDir.getExtension)) {
          reformatFile(fileOrDir, project)
          true
        } else {
          allFilesSupported = false
          true
        }
      }
    })
    allFilesSupported
  }

  /**
    * Reformat a file given a VirtualFile and a Project
    *
    * @param file    The file
    * @param project The project
    */
  def reformatFile(file: VirtualFile, project: Project): Unit = {
    if (PluginMain.isExtensionSupported(file.getExtension)) {
      val uri = FileUtils.VFSToURI(file)
      EditorEventManager.forUri(uri) match {
        case Some(manager) =>
          manager.reformat()
        case None =>
          ApplicationUtils.invokeLater(() => {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val descriptor = new OpenFileDescriptor(project, file)
            val editor = ApplicationUtils.computableWriteAction(() => {
              fileEditorManager.openTextEditor(descriptor, false)
            })
            EditorEventManager.forEditor(editor).get.reformat(closeAfter = true)
          })
      }
    }
  }

  /**
    * Reformat a file given its editor
    *
    * @param editor The editor
    */
  def reformatFile(editor: Editor): Unit =
    EditorEventManager.forEditor(editor).foreach(manager => manager.reformat())


  /**
    * Reformat a selection in a file given its editor
    *
    * @param editor The editor
    */
  def reformatSelection(editor: Editor): Unit = {
    EditorEventManager.forEditor(editor).foreach(manager => manager.reformatSelection())
  }

}
