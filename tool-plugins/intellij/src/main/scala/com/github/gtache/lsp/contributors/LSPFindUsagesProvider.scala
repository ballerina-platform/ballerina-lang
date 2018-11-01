package com.github.gtache.lsp.contributors

import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.{PsiElement, PsiFile, PsiNamedElement}

/**
  * A findUsagesProvider for LSP (ALT+F7)
  */
class LSPFindUsagesProvider extends FindUsagesProvider {

  private val LOG: Logger = Logger.getInstance(classOf[LSPFindUsagesProvider])

  override def getHelpId(psiElement: PsiElement): String = null

  override def canFindUsagesFor(psiElement: PsiElement): Boolean = {
    psiElement match {
      case p: PsiFile => true
      case l: LSPPsiElement => true
      case _ => false
    }
  }

  override def getWordsScanner: WordsScanner = null

  override def getNodeText(element: PsiElement, useFullName: Boolean): String = element.getText

  override def getDescriptiveName(element: PsiElement): String = element match {
    case e: PsiNamedElement => e.getName
    case _ => ""
  }

  override def getType(element: PsiElement): String = element match {
    case _ => ""
  }
}
