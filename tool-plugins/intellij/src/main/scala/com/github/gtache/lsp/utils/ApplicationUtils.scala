package com.github.gtache.lsp.utils

import java.util.concurrent.{Callable, Future}

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable

/**
  * Various methods to write thread related instructions more concisely
  */
object ApplicationUtils {

  def invokeLater(runnable: Runnable): Unit = {
    ApplicationManager.getApplication.invokeLater(runnable)
  }

  def pool(runnable: Runnable): Unit = {
    ApplicationManager.getApplication.executeOnPooledThread(runnable)
  }

  def callablePool[T](callable: Callable[T]): Future[T] = {
    ApplicationManager.getApplication.executeOnPooledThread(callable)
  }

  def computableReadAction[T](computable: Computable[T]): T = {
    ApplicationManager.getApplication.runReadAction(computable)
  }

  def writeAction(runnable: Runnable): Unit = {
    ApplicationManager.getApplication.runWriteAction(runnable)
  }

  def computableWriteAction[T](computable: Computable[T]): T = {
    ApplicationManager.getApplication.runWriteAction(computable)
  }
}
