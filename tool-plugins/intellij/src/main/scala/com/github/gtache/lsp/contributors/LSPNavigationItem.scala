package com.github.gtache.lsp.contributors

import com.intellij.navigation.{ItemPresentation, NavigationItem}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

/**
  * An LSP NavigationItem
  *
  * @param name      The name of the item
  * @param container The container name (a file, ...)
  * @param project   The project the item belongs to
  * @param file      The file it belongs to
  * @param line      Its line
  * @param col       Its column
  */
case class LSPNavigationItem(name: String, container: String, project: Project, file: VirtualFile, line: Int, col: Int, icon: Icon = null) extends OpenFileDescriptor(project, file, line, col) with NavigationItem {

  private val LOG: Logger = Logger.getInstance(classOf[LSPNavigationItem])

  override def getName: String = name

  override def getPresentation: ItemPresentation = new ItemPresentation {

    override def getPresentableText: String = name

    override def getLocationString: String = (if (container != null) container else "") + name

    override def getIcon(unused: Boolean): Icon = if (unused) null else icon
  }
}
