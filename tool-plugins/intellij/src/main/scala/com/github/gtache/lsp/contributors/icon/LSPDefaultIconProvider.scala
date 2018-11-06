package com.github.gtache.lsp.contributors.icon

import com.github.gtache.lsp.client.languageserver.ServerStatus
import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon
import org.eclipse.lsp4j.{CompletionItemKind, SymbolKind}


object LSPDefaultIconProvider extends LSPIconProvider {

  import com.intellij.icons.AllIcons.Nodes

  private val STARTED = IconLoader.getIcon("/images/started.png")
  private val STARTING = IconLoader.getIcon("/images/starting.png")
  private val STOPPED = IconLoader.getIcon("/images/stopped.png")

  override def getCompletionIcon(kind: CompletionItemKind): Icon = {
    kind match {
      case CompletionItemKind.Class => Nodes.Class
      case CompletionItemKind.Color => null
      case CompletionItemKind.Constructor => null
      case CompletionItemKind.Enum => Nodes.Enum
      case CompletionItemKind.Field => Nodes.Advice
      case CompletionItemKind.File => AllIcons.FileTypes.Any_type
      case CompletionItemKind.Function => Nodes.Field
      case CompletionItemKind.Interface => Nodes.Interface
      case CompletionItemKind.Keyword => Nodes.UpLevel
      case CompletionItemKind.Method => Nodes.Method
      case CompletionItemKind.Module => Nodes.Module
      case CompletionItemKind.Property => Nodes.Property
      case CompletionItemKind.Reference => Nodes.MethodReference
      case CompletionItemKind.Snippet => Nodes.Static
      case CompletionItemKind.Text => AllIcons.FileTypes.Text
      case CompletionItemKind.Unit => Nodes.Artifact
      case CompletionItemKind.Value => Nodes.DataSource
      case CompletionItemKind.Variable => Nodes.Variable
      case _ => null
    }
  }

  override def getStatusIcons: Map[ServerStatus, Icon] = {
    Map(ServerStatus.STOPPED -> STOPPED, ServerStatus.STARTING -> STARTING, ServerStatus.STARTED -> STARTED)
  }

  override def getSymbolIcon(kind: SymbolKind): Icon = {
    kind match {
      case SymbolKind.Array => null
      case SymbolKind.Boolean => null
      case SymbolKind.Class => Nodes.Class
      case SymbolKind.Constant => null
      case SymbolKind.Constructor => null
      case SymbolKind.Enum => Nodes.Enum
      case SymbolKind.Field => Nodes.Field
      case SymbolKind.File => AllIcons.FileTypes.Any_type
      case SymbolKind.Function => Nodes.Function
      case SymbolKind.Interface => Nodes.Interface
      case SymbolKind.Method => Nodes.Method
      case SymbolKind.Module => Nodes.Module
      case SymbolKind.Namespace => null
      case SymbolKind.Number => null
      case SymbolKind.Package => Nodes.Package
      case SymbolKind.Property => Nodes.Property
      case SymbolKind.String => null
      case SymbolKind.Variable => Nodes.Variable
      case _ => null
    }
  }

  override def isSpecificFor(serverDefinition: LanguageServerDefinition): Boolean = false
}
