package com.github.gtache.lsp.actions

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.find.FindBundle
import com.intellij.find.findUsages.{FindUsagesOptions, PsiElement2UsageTargetAdapter}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.{UsageInfo, UsageViewUtil}
import com.intellij.usages.impl.UsageViewManagerImpl
import com.intellij.usages.{UsageInfo2UsageAdapter, UsageViewPresentation}

/**
  * Action for references / see usages (SHIFT+ALT+F7)
  */
class LSPReferencesAction extends DumbAwareAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      //EditorEventManager.forEditor(editor).foreach(manager => manager.showReferences())
      val targets = EditorEventManager.forEditor(editor)
        .map(m => m.references(editor.getCaretModel.getCurrentCaret.getOffset, getOriginalElement = true, close = true)._1)
        .getOrElse(Seq())
        .map(r => new PsiElement2UsageTargetAdapter(r))

      val usageInfo = targets.map(ut => {
        val elem = ut.getElement
        new UsageInfo(elem, -1, -1, false)
      })

      val presentation = createPresentation(targets.head.getElement, new FindUsagesOptions(editor.getProject), toOpenInNewTab = false)
      /*val factory = new Factory[UsageSearcher] {
        override def create(): UsageSearcher = {
          new UsageSearcher {
            override def generate(processor: Processor[Usage]): Unit = {
              processor.process()
            }
          }
        }
      }
      new UsageViewImpl(editor.getProject,
        createPresentation(targets.head.getElement, new FindUsagesOptions(editor.getProject),toOpenInNewTab = false),
        targets.toArray,
        null
      )*/

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
