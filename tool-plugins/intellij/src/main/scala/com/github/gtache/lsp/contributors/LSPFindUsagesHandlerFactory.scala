package com.github.gtache.lsp.contributors

import java.util

import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.{ApplicationUtils, FileUtils}
import com.intellij.find.findUsages.{FindUsagesHandler, FindUsagesHandlerFactory}
import com.intellij.psi.search.SearchScope
import com.intellij.psi.{PsiElement, PsiFile, PsiReference}

class LSPFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

  import scala.collection.JavaConverters._

  override def createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler =
    new FindUsagesHandler(element) {
      @volatile private var elements: Iterable[PsiElement] = Seq.empty
      element match {
        case p: PsiFile =>
          val editor = FileUtils.editorFromPsiFile(p)
          EditorEventManager.forEditor(editor) match {
            case Some(m) => ApplicationUtils.invokeLater(() => elements = m.references(editor.getCaretModel.getCurrentCaret.getOffset, getOriginalElement = true)._1)
            case None =>
          }
        case l: LSPPsiElement =>
          val editor = FileUtils.editorFromPsiFile(l.getContainingFile)
          EditorEventManager.forEditor(editor) match {
            case Some(m) => ApplicationUtils.invokeLater(() => elements = m.references(editor.getCaretModel.getCurrentCaret.getOffset, getOriginalElement = true)._1)
            case None =>
          }
        case _ =>
      }

      override def findReferencesToHighlight(target: PsiElement, searchScope: SearchScope): util.Collection[PsiReference] = {
        target match {
          case p: PsiFile if p == element =>
            elements.map(p => p.getReference).toList.asJava
          case l: LSPPsiElement if elements.exists(p => p == l) =>
            elements.map(p => p.getReference).toList.asJava
          case _ => Seq().asJava
        }
      }

      override def getPrimaryElements: Array[PsiElement] = {
        elements.toArray
      }

    }

  override def canFindUsages(element: PsiElement): Boolean = {
    element match {
      case p: PsiFile => true
      case l: LSPPsiElement => true
      case _ => false
    }
  }
}
