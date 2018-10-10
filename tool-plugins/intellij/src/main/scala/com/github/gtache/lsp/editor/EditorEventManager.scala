package com.github.gtache.lsp.editor

import java.awt._
import java.awt.event.{KeyEvent, MouseAdapter, MouseEvent}
import java.io.File
import java.net.URI
import java.util.concurrent.{ExecutionException, TimeUnit, TimeoutException}
import java.util.{Collections, Timer, TimerTask}

import com.github.gtache.lsp.client.languageserver.ServerOptions
import com.github.gtache.lsp.client.languageserver.requestmanager.RequestManager
import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapperImpl
import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.contributors.rename.LSPRenameProcessor
import com.github.gtache.lsp.requests.{HoverHandler, Timeouts, WorkspaceEditHandler}
import com.github.gtache.lsp.utils.{DocumentUtils, FileUtils, GUIUtils}
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.lookup._
import com.intellij.lang.LanguageDocumentation
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event._
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.editor.markup._
import com.intellij.openapi.editor.{Editor, LogicalPosition}
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.{PsiDocumentManager, PsiElement}
import com.intellij.ui.Hint
import com.intellij.uiDesigner.core.{GridConstraints, GridLayoutManager, Spacer}
import javax.swing.{JFrame, JLabel, JPanel}
import org.eclipse.lsp4j._
import org.eclipse.lsp4j.jsonrpc.JsonRpcException

import scala.collection.mutable

object EditorEventManager {
  private val HOVER_TIME_THRES: Long = EditorSettingsExternalizable.getInstance().getQuickDocOnMouseOverElementDelayMillis * 1000000
  private val SCHEDULE_THRES = 10000000 //Time before the Timer is scheduled
  private val POPUP_THRES = HOVER_TIME_THRES / 1000000 + 20
  private val CTRL_THRES = 500000000 //Time between requests when ctrl is pressed (500ms)

  private val uriToManager: mutable.Map[String, EditorEventManager] = mutable.HashMap()
  private val editorToManager: mutable.Map[Editor, EditorEventManager] = mutable.HashMap()

  @volatile private var isKeyPressed = false
  @volatile private var isCtrlDown = false
  @volatile private var ctrlRange: CtrlRangeMarker = _

  KeyboardFocusManager.getCurrentKeyboardFocusManager.addKeyEventDispatcher((e: KeyEvent) => this.synchronized {
    e.getID match {
      case KeyEvent.KEY_PRESSED =>
        isKeyPressed = true
        if (e.getKeyCode == KeyEvent.VK_CONTROL) isCtrlDown = true
      case KeyEvent.KEY_RELEASED =>
        isKeyPressed = false
        if (e.getKeyCode == KeyEvent.VK_CONTROL) {
          isCtrlDown = false
          if (ctrlRange != null) ctrlRange.dispose()
          ctrlRange = null
        }
      case _ =>
    }
    false
  })

  /**
    * @param uri A file uri
    * @return The manager for the given uri, or None
    */
  def forUri(uri: String): Option[EditorEventManager] = {
    prune()
    uriToManager.get(uri)
  }

  /**
    * @param editor An editor
    * @return The manager for the given editor, or None
    */
  def forEditor(editor: Editor): Option[EditorEventManager] = {
    prune()
    editorToManager.get(editor)
  }

  /**
    * Tells the server that all the documents will be saved
    */
  def willSaveAll(): Unit = {
    prune()
    editorToManager.foreach(e => e._2.willSave())
  }

  private def prune(): Unit = {
    editorToManager.filter(e => !e._2.wrapper.isActive).keys.foreach(editorToManager.remove)
    uriToManager.filter(e => !e._2.wrapper.isActive).keys.foreach(uriToManager.remove)
  }
}

/**
  * Class handling events related to an Editor (a Document)
  *
  * @param editor              The "watched" editor
  * @param mouseListener       A listener for mouse clicks
  * @param mouseMotionListener A listener for mouse movement
  * @param documentListener    A listener for keystrokes
  * @param selectionListener   A listener for selection changes in the editor
  * @param requestManager      The related RequestManager, connected to the right LanguageServer
  * @param serverOptions       the options of the server regarding completion, signatureHelp, syncKind, etc
  * @param wrapper             The corresponding LanguageServerWrapper
  */
class EditorEventManager(val editor: Editor, val mouseListener: EditorMouseListener, val mouseMotionListener: EditorMouseMotionListener,
                         val documentListener: DocumentListener, val selectionListener: SelectionListener,
                         val requestManager: RequestManager, val serverOptions: ServerOptions, val wrapper: LanguageServerWrapperImpl) {

  import EditorEventManager._
  import GUIUtils.createAndShowEditorHint
  import com.github.gtache.lsp.requests.Timeout._
  import com.github.gtache.lsp.utils.ApplicationUtils._

  import scala.collection.JavaConverters._

  private val identifier: TextDocumentIdentifier = new TextDocumentIdentifier(FileUtils.editorToURIString(editor))
  private val LOG: Logger = Logger.getInstance(classOf[EditorEventManager])
  private val changesParams = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(), Collections.singletonList(new TextDocumentContentChangeEvent()))
  private val selectedSymbHighlights: mutable.Set[RangeHighlighter] = mutable.HashSet()
  private val diagnosticsHighlights: mutable.Set[DiagnosticRangeHighlighter] = mutable.HashSet()
  private val syncKind = serverOptions.syncKind

  private val completionTriggers =
    if (serverOptions.completionOptions != null && serverOptions.completionOptions.getTriggerCharacters != null)
      serverOptions.completionOptions.getTriggerCharacters.asScala.toSet.filter(s => s != ".")
    else Set[String]()

  private val signatureTriggers =
    if (serverOptions.signatureHelpOptions != null && serverOptions.signatureHelpOptions.getTriggerCharacters != null)
      serverOptions.signatureHelpOptions.getTriggerCharacters.asScala.toSet
    else Set[String]()

  private val documentOnTypeFormattingOptions = serverOptions.documentOnTypeFormattingOptions
  private val onTypeFormattingTriggers =
    if (documentOnTypeFormattingOptions != null && documentOnTypeFormattingOptions.getMoreTriggerCharacter != null)
      (documentOnTypeFormattingOptions.getMoreTriggerCharacter.asScala += documentOnTypeFormattingOptions.getFirstTriggerCharacter).toSet
    else if (documentOnTypeFormattingOptions != null)
      Set(documentOnTypeFormattingOptions.getFirstTriggerCharacter)
    else
      Set[String]()

  private val project: Project = editor.getProject
  @volatile var needSave = false
  private var hoverThread = new Timer("Hover", true)
  private var version: Int = -1
  private var predTime: Long = -1L
  private var ctrlTime: Long = -1L
  private var isOpen: Boolean = false
  private var mouseInEditor: Boolean = true
  private var currentHint: Hint = _

  uriToManager.put(FileUtils.editorToURIString(editor), this)
  editorToManager.put(editor, this)
  changesParams.getTextDocument.setUri(identifier.getUri)

  /**
    * Calls onTypeFormatting or signatureHelp if the character typed was a trigger character
    *
    * @param c The character just typed
    */
  def characterTyped(c: Char): Unit = {
    if (completionTriggers.contains(c.toString)) {
      //completion(DocumentUtils.offsetToLSPPos(editor,editor.getCaretModel.getCurrentCaret.getOffset))
    } else if (signatureTriggers.contains(c.toString)) {
      signatureHelp()
    } else if (onTypeFormattingTriggers.contains(c.toString)) {
      onTypeFormatting(c.toString)
    }
  }

  /**
    * Calls signatureHelp at the current editor caret position
    */
  def signatureHelp(): Unit = {
    val lPos = editor.getCaretModel.getCurrentCaret.getLogicalPosition
    val point = editor.logicalPositionToXY(lPos)
    val params = new TextDocumentPositionParams(identifier, computableReadAction(() => DocumentUtils.logicalToLSPPos(lPos, editor)))
    pool(() => {
      if (!editor.isDisposed) {
        val future = requestManager.signatureHelp(params)
        if (future != null) {
          try {
            val signature = future.get(SIGNATURE_TIMEOUT, TimeUnit.MILLISECONDS)
            wrapper.notifySuccess(Timeouts.SIGNATURE)
            if (signature != null) {
              val signatures = signature.getSignatures
              if (signatures != null && !signatures.isEmpty) {
                val scalaSignatures = signatures.asScala
                val activeSignatureIndex = signature.getActiveSignature
                val activeParameterIndex = signature.getActiveParameter
                val activeParameter = scalaSignatures(activeSignatureIndex).getParameters.get(activeParameterIndex).getLabel
                val builder = StringBuilder.newBuilder
                builder.append("<html>")
                scalaSignatures.take(activeSignatureIndex).foreach(sig => builder.append(sig.getLabel).append("<br>"))
                builder.append("<b>").append(scalaSignatures(activeSignatureIndex).getLabel
                  .replace(activeParameter, "<font color=\"yellow\">" + activeParameter + "</font>")).append("</b>")
                scalaSignatures.drop(activeSignatureIndex + 1).foreach(sig => builder.append("<br>").append(sig.getLabel))
                builder.append("</html>")
                invokeLater(() => currentHint = createAndShowEditorHint(editor, builder.toString(), point, HintManager.UNDER, HintManager.HIDE_BY_OTHER_HINT))
              }
            }
          } catch {
            case e: TimeoutException =>
              LOG.warn(e)
              wrapper.notifyFailure(Timeouts.SIGNATURE)
            case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
              LOG.warn(e)
              wrapper.crashed(e.asInstanceOf[Exception])
          }
        }
      }
    })
  }

  /**
    * Formats the document when a trigger character is typed
    *
    * @param c The trigger character
    */
  private def onTypeFormatting(c: String): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        val params = new DocumentOnTypeFormattingParams()
        params.setCh(c)
        params.setPosition(computableReadAction(() => DocumentUtils.logicalToLSPPos(editor.getCaretModel.getCurrentCaret.getLogicalPosition, editor)))
        params.setTextDocument(identifier)
        params.setOptions(new FormattingOptions())
        val future = requestManager.onTypeFormatting(params)
        if (future != null) {
          try {
            val edits = future.get(FORMATTING_TIMEOUT, TimeUnit.MILLISECONDS)
            wrapper.notifySuccess(Timeouts.FORMATTING)
            if (edits != null) invokeLater(() => applyEdit(edits = edits.asScala, name = "On type formatting"))
          } catch {
            case e: TimeoutException =>
              LOG.warn(e)
              wrapper.notifyFailure(Timeouts.FORMATTING)
            case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
              LOG.warn(e)
              wrapper.crashed(e.asInstanceOf[Exception])
          }
        }
      }
    })
  }

  /**
    * Applies the given edits to the document
    *
    * @param version    The version of the edits (will be discarded if older than current version)
    * @param edits      The edits to apply
    * @param name       The name of the edits (Rename, for example)
    * @param closeAfter will close the file after edits if set to true
    * @return True if the edits were applied, false otherwise
    */
  def applyEdit(version: Int = Int.MaxValue, edits: Iterable[TextEdit], name: String = "Apply LSP edits", closeAfter: Boolean = false): Boolean = {
    val runnable = getEditsRunnable(version, edits, name)
    writeAction(() => {
      if (runnable != null) CommandProcessor.getInstance().executeCommand(project, runnable, name, "LSPPlugin", editor.getDocument)
      if (closeAfter) {
        FileEditorManager.getInstance(project)
          .closeFile(PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument).getVirtualFile)
      }
    })
    if (runnable != null) true else false
  }

  /**
    * Returns a Runnable used to apply the given edits and save the document
    * Used by WorkspaceEditHandler (allows to revert a rename for example)
    *
    * @param version The edit version
    * @param edits   The edits
    * @param name    The name of the edit
    * @return The runnable
    */
  def getEditsRunnable(version: Int = Int.MaxValue, edits: Iterable[TextEdit], name: String = "Apply LSP edits"): Runnable = {
    if (version >= this.version) {
      val document = editor.getDocument
      if (document.isWritable) {
        () => {
          edits.foreach(edit => {
            val text = edit.getNewText
            val range = edit.getRange
            val start = DocumentUtils.LSPPosToOffset(editor, range.getStart)
            val end = DocumentUtils.LSPPosToOffset(editor, range.getEnd)
            if (text == "" || text == null) {
              document.deleteString(start, end)
            } else if (end - start <= 0) {
              document.insertString(start, text)
            } else {
              document.replaceString(start, end, text)
            }
          })
          saveDocument()
        }
      } else {
        LOG.warn("Document is not writable")
        null
      }
    } else {
      LOG.warn("Edit version " + version + " is older than current version " + this.version)
      null
    }
  }

  private def saveDocument(): Unit = {
    invokeLater(() => writeAction(() => FileDocumentManager.getInstance().saveDocument(editor.getDocument)))
  }

  /**
    * Retrieves the commands needed to apply a CodeAction
    *
    * @param element The element which needs the CodeAction
    * @return The list of commands, or null if none are given / the request times out
    */
  def codeAction(element: LSPPsiElement): Iterable[Command] = {
    val params = new CodeActionParams()
    params.setTextDocument(identifier)
    val range = new Range(DocumentUtils.offsetToLSPPos(editor, element.start), DocumentUtils.offsetToLSPPos(editor, element.end))
    params.setRange(range)
    val context = new CodeActionContext(diagnosticsHighlights.map(_.diagnostic).toList.asJava)
    params.setContext(context)
    val future = requestManager.codeAction(params)
    if (future != null) {
      try {
        val res = future.get(CODEACTION_TIMEOUT, TimeUnit.MILLISECONDS).asScala
        wrapper.notifySuccess(Timeouts.CODEACTION)
        res
      } catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.CODEACTION)
          null
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
          null
      }

    } else {
      null
    }
  }

  /**
    * Returns the completion suggestions given a position
    *
    * @param pos The LSP position
    * @return The suggestions
    */
  def completion(pos: Position): Iterable[_ <: LookupElement] = {
    val request = requestManager.completion(new CompletionParams(identifier, pos))
    if (request != null) {
      try {
        val res = request.get(COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS)
        wrapper.notifySuccess(Timeouts.COMPLETION)
        if (res != null) {
          import scala.collection.JavaConverters._
          val completion /*: CompletionList | List[CompletionItem] */ = if (res.isLeft) res.getLeft.asScala else res.getRight

          /**
            * Creates a LookupElement given a CompletionItem
            *
            * @param item The CompletionItem
            * @return The corresponding LookupElement
            */
          def createLookupItem(item: CompletionItem): LookupElement = {
            val addTextEdits = item.getAdditionalTextEdits
            val command = item.getCommand
            val data = item.getData
            val detail = item.getDetail
            val doc = item.getDocumentation
            val filterText = item.getFilterText
            val insertText = item.getInsertText
            val insertFormat = item.getInsertTextFormat
            val kind = item.getKind
            val label = item.getLabel
            val textEdit = item.getTextEdit
            val sortText = item.getSortText
            val presentableText = if (label != null && label != "") label else if (insertText != null) insertText else ""
            val tailText = if (detail != null) detail else ""
            val iconProvider = GUIUtils.getIconProviderFor(wrapper.getServerDefinition)
            val icon = iconProvider.getCompletionIcon(kind)
            var lookupElementBuilder: LookupElementBuilder = null
            /*            .withRenderer((element: LookupElement, presentation: LookupElementPresentation) => { //TODO later
                      presentation match {
                        case realPresentation: RealLookupElementPresentation =>
                          if (!realPresentation.hasEnoughSpaceFor(presentation.getItemText, presentation.isItemTextBold)) {
                          }
                      }
                    })*/
            if (textEdit != null) {
              if (addTextEdits != null) {
                lookupElementBuilder = LookupElementBuilder.create("")
                  .withInsertHandler((context: InsertionContext, item: LookupElement) => {
                    context.commitDocument()
                    invokeLater(() => {
                      applyEdit(edits = addTextEdits.asScala :+ textEdit, name = "Completion : " + label)
                      if (command != null) executeCommands(Iterable(command))
                    })
                  })
              } else {
                lookupElementBuilder = LookupElementBuilder.create("")
                  .withInsertHandler((context: InsertionContext, item: LookupElement) => {
                    context.commitDocument()
                    invokeLater(() => {
                      applyEdit(edits = Seq(textEdit), name = "Completion : " + label)
                      if (command != null) executeCommands(Iterable(command))
                    })
                  })
              }
            } else if (addTextEdits != null) {
              lookupElementBuilder = LookupElementBuilder.create("")
                .withInsertHandler((context: InsertionContext, item: LookupElement) => {
                  context.commitDocument()
                  invokeLater(() => {
                    applyEdit(edits = addTextEdits.asScala, name = "Completion : " + label)
                    if (command != null) executeCommands(Iterable(command))
                  })
                })
            } else {
              lookupElementBuilder = LookupElementBuilder.create(if (insertText != null && insertText != "") insertText else label)
              if (command != null) lookupElementBuilder = lookupElementBuilder.withInsertHandler((context: InsertionContext, item: LookupElement) => {
                context.commitDocument()
                invokeLater(() => {
                  executeCommands(Iterable(command))
                })
              })
            }
            if (kind == CompletionItemKind.Keyword) lookupElementBuilder = lookupElementBuilder.withBoldness(true)
            lookupElementBuilder.withPresentableText(presentableText).withTailText(tailText, true).withIcon(icon).withAutoCompletionPolicy(AutoCompletionPolicy.SETTINGS_DEPENDENT)
          }

          completion match {
            case c: CompletionList =>
              c.getItems.asScala.map(item => {
                createLookupItem(item)
              })
            case l: Iterable[CompletionItem@unchecked] => l.map(item => {
              createLookupItem(item)
            })
          }
        } else Iterable()
      }
      catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.COMPLETION)
          Iterable.empty
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
          Iterable.empty
      }
    } else Iterable.empty
  }

  /**
    * Sends commands to execute to the server and applies the changes returned if the future returns a WorkspaceEdit
    *
    * @param commands The commands to execute
    */
  def executeCommands(commands: Iterable[Command]): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        commands.map(c => {
          val params = new ExecuteCommandParams()
          params.setArguments(c.getArguments)
          params.setCommand(c.getCommand)
          requestManager.executeCommand(params)
        }).foreach(f => {
          if (f != null) {
            try {
              val ret = f.get(EXECUTE_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS)
              wrapper.notifySuccess(Timeouts.EXECUTE_COMMAND)
              ret match {
                case e: WorkspaceEdit => WorkspaceEditHandler.applyEdit(e, name = "Execute command")
                case _ =>
              }
            } catch {
              case e: TimeoutException =>
                LOG.warn(e)
                wrapper.notifyFailure(Timeouts.EXECUTE_COMMAND)
              case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
                LOG.warn(e)
                wrapper.crashed(e.asInstanceOf[Exception])
            }
          }
        })
      }
    })
  }

  /**
    * Applies the diagnostics to the document
    *
    * @param diagnostics The diagnostics to apply from the server
    */
  def diagnostics(diagnostics: Iterable[Diagnostic]): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        invokeLater(() => {
          diagnosticsHighlights.foreach(highlight => editor.getMarkupModel.removeHighlighter(highlight.rangeHighlighter))
          diagnosticsHighlights.clear()
        })
        for (diagnostic <- diagnostics) {
          val code = diagnostic.getCode
          val message = diagnostic.getMessage
          val source = diagnostic.getSource
          val range = diagnostic.getRange
          val severity = diagnostic.getSeverity
          LOG.info("code : " + code + " message : " + message + " source : " + source + " range : " + range)
          val (start, end) = computableReadAction(() => (DocumentUtils.LSPPosToOffset(editor, range.getStart), DocumentUtils.LSPPosToOffset(editor, range.getEnd)))

          val markupModel = editor.getMarkupModel
          val colorScheme = editor.getColorsScheme

          val (effectType, effectColor, layer) = severity match {
            case null => null
            case DiagnosticSeverity.Error => (EffectType.WAVE_UNDERSCORE, java.awt.Color.RED, HighlighterLayer.ERROR)
            case DiagnosticSeverity.Warning => (EffectType.WAVE_UNDERSCORE, java.awt.Color.YELLOW, HighlighterLayer.WARNING)
            case DiagnosticSeverity.Information => (EffectType.WAVE_UNDERSCORE, java.awt.Color.GRAY, HighlighterLayer.WARNING)
            case DiagnosticSeverity.Hint => (EffectType.BOLD_DOTTED_LINE, java.awt.Color.GRAY, HighlighterLayer.WARNING)
          }
          invokeLater(() => {
            if (!editor.isDisposed) {
              diagnosticsHighlights
                .add(DiagnosticRangeHighlighter(markupModel.addRangeHighlighter(start, end, layer,
                  new TextAttributes(colorScheme.getDefaultForeground, colorScheme.getDefaultBackground, effectColor, effectType, Font.PLAIN), HighlighterTargetArea.EXACT_RANGE),
                  diagnostic))
            }
          })
        }
      }
    })
  }

  /**
    * Handles the DocumentChanged events
    *
    * @param event The DocumentEvent
    */
  def documentChanged(event: DocumentEvent): Unit = {
    if (!editor.isDisposed) {
      if (event.getDocument == editor.getDocument) {
        predTime = System.nanoTime() //So that there are no hover events while typing
        changesParams.getTextDocument.setVersion({
          version += 1
          version - 1
        })
        syncKind match {
          case TextDocumentSyncKind.None =>
          case TextDocumentSyncKind.Incremental =>
            val changeEvent = changesParams.getContentChanges.get(0)
            val newText = event.getNewFragment
            val offset = event.getOffset
            val newTextLength = event.getNewLength
            val lspPosition: Position = DocumentUtils.offsetToLSPPos(editor, offset)
            val startLine = lspPosition.getLine
            val startColumn = lspPosition.getCharacter
            val oldText = event.getOldFragment

            //if text was deleted/replaced, calculate the end position of inserted/deleted text
            val (endLine, endColumn) = if (oldText.length() > 0) {
              val line = startLine + StringUtil.countNewLines(oldText)
              val oldLines = oldText.toString.split('\n')
              val oldTextLength = if (oldLines.isEmpty) 0 else oldLines.last.length
              val column = if (oldLines.length == 1) startColumn + oldTextLength else oldTextLength
              (line, column)
            } else (startLine, startColumn) //if insert or no text change, the end position is the same
          val range = new Range(new Position(startLine, startColumn), new Position(endLine, endColumn))
            changeEvent.setRange(range)
            changeEvent.setRangeLength(newTextLength)
            changeEvent.setText(newText.toString)

          case TextDocumentSyncKind.Full =>
            changesParams.getContentChanges.get(0).setText(editor.getDocument.getText())
        }
        requestManager.didChange(changesParams)
      } else {
        LOG.error("Wrong document for the EditorEventManager")
      }
    }
  }

  /**
    * Notifies the server that the corresponding document has been closed
    */
  def documentClosed(): Unit = {
    pool(() => {
      if (isOpen) {
        requestManager.didClose(new DidCloseTextDocumentParams(identifier))
        isOpen = false
        editorToManager.remove(editor)
        uriToManager.remove(FileUtils.editorToURIString(editor))
      } else {
        LOG.warn("Editor " + identifier.getUri + " was already closed")
      }
    })
  }

  def documentOpened(): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        if (isOpen) {
          LOG.warn("Editor " + editor + " was already open")
        } else {
          requestManager.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(identifier.getUri, wrapper.serverDefinition.id, {
            version += 1
            version - 1
          }, editor.getDocument.getText)))
          isOpen = true
        }
      }
    })
  }

  /**
    * Notifies the server that the corresponding document has been saved
    */
  def documentSaved(): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        val params: DidSaveTextDocumentParams = new DidSaveTextDocumentParams(identifier, editor.getDocument.getText)
        requestManager.didSave(params)
      }
    })
  }

  /**
    * Indicates that the document will be saved
    */
  //TODO Manual
  def willSave(): Unit = {
    if (wrapper.isWillSaveWaitUntil && !needSave) willSaveWaitUntil() else pool(() => {
      if (!editor.isDisposed) requestManager.willSave(new WillSaveTextDocumentParams(identifier, TextDocumentSaveReason.Manual))
    })
  }

  /**
    * If the server supports willSaveWaitUntil, the LSPVetoer will check if  a save is needed
    * (needSave will basically alterate between true or false, so the document will always be saved)
    */
  private def willSaveWaitUntil(): Unit = {
    if (wrapper.isWillSaveWaitUntil) {
      pool(() => {
        if (!editor.isDisposed) {
          val params = new WillSaveTextDocumentParams(identifier, TextDocumentSaveReason.Manual)
          val future = requestManager.willSaveWaitUntil(params)
          if (future != null) {
            try {
              val edits = future.get(WILLSAVE_TIMEOUT, TimeUnit.MILLISECONDS)
              wrapper.notifySuccess(Timeouts.WILLSAVE)
              if (edits != null) {
                invokeLater(() => applyEdit(edits = edits.asScala, name = "WaitUntil edits"))
              }
            } catch {
              case e: TimeoutException =>
                LOG.warn(e)
                wrapper.notifyFailure(Timeouts.WILLSAVE)
              case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
                LOG.warn(e)
                wrapper.crashed(e.asInstanceOf[Exception])
            } finally {
              needSave = true
              saveDocument()
            }
          } else {
            needSave = true
            saveDocument()
          }
        }
      })
    } else {
      LOG.error("Server doesn't support WillSaveWaitUntil")
      needSave = true
      saveDocument()
    }
  }

  /**
    * Gets references, synchronously
    *
    * @param offset The offset of the element
    * @return A list of start/end offset
    */
  def documentReferences(offset: Int): Iterable[(Int, Int)] = {
    val params = new ReferenceParams()
    val context = new ReferenceContext()
    context.setIncludeDeclaration(true)
    params.setContext(context)
    params.setPosition(DocumentUtils.offsetToLSPPos(editor, offset))
    params.setTextDocument(identifier)
    val future = requestManager.references(params)
    if (future != null) {
      try {
        val references = future.get(REFERENCES_TIMEOUT, TimeUnit.MILLISECONDS)
        wrapper.notifySuccess(Timeouts.REFERENCES)
        if (references != null) {
          references.asScala.collect {
            case l: Location if l.getUri == identifier.getUri =>
              computableReadAction(() => (DocumentUtils.LSPPosToOffset(editor, l.getRange.getStart), DocumentUtils.LSPPosToOffset(editor, l.getRange.getEnd)))
          }
        } else {
          null
        }
      } catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.REFERENCES)
          null
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
          null
      }
    }
    else {
      null
    }
  }

  /**
    * @return The current diagnostics highlights
    */
  def getDiagnostics: mutable.Set[DiagnosticRangeHighlighter] = {
    diagnosticsHighlights.clone()
  }

  def getElementAtOffset(offset: Int): LSPPsiElement = {
    computableReadAction(() => {
      if (!editor.isDisposed) {
        val params = new TextDocumentPositionParams(identifier, DocumentUtils.offsetToLSPPos(editor, offset))
        val future = requestManager.documentHighlight(params)
        if (future != null) {
          try {
            val res = future.get(DOC_HIGHLIGHT_TIMEOUT, TimeUnit.MILLISECONDS)
            wrapper.notifySuccess(Timeouts.DOC_HIGHLIGHT)
            if (res != null && !editor.isDisposed)
              res.asScala.map(dh => new TextRange(DocumentUtils.LSPPosToOffset(editor, dh.getRange.getStart), DocumentUtils.LSPPosToOffset(editor, dh.getRange.getEnd)))
                .find(range => range.getStartOffset <= offset && offset <= range.getEndOffset)
                .map(range => LSPPsiElement(editor.getDocument.getText(range), project, range.getStartOffset, range.getEndOffset, PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument)))
                .orNull
            else null
          } catch {
            case e: TimeoutException =>
              wrapper.notifyFailure(Timeouts.DOC_HIGHLIGHT)
              LOG.warn(e)
              null
            case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
              LOG.warn(e)
              wrapper.crashed(e.asInstanceOf[Exception])
              null
          }
        } else null
      }
      else null
    })
  }

  /**
    * Called when the mouse is clicked
    * At the moment, is used by CTRL+click to see references / goto definition
    *
    * @param e The mouse event
    */
  def mouseClicked(e: EditorMouseEvent): Unit = {
    if (isCtrlDown) {
      if (ctrlRange != null) {
        createCtrlRange(computableReadAction(() => DocumentUtils.logicalToLSPPos(editor.xyToLogicalPosition(e.getMouseEvent.getPoint), editor)), null)
      }
      if (ctrlRange != null) {
        val loc = ctrlRange.loc
        invokeLater(() => {
          if (!editor.isDisposed) {
            val offset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(e.getMouseEvent.getPoint))
            if (identifier.getUri == loc.getUri && DocumentUtils.LSPPosToOffset(editor, loc.getRange.getStart) <= offset && offset <= DocumentUtils.LSPPosToOffset(editor, loc.getRange.getEnd)) {
              showReferences(offset)
            } else {
              val file = LocalFileSystem.getInstance().findFileByIoFile(new File(new URI(FileUtils.sanitizeURI(loc.getUri))))
              val descriptor = new OpenFileDescriptor(project, file)
              writeAction(() => {
                val newEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
                val startOffset = DocumentUtils.LSPPosToOffset(newEditor, loc.getRange.getStart)
                newEditor.getCaretModel.getCurrentCaret.moveToOffset(startOffset)
                newEditor.getSelectionModel.setSelection(startOffset, DocumentUtils.LSPPosToOffset(newEditor, loc.getRange.getEnd))
              })
            }
            if (ctrlRange != null) ctrlRange.dispose()
            ctrlRange = null
          }
        })
      }
    }
  }

  /**
    * Queries references and show them in a window, given the offset of the symbol in the editor
    *
    * @param offset The offset
    */
  def showReferences(offset: Int): Unit = {
    invokeLater(() => {
      if (!editor.isDisposed) {
        writeAction(() => editor.getCaretModel.getCurrentCaret.moveToOffset(offset))
        showReferences(includeDefinition = false)
      }
    })
  }

  /**
    * Queries references and show a window with these references (click on a row to get to the location)
    */
  def showReferences(includeDefinition: Boolean = true): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        val context = new ReferenceContext(includeDefinition)
        val params = new ReferenceParams(context)
        params.setTextDocument(identifier)
        val serverPos = computableReadAction(() => {
          DocumentUtils.logicalToLSPPos(editor.getCaretModel.getCurrentCaret.getLogicalPosition, editor)
        })
        params.setPosition(serverPos)
        val future = requestManager.references(params)
        if (future != null) {
          try {
            val references = future.get(REFERENCES_TIMEOUT, TimeUnit.MILLISECONDS)
            wrapper.notifySuccess(Timeouts.REFERENCES)
            if (references != null) {
              invokeLater(() => {
                if (!editor.isDisposed) showReferences(references.asScala)
              })
            }
          } catch {
            case e: TimeoutException =>
              LOG.warn(e)
              wrapper.notifyFailure(Timeouts.REFERENCES)
            case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
              LOG.warn(e)
              wrapper.crashed(e.asInstanceOf[Exception])
          }
        }
      }
    })
  }

  private def showReferences(references: Iterable[Location]): Unit = {
    var name: String = ""

    /**
      * Opens the editor and get the required infos
      *
      * @param file              The file of the editor
      * @param fileEditorManager The fileEditorManager
      * @param start             The starting position
      * @param end               The ending position
      * @return (StartOffset, EndOffset, Name (of the symbol), line (containing the symbol))
      */
    def openEditorAndGetOffsetsAndName(file: VirtualFile, fileEditorManager: FileEditorManager, start: Position, end: Position): (Int, Int, String, String) = {
      val descriptor = new OpenFileDescriptor(project, file)
      computableWriteAction(() => {
        val newEditor = fileEditorManager.openTextEditor(descriptor, false)
        val startOffset = DocumentUtils.LSPPosToOffset(newEditor, start)
        val endOffset = DocumentUtils.LSPPosToOffset(newEditor, end)
        val doc = newEditor.getDocument
        val name = doc.getText(new TextRange(startOffset, endOffset))
        fileEditorManager.closeFile(file)
        (startOffset, endOffset, name, DocumentUtils.getLineText(newEditor, startOffset, endOffset))
      })
    }

    val locations = references.map(l => {
      val start = l.getRange.getStart
      val end = l.getRange.getEnd
      var startOffset: Int = -1
      var endOffset: Int = -1
      var sample: String = ""

      /**
        * Opens the editor to retrieve the offsets, line, etc if needed
        */
      def manageUnopenedEditor(): Unit = {
        val file = LocalFileSystem.getInstance().findFileByIoFile(new File(new URI(FileUtils.sanitizeURI(l.getUri))))
        val fileEditorManager = FileEditorManager.getInstance(project)
        if (fileEditorManager.isFileOpen(file)) {
          val editors = fileEditorManager.getAllEditors(file).collect { case t: TextEditor => t.getEditor }
          if (editors.isEmpty) {
            val (s, e, n, sa) = openEditorAndGetOffsetsAndName(file, fileEditorManager, start, end)
            startOffset = s
            endOffset = e
            name = n
            sample = sa
          } else {
            startOffset = DocumentUtils.LSPPosToOffset(editors.head, start)
            endOffset = DocumentUtils.LSPPosToOffset(editors.head, end)
          }
        } else {
          val (s, e, n, sa) = openEditorAndGetOffsetsAndName(file, fileEditorManager, start, end)
          startOffset = s
          endOffset = e
          name = n
          sample = sa
        }
      }

      EditorEventManager.forUri(l.getUri) match {
        case Some(m) =>
          try {
            startOffset = DocumentUtils.LSPPosToOffset(m.editor, start)
            endOffset = DocumentUtils.LSPPosToOffset(m.editor, end)
            name = m.editor.getDocument.getText(new TextRange(startOffset, endOffset))
            sample = DocumentUtils.getLineText(m.editor, startOffset, endOffset)
          } catch {
            case e: RuntimeException =>
              LOG.warn(e)
              manageUnopenedEditor()
          }
        case None =>
          manageUnopenedEditor()
      }

      (l.getUri, startOffset, endOffset, sample)
    }).toArray

    val caretPoint = editor.logicalPositionToXY(editor.getCaretModel.getCurrentCaret.getLogicalPosition)
    showReferencesWindow(locations, name, caretPoint)
  }

  /**
    * Creates and shows the references window given the locations
    *
    * @param locations The locations : The file URI, the start offset, end offset, and the sample (line) containing the offsets
    * @param name      The name of the symbol
    * @param point     The point at which to show the window
    */
  private def showReferencesWindow(locations: Array[(String, Int, Int, String)], name: String, point: Point): Unit = {
    if (locations.isEmpty) {
      invokeLater(() => if (!editor.isDisposed) currentHint = createAndShowEditorHint(editor, "No usages found", point))
    } else {
      val frame = new JFrame()
      frame.setTitle("Usages of " + name + " (" + locations.length + (if (locations.length > 1) " usages found)" else " usage found"))
      val panel = new JPanel()
      var row = 0
      panel.setLayout(new GridLayoutManager(locations.length, 4, new Insets(10, 10, 10, 10), -1, -1))
      locations.foreach(l => {
        val listener = new MouseAdapter() {
          override def mouseClicked(e: MouseEvent): Unit = {
            val file = LocalFileSystem.getInstance().findFileByIoFile(new File(new URI(FileUtils.sanitizeURI(l._1))))
            val descriptor = new OpenFileDescriptor(project, file, l._2)
            writeAction(() => {
              val newEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
              if (l._2 != -1 && l._3 != -1) newEditor.getSelectionModel.setSelection(l._2, l._3)
            })
            frame.setVisible(false)
            frame.dispose()
          }
        }
        val fileLabel = new JLabel(new File(new URI(FileUtils.sanitizeURI(l._1))).getName)
        val spacer = new Spacer()
        val offsetLabel = new JLabel(l._2.toString)
        val sampleLabel = new JLabel("<html>" + l._4 + "</html>")
        panel.add(fileLabel, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        panel.add(spacer, new GridConstraints(row, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false))
        panel.add(offsetLabel, new GridConstraints(row, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        panel.add(sampleLabel, new GridConstraints(row, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
        row += 1
        //TODO refine
        fileLabel.addMouseListener(listener)
        spacer.addMouseListener(listener)
        offsetLabel.addMouseListener(listener)
        sampleLabel.addMouseListener(listener)
      })
      panel.setVisible(true)
      frame.setContentPane(panel)

      frame.setLocationRelativeTo(editor.getContentComponent)
      frame.setLocation(point)
      frame.pack()
      frame.setAutoRequestFocus(true)
      frame.setAlwaysOnTop(true)
      frame.setVisible(true)
    }
  }

  /**
    * Will show documentation if the mouse doesn't move for a given time (Hover)
    *
    * @param e the event
    */
  def mouseMoved(e: EditorMouseEvent): Unit = {
    if (e.getEditor == editor) {
      val language = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument).getLanguage
      if ((EditorSettingsExternalizable.getInstance().isShowQuickDocOnMouseOverElement && //Fixes double doc if documentation provider is present
        (LanguageDocumentation.INSTANCE.allForLanguage(language).isEmpty || language.equals(PlainTextLanguage.INSTANCE))) || isCtrlDown) {
        val curTime = System.nanoTime()
        if (predTime == (-1L) || ctrlTime == (-1L)) {
          predTime = curTime
          ctrlTime = curTime
        } else {
          val lPos = getPos(e)
          if (lPos != null) {
            if (!isKeyPressed || isCtrlDown) {
              val offset = editor.logicalPositionToOffset(lPos)
              if (isCtrlDown && curTime - ctrlTime > CTRL_THRES) {
                if (ctrlRange == null || !ctrlRange.highlightContainsOffset(offset)) {
                  if (currentHint != null) currentHint.hide()
                  currentHint = null
                  if (ctrlRange != null) ctrlRange.dispose()
                  ctrlRange = null
                  pool(() => requestAndShowDoc(curTime, lPos, e.getMouseEvent.getPoint))
                } else if (ctrlRange.definitionContainsOffset(offset)) {
                  createAndShowEditorHint(editor, "Click to show usages", editor.offsetToXY(offset))
                } else {
                  editor.getContentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                }
                ctrlTime = curTime
              } else {
                scheduleDocumentation(curTime, lPos, e.getMouseEvent.getPoint)
              }

            }
          }
          predTime = curTime
        }
      }
    } else {
      LOG.error("Wrong editor for EditorEventManager")
    }
  }

  /**
    * Immediately requests the server for documentation at the current editor position
    *
    * @param editor The editor
    */
  def quickDoc(editor: Editor): Unit = {
    if (editor == this.editor) {
      val caretPos = editor.getCaretModel.getLogicalPosition
      val pointPos = editor.logicalPositionToXY(caretPos)
      val currentTime = System.nanoTime()
      pool(() => requestAndShowDoc(currentTime, caretPos, pointPos))
      predTime = currentTime
    } else {
      LOG.warn("Not same editor!")
    }
  }

  /**
    * Gets the hover request and shows it
    *
    * @param curTime   The current time
    * @param editorPos The editor position
    * @param point     The point at which to show the hint
    */
  private def requestAndShowDoc(curTime: Long, editorPos: LogicalPosition, point: Point): Unit = {
    val serverPos = computableReadAction[Position](() => DocumentUtils.logicalToLSPPos(editorPos, editor))
    val request = requestManager.hover(new TextDocumentPositionParams(identifier, serverPos))
    if (request != null) {
      try {
        val hover = request.get(HOVER_TIMEOUT, TimeUnit.MILLISECONDS)
        wrapper.notifySuccess(Timeouts.HOVER)
        if (hover != null) {
          val string = HoverHandler.getHoverString(hover)
          if (string != null && string != "") {
            if (isCtrlDown) {
              invokeLater(() => if (!editor.isDisposed) currentHint = createAndShowEditorHint(editor, string, point, flags = HintManager.HIDE_BY_OTHER_HINT))
              createCtrlRange(serverPos, hover.getRange)
            } else {
              invokeLater(() => if (!editor.isDisposed) currentHint = createAndShowEditorHint(editor, string, point))
            }
          } else {
            LOG.warn("Hover string returned is null for file " + identifier.getUri + " and pos (" + serverPos.getLine + ";" + serverPos.getCharacter + ")")
          }
        } else {
          LOG.warn("Hover is null for file " + identifier.getUri + " and pos (" + serverPos.getLine + ";" + serverPos.getCharacter + ")")
        }
      } catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.HOVER)
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
      }
    }


  }

  private def createCtrlRange(serverPos: Position, range: Range): Unit = {
    val loc = requestDefinition(serverPos)
    if (loc != null) {
      invokeLater(() => {
        if (!editor.isDisposed) {
          val corRange = if (range == null) {
            val params = new TextDocumentPositionParams(identifier, serverPos)
            val future = requestManager.documentHighlight(params)
            if (future != null) {
              try {
                val highlights = future.get(DOC_HIGHLIGHT_TIMEOUT, TimeUnit.MILLISECONDS)
                if (highlights != null) {
                  wrapper.notifySuccess(Timeouts.DOC_HIGHLIGHT)
                  val offset = DocumentUtils.LSPPosToOffset(editor, serverPos)
                  highlights.asScala.find(dh => DocumentUtils.LSPPosToOffset(editor, dh.getRange.getStart) <= offset
                    && offset <= DocumentUtils.LSPPosToOffset(editor, dh.getRange.getEnd)).fold(new Range(serverPos, serverPos))(dh => dh.getRange)
                } else new Range(serverPos, serverPos)
              } catch {
                case e: TimeoutException =>
                  LOG.warn(e)
                  wrapper.notifyFailure(Timeouts.DOC_HIGHLIGHT)
                  new Range(serverPos, serverPos)
                case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
                  LOG.warn(e)
                  wrapper.crashed(e.asInstanceOf[Exception])
                  new Range(serverPos, serverPos)
              }
            } else new Range(serverPos, serverPos)

          } else range
          val startOffset = DocumentUtils.LSPPosToOffset(editor, corRange.getStart)
          val endOffset = DocumentUtils.LSPPosToOffset(editor, corRange.getEnd)
          val isDefinition = DocumentUtils.LSPPosToOffset(editor, loc.getRange.getStart) == startOffset
          if (ctrlRange != null) ctrlRange.dispose()
          ctrlRange = CtrlRangeMarker(loc, editor,
            if (!isDefinition) editor.getMarkupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.HYPERLINK, editor.getColorsScheme.getAttributes(EditorColors.REFERENCE_HYPERLINK_COLOR), HighlighterTargetArea.EXACT_RANGE)
            else null)
        }
      })
    }
  }

  /**
    * Returns the position of the definition given a position in the editor
    *
    * @param position The position
    * @return The location of the definition
    */
  private def requestDefinition(position: Position): Location = {
    val params = new TextDocumentPositionParams(identifier, position)
    val request = requestManager.definition(params)
    if (request != null) {
      try {
        val definition = request.get(DEFINITION_TIMEOUT, TimeUnit.MILLISECONDS)
        wrapper.notifySuccess(Timeouts.DEFINITION)
        if (definition != null && !definition.isEmpty) {
          definition.get(0)
        } else {
          null
        }
      } catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.DEFINITION)
          null
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
          null
      }
    } else {
      null
    }
  }

  /**
    * Returns the references given the position of the word to search for
    * Must be called from main thread
    *
    * @param offset The offset in the editor
    * @return An array of PsiElement
    */
  def references(offset: Int, getOriginalElement: Boolean = false, close: Boolean = false): (Iterable[PsiElement], Iterable[VirtualFile]) = {
    val lspPos = DocumentUtils.offsetToLSPPos(editor, offset)
    val params = new ReferenceParams(new ReferenceContext(getOriginalElement))
    params.setPosition(lspPos)
    params.setTextDocument(identifier)
    val request = requestManager.references(params)
    if (request != null) {
      try {
        val res = request.get(REFERENCES_TIMEOUT, TimeUnit.MILLISECONDS)
        wrapper.notifySuccess(Timeouts.REFERENCES)
        if (res != null) {
          val openedEditors = mutable.ListBuffer[VirtualFile]()
          val elements = res.asScala.map(l => {
            val start = l.getRange.getStart
            val end = l.getRange.getEnd
            val uri = FileUtils.sanitizeURI(l.getUri)
            val file = FileUtils.virtualFileFromURI(uri)
            var curEditor = FileUtils.editorFromUri(uri, project)
            if (curEditor == null) {
              val descriptor = new OpenFileDescriptor(project, file)
              curEditor = computableWriteAction(() => FileEditorManager.getInstance(project).openTextEditor(descriptor, false))
              openedEditors += file
            }
            val logicalStart = DocumentUtils.LSPPosToOffset(curEditor, start)
            val logicalEnd = DocumentUtils.LSPPosToOffset(curEditor, end)
            val name = curEditor.getDocument.getText(new TextRange(logicalStart, logicalEnd))
            LSPPsiElement(name, project, logicalStart, logicalEnd, PsiDocumentManager.getInstance(project).getPsiFile(curEditor.getDocument))
              .asInstanceOf[PsiElement]
          })
          if (close) {
            writeAction(() => openedEditors.foreach(f => FileEditorManager.getInstance(project).closeFile(f)))
            openedEditors.clear()
          }
          (elements, openedEditors.clone())
        } else {
          (Seq.empty, Seq.empty)
        }
      } catch {
        case e: TimeoutException =>
          LOG.warn(e)
          wrapper.notifyFailure(Timeouts.REFERENCES)
          (Seq.empty, Seq.empty)
        case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
          LOG.warn(e)
          wrapper.crashed(e.asInstanceOf[Exception])
          (Seq.empty, Seq.empty)
      }
    } else (Seq.empty, Seq.empty)
  }

  /**
    * Reformat the whole document
    */
  def reformat(closeAfter: Boolean = false): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        val params = new DocumentFormattingParams()
        params.setTextDocument(identifier)
        val options = new FormattingOptions()
        params.setOptions(options)
        val request = requestManager.formatting(params)
        if (request != null) request.thenAccept(formatting => if (formatting != null) invokeLater(() =>
          applyEdit(edits = formatting.asScala, name = "Reformat document", closeAfter = closeAfter)))
      }
    })
  }

  /**
    * Reformat the text currently selected in the editor
    */
  def reformatSelection(): Unit = {
    pool(() => {
      if (!editor.isDisposed) {
        val params = new DocumentRangeFormattingParams()
        params.setTextDocument(identifier)
        val selectionModel = editor.getSelectionModel
        val start = computableReadAction(() => selectionModel.getSelectionStart)
        val end = computableReadAction(() => selectionModel.getSelectionEnd)
        val startingPos = DocumentUtils.offsetToLSPPos(editor, start)
        val endPos = DocumentUtils.offsetToLSPPos(editor, end)
        params.setRange(new Range(startingPos, endPos))
        val options = new FormattingOptions() //TODO
        params.setOptions(options)
        val request = requestManager.rangeFormatting(params)
        if (request != null) request.thenAccept(formatting => if (formatting != null) invokeLater(() => applyEdit(edits = formatting.asScala, name = "Reformat selection")))
      }
    })
  }

  /**
    * Adds all the listeners
    */
  def registerListeners(): Unit = {
    editor.addEditorMouseListener(mouseListener)
    editor.addEditorMouseMotionListener(mouseMotionListener)
    editor.getDocument.addDocumentListener(documentListener)
    editor.getSelectionModel.addSelectionListener(selectionListener)
  }

  /**
    * Removes all the listeners
    */
  def removeListeners(): Unit = {
    editor.removeEditorMouseMotionListener(mouseMotionListener)
    editor.getDocument.removeDocumentListener(documentListener)
    editor.removeEditorMouseListener(mouseListener)
    editor.getSelectionModel.removeSelectionListener(selectionListener)
  }

  /**
    * Rename a symbol in the document
    *
    * @param renameTo The new name
    */
  def rename(renameTo: String, offset: Int = editor.getCaretModel.getCurrentCaret.getOffset): Unit = {
    pool(() => {
      val servPos = DocumentUtils.offsetToLSPPos(editor, offset)
      if (!editor.isDisposed) {
        val params = new RenameParams(identifier, servPos, renameTo)
        val request = requestManager.rename(params)
        if (request != null) request.thenAccept(res => {
          WorkspaceEditHandler.applyEdit(res, "Rename to " + renameTo, LSPRenameProcessor.getEditors.toList)
          LSPRenameProcessor.clearEditors()
        })
      }
    })
  }

  /**
    * Requests the Hover information
    *
    * @param editor The editor
    * @param offset The offset in the editor
    * @return The information
    */
  def requestDoc(editor: Editor, offset: Int): String = {
    if (editor == this.editor) {
      if (offset != -1) {
        val serverPos = DocumentUtils.offsetToLSPPos(editor, offset)
        val request = requestManager.hover(new TextDocumentPositionParams(identifier, serverPos))
        if (request != null) {
          try {
            val response = request.get(HOVER_TIMEOUT, TimeUnit.MILLISECONDS)
            wrapper.notifySuccess(Timeouts.HOVER)
            HoverHandler.getHoverString(response)
          } catch {
            case e: TimeoutException =>
              LOG.warn(e)
              wrapper.notifyFailure(Timeouts.HOVER)
              ""
            case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
              LOG.warn(e)
              wrapper.crashed(e.asInstanceOf[Exception])
              ""
          }
        } else {
          ""
        }
      } else {
        LOG.warn("Offset at -1")
        ""
      }
    } else {
      LOG.warn("Not same editor")
      ""
    }
  }

  /**
    * Manages the change of selected text in the editor
    *
    * @param e The selection event
    */
  def selectionChanged(e: SelectionEvent): Unit = {
    if (CodeInsightSettings.getInstance().HIGHLIGHT_IDENTIFIER_UNDER_CARET) {
      if (e.getEditor == editor) {
        selectedSymbHighlights.foreach(h => editor.getMarkupModel.removeHighlighter(h))
        selectedSymbHighlights.clear()
        if (editor.getSelectionModel.hasSelection) {
          val ideRange = e.getNewRange
          val LSPPos = DocumentUtils.offsetToLSPPos(editor, ideRange.getStartOffset)
          val request = requestManager.documentHighlight(new TextDocumentPositionParams(identifier, LSPPos))
          if (request != null) {
            pool(() => {
              if (!editor.isDisposed) {
                try {
                  val resp = request.get(DOC_HIGHLIGHT_TIMEOUT, TimeUnit.MILLISECONDS)
                  wrapper.notifySuccess(Timeouts.DOC_HIGHLIGHT)
                  if (resp != null) {
                    invokeLater(() => resp.asScala.foreach(dh => {
                      if (!editor.isDisposed) {
                        val range = dh.getRange
                        val kind = dh.getKind
                        val startOffset = DocumentUtils.LSPPosToOffset(editor, range.getStart)
                        val endOffset = DocumentUtils.LSPPosToOffset(editor, range.getEnd)
                        val colorScheme = editor.getColorsScheme
                        val highlight = editor.getMarkupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.SELECTION - 1, colorScheme.getAttributes(EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES), HighlighterTargetArea.EXACT_RANGE)
                        selectedSymbHighlights.add(highlight)
                      }
                    }))
                  }
                } catch {
                  case e: TimeoutException =>
                    LOG.warn(e)
                    wrapper.notifyFailure(Timeouts.DOC_HIGHLIGHT)
                  case e@(_: java.io.IOException | _: JsonRpcException | _: ExecutionException) =>
                    LOG.warn(e)
                    wrapper.crashed(e.asInstanceOf[Exception])
                }
              }
            })
          }
        }
      }
    }
  }

  /**
    * Tells the manager that the mouse is in the editor
    */
  def mouseEntered(): Unit = {
    mouseInEditor = true

  }

  /**
    * Tells the manager that the mouse is not in the editor
    */
  def mouseExited(): Unit = {
    mouseInEditor = false
    isCtrlDown = false
  }

  /**
    * Returns the logical position given a mouse event
    *
    * @param e The event
    * @return The position (or null if out of bounds)
    */
  private def getPos(e: EditorMouseEvent): LogicalPosition = {
    val mousePos = e.getMouseEvent.getPoint
    val editorPos = editor.xyToLogicalPosition(mousePos)
    val doc = e.getEditor.getDocument
    val maxLines = doc.getLineCount
    if (editorPos.line >= maxLines) {
      null
    } else {
      val minY = doc.getLineStartOffset(editorPos.line) - (if (editorPos.line > 0) doc.getLineEndOffset(editorPos.line - 1) else 0)
      val maxY = doc.getLineEndOffset(editorPos.line) - (if (editorPos.line > 0) doc.getLineEndOffset(editorPos.line - 1) else 0)
      if (editorPos.column < minY || editorPos.column > maxY) {
        null
      } else {
        editorPos
      }
    }
  }

  /**
    * Schedule the documentation using the Timer
    *
    * @param time      The current time
    * @param editorPos The position in the editor
    * @param point     The point where to show the doc
    */
  private def scheduleDocumentation(time: Long, editorPos: LogicalPosition, point: Point): Unit = {
    if (editorPos != null) {
      if (time - predTime > SCHEDULE_THRES) {
        try {
          hoverThread.schedule(new TimerTask {
            override def run(): Unit = {
              if (!editor.isDisposed) {
                val curTime = System.nanoTime()
                if (curTime - predTime > HOVER_TIME_THRES && mouseInEditor && editor.getContentComponent.hasFocus && (!isKeyPressed || isCtrlDown)) {
                  val editorOffset = computableReadAction[Int](() => editor.logicalPositionToOffset(editorPos))
                  val inHighlights = diagnosticsHighlights.filter(diag =>
                    diag.rangeHighlighter.getStartOffset <= editorOffset &&
                      editorOffset <= diag.rangeHighlighter.getEndOffset)
                  if (inHighlights.isEmpty || isCtrlDown) {
                    requestAndShowDoc(curTime, editorPos, point)
                  }
                }
              }
            }
          }, POPUP_THRES)
        } catch {
          case e: Exception =>
            hoverThread = new Timer("Hover", true) //Restart Timer if it crashes
            LOG.warn(e)
            LOG.warn("Hover timer reset")
        }
      }
    }
  }
}
