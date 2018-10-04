package com.github.gtache.lsp.contributors.gotoo

import com.github.gtache.lsp.PluginMain
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import org.eclipse.lsp4j.SymbolKind

/**
  * This class handles the GotoClass IntelliJ request
  */
class LSPGotoClassContributor extends LSPGotoContributor {
  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    val res = PluginMain.workspaceSymbols(name, pattern, project, includeNonProjectItems, Set(SymbolKind.Class, SymbolKind.Enum, SymbolKind.Interface))
    res
  }

}
