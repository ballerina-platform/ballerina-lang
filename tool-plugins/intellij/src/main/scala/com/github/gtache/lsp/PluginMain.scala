package com.github.gtache.lsp

import java.util.concurrent.{TimeUnit, TimeoutException}

import com.github.gtache.lsp.client.languageserver.ServerStatus
import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import com.github.gtache.lsp.client.languageserver.wrapper.{LanguageServerWrapper, LanguageServerWrapperImpl}
import com.github.gtache.lsp.contributors.LSPNavigationItem
import com.github.gtache.lsp.editor.listeners.{EditorListener, FileDocumentManagerListenerImpl, VFSListener}
import com.github.gtache.lsp.requests.{Timeout, Timeouts}
import com.github.gtache.lsp.settings.BallerinaLSPState
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

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * The main class of the plugin
  */
object PluginMain {

  private val LOG: Logger = Logger.getInstance(classOf[PluginMain])
  private val extToLanguageWrapper: mutable.Map[(String, String), LanguageServerWrapper] = mutable.HashMap()
  private val projectToLanguageWrappers: mutable.Map[String, mutable.Set[LanguageServerWrapper]] = mutable.HashMap()
  private val forcedAssociationsInstances: mutable.Map[(String, String), LanguageServerWrapper] = mutable.HashMap()
  private var forcedAssociations: mutable.Map[(String, String), LanguageServerDefinition] = mutable.HashMap()
  private var extToServerDefinition: Map[String, LanguageServerDefinition] = HashMap()
  private var loadedExtensions: Boolean = false

  def resetAssociations(): Unit = {
    forcedAssociationsInstances.synchronized {
      forcedAssociationsInstances.foreach(t => t._2.disconnect(t._1._1))
      forcedAssociationsInstances.clear()
    }
    forcedAssociations.synchronized {
      forcedAssociations.clear()
      BallerinaLSPState.getInstance().setForcedAssociations(forcedAssociations.map(mapping => Array(mapping._1._1, mapping._1._2) -> mapping._2.toArray).asJava)
    }
  }

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
    setExtToServerDefinition(newExt.asScala)
  }

  /**
    * Sets the extensions->languageServer mapping
    *
    * @param newExt a Scala map
    */
  def setExtToServerDefinition(newExt: collection.Map[String, LanguageServerDefinition]): Unit = {
    val nullDef = newExt.filter(d => d._2 == null)
    val oldServerDef = extToServerDefinition
    val flattened = newExt.filter(d => d._2 != null).flatMap(t => t._1.split(LanguageServerDefinition.SPLIT_CHAR).map(ext => ext -> t._2)).toMap
    extToServerDefinition = flattened
    flattenExt()
    nullDef.foreach(ext => LOG.error("Definition for " + ext + " is null"))
    ApplicationUtils.pool(() => {
      val added = flattened.keys.filter(e => !oldServerDef.contains(e)).toSet
      val removed = oldServerDef.keys.filter(e => !flattened.contains(e)).toSet
      forcedAssociations.synchronized {
        forcedAssociationsInstances.synchronized {
          val newValues = flattened.values.toSet
          forcedAssociations = forcedAssociations.filter(t => newValues.contains(t._2))
          forcedAssociationsInstances.filter(t => !newValues.contains(t._2.getServerDefinition)).keys.foreach(k => {
            forcedAssociationsInstances(k).disconnect(k._1)
            forcedAssociationsInstances.remove(k)
          })
        }
      }
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

  /**
    * Called when an editor is opened. Instantiates a LanguageServerWrapper if necessary, and adds the Editor to the Wrapper
    *
    * @param editor the editor
    */
  def editorOpened(editor: Editor): Unit = {
    addExtensions()
    val file: VirtualFile = FileDocumentManager.getInstance.getFile(editor.getDocument)
    if (file != null) {
      ApplicationUtils.pool(() => {
        val ext: String = file.getExtension
        LOG.info("Opened " + file.getName)
        val uri = FileUtils.editorToURIString(editor)
        val pUri = FileUtils.editorToProjectFolderUri(editor)
        val forced = forcedAssociationsInstances.get((uri, pUri)).orNull
        if (forced == null) {
          val forcedDef = forcedAssociations.get((uri, pUri)).orNull
          if (forcedDef == null) {
            extToServerDefinition.get(ext).foreach(s => {
              val wrapper = getWrapperFor(ext, editor, s)
              LOG.info("Adding file " + file.getName)
              wrapper.connect(editor)
            })
          } else {
            val wrapper = getWrapperFor(ext, editor, forcedDef)
            LOG.info("Adding file " + file.getName)
            wrapper.connect(editor)
          }
        } else forced.connect(editor)
      })
    } else {
      LOG.warn("File for editor " + editor.getDocument.getText + " is null")
    }
  }

  def forceEditorOpened(editor: Editor, serverDefinition: LanguageServerDefinition, project: Project): Unit = {
    addExtensions()
    val file: VirtualFile = FileDocumentManager.getInstance.getFile(editor.getDocument)
    if (file != null) {
      val uri = FileUtils.editorToURIString(editor)
      val pUri = FileUtils.projectToUri(project)
      forcedAssociations.synchronized {
        forcedAssociations.update((uri, pUri), serverDefinition)
      }
      ApplicationUtils.pool(() => {
        LanguageServerWrapperImpl.forEditor(editor).foreach(l => {
          LOG.info("Disconnecting " + FileUtils.editorToURIString(editor))
          l.disconnect(editor)
        })
        LOG.info("Opened " + file.getName)
        val wrapper = getWrapperFor(serverDefinition.ext, editor, serverDefinition)
        BallerinaLSPState.getInstance().setForcedAssociations(forcedAssociations.map(mapping => Array(mapping._1._1, mapping._1._2) -> mapping._2.toArray).asJava)
        wrapper.connect(editor)
        LOG.info("Adding file " + file.getName)
      })
    } else {
      LOG.warn("File for editor " + editor.getDocument.getText + " is null")
    }
  }

  private def getWrapperFor(ext: String, editor: Editor, serverDefinition: LanguageServerDefinition): LanguageServerWrapper = {
    val project: Project = editor.getProject
    val rootPath: String = FileUtils.editorToProjectFolderPath(editor)
    val rootUri: String = FileUtils.pathToUri(rootPath)
    forcedAssociationsInstances.synchronized {
      val wrapper = forcedAssociationsInstances.get((FileUtils.editorToURIString(editor), FileUtils.projectToUri(project))).orNull
      if (wrapper == null || wrapper.getServerDefinition != serverDefinition) {

        extToLanguageWrapper.synchronized {
          var wrapper = extToLanguageWrapper.get((ext, rootUri)).orNull
          wrapper match {
            case null =>
              LOG.info("Instantiating wrapper for " + ext + " : " + rootUri)
              wrapper = new LanguageServerWrapperImpl(serverDefinition, project)
              val exts = serverDefinition.ext.split(LanguageServerDefinition.SPLIT_CHAR)
              exts.foreach(ext => extToLanguageWrapper.put((ext, rootUri), wrapper))
              extToLanguageWrapper.put((serverDefinition.ext, rootUri), wrapper)
              projectToLanguageWrappers.get(rootUri) match {
                case Some(set) =>
                  set.add(wrapper)
                case None =>
                  projectToLanguageWrappers.put(rootUri, mutable.Set(wrapper))
              }
            case _: LanguageServerWrapperImpl =>
              LOG.info("Wrapper already existing for " + ext + " , " + rootUri)
          }
          forcedAssociationsInstances.synchronized {
            forcedAssociations.foreach(t => {
              if (t._2 == serverDefinition && t._1._2 == rootUri) {
                forcedAssociationsInstances.update(t._1, wrapper)
              }
            })
            forcedAssociationsInstances.update((FileUtils.editorToURIString(editor), rootUri), wrapper)
          }
          wrapper
        }
      } else wrapper
    }
  }

  /**
    * Returns the extensions->languageServer mapping
    *
    * @return The Java map
    */
  def getExtToServerDefinitionJava: java.util.Map[String, LanguageServerDefinition] = {
    import scala.collection.JavaConverters._
    getExtToServerDefinition.asJava
  }

  /**
    * Returns the extensions->languageServer mapping
    *
    * @return the Scala map
    */
  def getExtToServerDefinition: Map[String, LanguageServerDefinition] = {
    addExtensions()
    extToServerDefinition
  }

  private def addExtensions(): Unit = {
    if (!loadedExtensions) {
      val extensions = LanguageServerDefinition.getAllDefinitions.filter(s => !extToServerDefinition.contains(s.ext))
      LOG.info("Added serverDefinitions " + extensions + " from plugins")
      extToServerDefinition = extToServerDefinition ++ extensions.map(s => (s.ext, s))
      flattenExt()
      loadedExtensions = true
    }
  }

  private def flattenExt(): Unit = {
    extToServerDefinition = extToServerDefinition.map(p => {
      val ext = p._1
      val sDef = p._2
      val split = ext.split(LanguageServerDefinition.SPLIT_CHAR)
      split.map(s => (s, sDef)) :+ (ext -> sDef)
    }).flatten.toMap
  }

  /**
    * Called when an editor is closed. Notifies the LanguageServerWrapper if needed
    *
    * @param editor the editor.
    */
  def editorClosed(editor: Editor): Unit = {
    ApplicationUtils.pool(() => {
      LanguageServerWrapperImpl.forEditor(editor).foreach(l => {
        LOG.info("Disconnecting " + FileUtils.editorToURIString(editor))
        l.disconnect(editor)
      })
    })
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

  def removeWrapper(wrapper: LanguageServerWrapper): Unit = {
    extToLanguageWrapper.remove((wrapper.getServerDefinition.ext, FileUtils.pathToUri(wrapper.getProject.getBasePath)))
  }

  def setForcedAssociations(associations: java.util.Map[Array[String], Array[String]]): Unit = {
    val scAssociations = associations.asScala
    if (!scAssociations.keys.forall(t => t.length == 2)) {
      LOG.warn("Unable to set forced associations : bad array length")
    } else {
      this.forcedAssociations = mutable.Map() ++ scAssociations.map(mapping => (mapping._1(0), mapping._1(1)) -> LanguageServerDefinition.fromArray(mapping._2))
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
    BallerinaLSPState.getInstance.getState //Need that to trigger loadState
    EditorFactory.getInstance.addEditorFactoryListener(new EditorListener, Disposer.newDisposable())
    VirtualFileManager.getInstance().addVirtualFileListener(VFSListener)
    ApplicationManager.getApplication.getMessageBus.connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, FileDocumentManagerListenerImpl)
    LOG.info("PluginMain init finished")
  }
}
