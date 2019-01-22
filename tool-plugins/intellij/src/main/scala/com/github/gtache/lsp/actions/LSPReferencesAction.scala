package com.github.gtache.lsp.actions

import java.awt.Color

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.codeInsight.hint.{HintManager, HintManagerImpl}
import com.intellij.find.FindBundle
import com.intellij.find.findUsages.{FindUsagesOptions, PsiElement2UsageTargetAdapter}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.{Editor, LogicalPosition}
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.ui.LightweightHint
import com.intellij.usageView.{UsageInfo, UsageViewUtil}
import com.intellij.usages.impl.UsageViewManagerImpl
import com.intellij.usages.{UsageInfo2UsageAdapter, UsageViewPresentation}
import javax.swing.JLabel

/**
  * Action for references / see usages (SHIFT+ALT+F7)
  */
class LSPReferencesAction extends DumbAwareAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      val targets = EditorEventManager.forEditor(editor)
        .map(m => m.references(editor.getCaretModel.getCurrentCaret.getOffset, getOriginalElement = true, close = true)._1)
        .getOrElse(Seq())
        .map(r => new PsiElement2UsageTargetAdapter(r))

      showReferences(editor, targets, editor.getCaretModel.getCurrentCaret.getLogicalPosition)
    }
  }

  def forManagerAndOffset(manager: EditorEventManager, offset: Int): Unit = {
    val targets = manager.references(offset, getOriginalElement = true, close = true)._1.map(r => new PsiElement2UsageTargetAdapter(r))
    val editor = manager.editor
    showReferences(editor, targets, editor.offsetToLogicalPosition(offset))
  }

  def showReferences(editor: Editor, targets: Iterable[PsiElement2UsageTargetAdapter], logicalPosition: LogicalPosition): Unit = {
    if (targets.isEmpty) {
      val constraint: Short = HintManager.ABOVE
      val flags: Int = HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING
      val label = new JLabel("No references found")
      label.setBackground(new Color(150, 0, 0))
      val hint = new LightweightHint(label)
      val p = HintManagerImpl.getHintPosition(hint, editor, logicalPosition, constraint)
      HintManagerImpl.getInstanceImpl.showEditorHint(hint, editor, p, flags, 0, false, HintManagerImpl.createHintHint(editor, p, hint, constraint).setContentActive(false))
    } else {
      val usageInfo = targets.map(ut => {
        val elem = ut.getElement
        new UsageInfo(elem, -1, -1, false)
      })

      val presentation = createPresentation(targets.head.getElement, new FindUsagesOptions(editor.getProject), toOpenInNewTab = false)

      new UsageViewManagerImpl(editor.getProject).showUsages(Array(targets.head), usageInfo.map(ui => new UsageInfo2UsageAdapter(ui)).toArray, presentation)
    }
  }


  private def createPresentation(psiElement: PsiElement, options: FindUsagesOptions, toOpenInNewTab: Boolean) = {
    val presentation = new UsageViewPresentation
    val scopeString = options.searchScope.getDisplayName
    presentation.setScopeText(scopeString)
    val usagesString = options.generateUsagesString()
    presentation.setUsagesString(usagesString)
    val title = FindBundle.message("find.usages.of.element.in.scope.panel.title", usagesString, UsageViewUtil.getLongName(psiElement), scopeString)
    presentation.setTabText(title)
    presentation.setTabName(FindBundle.message("find.usages.of.element.tab.name", usagesString, UsageViewUtil.getShortName(psiElement)))
    presentation.setTargetsNodeText(StringUtil.capitalize(UsageViewUtil.getType(psiElement)))
    presentation.setOpenInNewTab(toOpenInNewTab)
    presentation
  }

}
