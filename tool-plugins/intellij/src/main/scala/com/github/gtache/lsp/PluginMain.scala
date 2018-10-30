package com.github.gtache.lsp

import java.util.concurrent.{TimeUnit, TimeoutException}

import com.github.gtache.lsp.client.languageserver.ServerStatus
import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import com.github.gtache.lsp.client.languageserver.wrapper.{LanguageServerWrapper, LanguageServerWrapperImpl}
import com.github.gtache.lsp.contributors.LSPNavigationItem
import com.github.gtache.lsp.editor.listeners.{EditorListener, FileDocumentManagerListenerImpl, VFSListener}
import com.github.gtache.lsp.requests.{Timeout, Timeouts}
import com.github.gtache.lsp.settings.LSPState
import com.github.gtache.lsp.utils.{ApplicationUtils, FileUtils, GUIUtils}
import com.intellij.AppTopics
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.{Editor, EditorFactory}
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, TextEditor}
import com.intellij.openapi.project.{Project, ProjectManager}
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.{VirtualFile, VirtualFileManager}
import org.eclipse.lsp4j._

import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * The main class of the plugin
  */
object PluginMain {

  private val LOG: Logger = Logger.getInstance(classOf[PluginMain])
  private val extToLanguageWrapper: mutable.Map[(String, String), LanguageServerWrapper] = mutable.HashMap()
  private val projectToLanguageWrappers: mutable.Map[String, mutable.Set[LanguageServerWrapper]] = mutable.HashMap()
  private var extToServerDefinition: Map[String, LanguageServerDefinition] = HashMap()


  /**
    * @return All instantiated ServerWrappers
    */
  def getAllServerWrappers: Set[LanguageServerWrapper] = {
    projectToLanguageWrappers.values.flatten.toSet
  }

  /**
    * @param ext An extension
    * @return True if there is a LanguageServer supporting this extension, false otherwise
    */
  def isExtensionSupported(ext: String): Boolean = {
    extToServerDefinition.contains(ext)
  }

  /**
    * Sets the extensions->languageServer mapping
    *
    * @param newExt a Java Map
    */
  def setExtToServerDefinition(newExt: java.util.Map[String, _ <: LanguageServerDefinition]): Unit = {
    import scala.collection.JavaConverters._
    setExtToServerDefinition(newExt.asScala)
  }

  /**
    * Sets the extensions->languageServer mapping
    *
    * @param newExt a Scala map
    */
  def setExtToServerDefinition(newExt: collection.Map[String, _ <: LanguageServerDefinition]): Unit = {
    val nullDef = newExt.filter(d => d._2 == null)
    val oldServerDef = extToServerDefinition
    extToServerDefinition = newExt.toMap.filter(d => d._2 != null)
    flattenExt()
    nullDef.foreach(ext => LOG.error("Definition for " + ext + " is null"))
    ApplicationUtils.pool(() => {
      val added = newExt.keys.filter(e => !oldServerDef.contains(e)).toSet
      val removed = oldServerDef.keys.filter(e => !newExt.contains(e)).toSet
      extToLanguageWrapper.keys.filter(k => removed.contains(k._1)).foreach(k => {
        val wrapper = extToLanguageWrapper(k)
        wrapper.stop()
        wrapper.removeWidget()
        extToLanguageWrapper.remove(k)
      })
      val openedEditors: Iterable[Editor] = ApplicationUtils.computableReadAction(() => ProjectManager.getInstance().getOpenProjects.flatMap(proj => FileEditorManager.getInstance(proj).getAllEditors()).collect { case t: TextEditor => t.getEditor })
      val files = openedEditors.map(e => FileDocumentManager.getInstance().getFile(e.getDocument))
      files.zip(openedEditors).foreach(f => if (added.contains(f._1.getExtension)) editorOpened(f._2))
    })
  }

  private def flattenExt(): Unit = {
    extToServerDefinition = extToServerDefinition.map(p => {
      val ext = p._1
      val sDef = p._2
      val split = ext.split(LanguageServerDefinition.SPLIT_CHAR)
      split.map(s => (s, sDef))
    }).flatten.toMap
  }

  /**
    * Called when an editor is opened. Instantiates a LanguageServerWrapper if necessary, and adds the Editor to the Wrapper
    *
    * @param editor the editor
    */
  def editorOpened(editor: Editor): Unit = {
    extToServerDefinition = HashMap()
    val extensions = LanguageServerDefinition.getAllDefinitions.filter(s => !extToServerDefinition.contains(s.ext))
    LOG.info("Added serverDefinitions " + extensions + " from plugins")
    extToServerDefinition = extToServerDefinition ++ extensions.map(s => (s.ext, s))
    flattenExt()

    val file: VirtualFile = FileDocumentManager.getInstance.getFile(editor.getDocument)
    if (file != null) {
      ApplicationUtils.pool(() => {
        val ext: String = file.getExtension
        val project: Project = editor.getProject
        val rootPath: String = FileUtils.editorToProjectFolderPath(editor)
        val rootUri: String = FileUtils.pathToUri(rootPath)
        LOG.info("Opened " + file.getName)
        extToServerDefinition.get(ext).foreach(s => {
          extToLanguageWrapper.synchronized {
            var wrapper = extToLanguageWrapper.get((ext, rootUri)).orNull
            wrapper match {
              case null =>
                LOG.info("Instantiating wrapper for " + ext + " : " + rootUri)
                wrapper = new LanguageServerWrapperImpl(s, project)
                val exts = s.ext.split(LanguageServerDefinition.SPLIT_CHAR)
                exts.foreach(ext => extToLanguageWrapper.put((ext, rootUri), wrapper))
                projectToLanguageWrappers.get(rootUri) match {
                  case Some(set) =>
                    set.add(wrapper)
                  case None =>
                    projectToLanguageWrappers.put(rootUri, mutable.Set(wrapper))
                }
              case _: LanguageServerWrapperImpl =>
                LOG.info("Wrapper already existing for " + ext + " , " + rootUri)
            }
            LOG.info("Adding file " + file.getName)
            wrapper.connect(editor)
          }
        })
      })

    } else {
      LOG.warn("File for editor " + editor.getDocument.getText + " is null")
    }
  }

  /**
    * Returns the extensions->languageServer mapping
    *
    * @return the Scala map
    */
  def getExtToServerDefinition: Map[String, LanguageServerDefinition] = extToServerDefinition

  /**
    * Returns the extensions->languageServer mapping
    *
    * @return The Java map
    */
  def getExtToServerDefinitionJava: java.util.Map[String, LanguageServerDefinition] = {
    import scala.collection.JavaConverters._
    extToServerDefinition.asJava
  }

  /**
    * Called when an editor is closed. Notifies the LanguageServerWrapper if needed
    *
    * @param editor the editor.
    */
  def editorClosed(editor: Editor): Unit = {
    val file: VirtualFile = FileDocumentManager.getInstance.getFile(editor.getDocument)
    if (file != null) {
      val ext: String = file.getExtension
      extToServerDefinition.get(ext) match {
        case Some(_) =>
          val uri = FileUtils.editorToURIString(editor)
          ApplicationUtils.pool(() => {
            LanguageServerWrapperImpl.forUri(uri).foreach(l => {
              LOG.info("Disconnecting " + uri)
              l.disconnect(uri)
            })
          })
        case None =>
          LOG.info("Closing LSP-unsupported file with extension " + ext)
      }
    } else {
      LOG.warn("File for document " + editor.getDocument.getText + " is null")
    }
  }


  /**
    * Returns the corresponding workspaceSymbols given a name and a project
    *
    * @param name                   The name to search for
    * @param pattern                The pattern (unused)
    * @param project                The project in which to search
    * @param includeNonProjectItems Whether to search in libraries for example (unused)
    * @param onlyKind               Filter the results to only the kinds in the set (all by default)
    * @return An array of NavigationItem
    */
  def workspaceSymbols(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean = false, onlyKind: Set[SymbolKind] = Set()): Array[NavigationItem] = {
    projectToLanguageWrappers.get(FileUtils.pathToUri(project.getBasePath)) match {
      case Some(set) =>
        val params: WorkspaceSymbolParams = new WorkspaceSymbolParams(name)
        val servDefToReq = set.collect {
          case w: LanguageServerWrapper if w.getStatus == ServerStatus.STARTED && w.getRequestManager != null =>
            (w, w.getRequestManager.symbol(params))
        }.toSet.filter(w => w._2 != null)
        if (!servDefToReq.contains(null)) {
          import scala.collection.JavaConverters._
          val servDefToSymb = servDefToReq.map(w => {
            try {
              val symbols = w._2.get(Timeout.SYMBOLS_TIMEOUT, TimeUnit.MILLISECONDS)
              w._1.notifyResult(Timeouts.SYMBOLS, success = true)
              (w._1, if (symbols != null) symbols.asScala
                .filter(s => if (onlyKind.isEmpty) true else onlyKind.contains(s.getKind)) else null)
            } catch {
              case e: TimeoutException =>
                LOG.warn(e)
                w._1.notifyResult(Timeouts.SYMBOLS, success = false)
                null
            }
          }
          ).filter(r => r._2 != null)
          servDefToSymb.flatMap(res => {
            val definition = res._1
            val symbols = res._2
            symbols.map(symb => {
              val start = symb.getLocation.getRange.getStart
              val uri = FileUtils.URIToVFS(symb.getLocation.getUri)
              val iconProvider = GUIUtils.getIconProviderFor(definition.getServerDefinition)
              LSPNavigationItem(symb.getName, symb.getContainerName, project, uri, start.getLine, start.getCharacter, iconProvider.getSymbolIcon(symb.getKind))
            })
          }).toArray.asInstanceOf[Array[NavigationItem]]

        } else Array.empty
      case None => LOG.info("No wrapper for project " + project.getBasePath)
        Array.empty
    }
  }
}

/**
  * The main class of the plugin
  */
class PluginMain extends ApplicationComponent {

  import PluginMain._

  override val getComponentName: String = "PluginMain"

  override def initComponent(): Unit = {
    LSPState.getInstance.getState //Need that to trigger loadState
    EditorFactory.getInstance.addEditorFactoryListener(new EditorListener, Disposer.newDisposable())
    VirtualFileManager.getInstance().addVirtualFileListener(VFSListener)
    ApplicationManager.getApplication.getMessageBus.connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, FileDocumentManagerListenerImpl)
    LOG.info("PluginMain init finished")
  }
}
