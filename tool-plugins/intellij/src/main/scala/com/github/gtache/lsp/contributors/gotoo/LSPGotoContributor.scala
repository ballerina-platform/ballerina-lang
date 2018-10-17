package com.github.gtache.lsp.contributors.gotoo

import com.github.gtache.lsp.PluginMain
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
  * This trait is the base implementation of a GotoContributor
  */
trait LSPGotoContributor extends ChooseByNameContributor {
  protected val LOG: Logger = Logger.getInstance(this.getClass)

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    val res = PluginMain.workspaceSymbols("", "", project, includeNonProjectItems).map(f => f.getName)
    res
  }


}
