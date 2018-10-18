package com.github.gtache.lsp.client.languageserver

import java.awt.Point
import java.awt.event.MouseEvent

import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.github.gtache.lsp.requests.Timeouts
import com.github.gtache.lsp.utils.{ApplicationUtils, GUIUtils}
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, DefaultActionGroup}
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.StatusBarWidget.IconPresentation
import com.intellij.openapi.wm.{StatusBar, StatusBarWidget, WindowManager}
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Consumer
import javax.swing.Icon

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object LSPServerStatusWidget {

  private val widgetIDs: mutable.Map[Project, ListBuffer[String]] = mutable.Map()

  /**
    * Creates a widget given a LanguageServerWrapper and adds it to the status bar
    *
    * @param wrapper The wrapper
    * @return The widget
    */
  def createWidgetFor(wrapper: LanguageServerWrapper): LSPServerStatusWidget = {
    val widget = new LSPServerStatusWidget(wrapper)
    val project = wrapper.getProject
    val statusBar = WindowManager.getInstance().getStatusBar(project)
    widgetIDs.get(project) match {
      case Some(_) =>
      case None =>
        widgetIDs.put(project, ListBuffer("Position"))
    }
    statusBar.addWidget(widget, "before " + widgetIDs(project).head)
    widgetIDs(project).prepend(widget.ID())
    widget
  }

  def removeWidgetID(widget: LSPServerStatusWidget): Unit = {
    val project = widget.wrapper.getProject
    widgetIDs(project) -= widget.ID()
  }

}

/**
  * A status bar widget for a server status
  *
  * @param wrapper The wrapper corresponding to the server
  */
class LSPServerStatusWidget(val wrapper: LanguageServerWrapper) extends StatusBarWidget {

  private val timeouts: mutable.Map[Timeouts, (Int, Int)] = mutable.HashMap()
  Timeouts.values().foreach(t => timeouts.put(t, (0, 0)))
  private val ext: String = wrapper.getServerDefinition.ext
  private val project: Project = wrapper.getProject
  private val projectName: String = project.getName
  private val icons: Map[ServerStatus, Icon] = GUIUtils.getIconProviderFor(wrapper.getServerDefinition).getStatusIcons
  private var status: ServerStatus = ServerStatus.STOPPED


  def notifyResult(timeout: Timeouts, success: Boolean): Unit = {
    val oldValue = timeouts(timeout)
    timeouts.update(timeout, if (success) (oldValue._1 + 1, oldValue._2) else (oldValue._1, oldValue._2 + 1))
  }

  override def getPresentation(`type`: StatusBarWidget.PlatformType): StatusBarWidget.IconPresentation = new IconPresentation {

    override def getIcon: Icon = {
      icons.get(status).orNull
    }

    override def getClickConsumer: Consumer[MouseEvent] = (t: MouseEvent) => {
      val mnemonics = JBPopupFactory.ActionSelectionAid.MNEMONICS
      val component = t.getComponent
      val actions = wrapper.getStatus match {
        case ServerStatus.STARTED => Seq(ShowConnectedFiles, ShowTimeouts)
        case _ => Seq(ShowTimeouts)
      }
      val title = "Server actions"
      val context = DataManager.getInstance().getDataContext(component)
      val group = new DefaultActionGroup(actions: _*)
      val popup = JBPopupFactory.getInstance().createActionGroupPopup(title, group, context, mnemonics, true)
      val dimension = popup.getContent.getPreferredSize
      val at = new Point(0, -dimension.height)
      popup.show(new RelativePoint(t.getComponent, at))
    }

    private object ShowConnectedFiles extends AnAction("&Show connected files", "Show the files connected to the server", null) with DumbAware {
      override def actionPerformed(e: AnActionEvent): Unit = {
        Messages.showInfoMessage("Connected files :\n" + wrapper.getConnectedFiles.mkString("\n"), "Connected files")
      }
    }

    private object ShowTimeouts extends AnAction("&Show timeouts", "Show the timeouts proportions of the server", null) with DumbAware {
      override def actionPerformed(e: AnActionEvent): Unit = {
        val message: mutable.StringBuilder = StringBuilder.newBuilder
        message.append("<html>")
        message.append("Timeouts (failed requests) :<br>")
        timeouts.foreach(t => {
          val timeouts = t._2._2
          message.append(t._1.name().substring(0, 1)).append(t._1.name().substring(1).toLowerCase).append(" => ")
          val total = t._2._1 + timeouts
          if (total != 0) {
            if (timeouts > 0) message.append("<font color=\"red\">")
            message.append(timeouts).append("/").append(total).append(" (").append(timeouts.asInstanceOf[Double] / total * 100).append("%)<br>")
            if (timeouts > 0) message.append("</font>")
          } else message.append("0/0 (0%)<br>")
        })
        message.append("</html>")
        Messages.showInfoMessage(message.toString(), "Timeouts")
      }
    }

    override def getTooltipText: String = "Language server for extension " + ext + ", project " + projectName
  }

  override def install(statusBar: StatusBar): Unit = {}

  /**
    * Sets the status of the server
    *
    * @param status The new status
    */
  def setStatus(status: ServerStatus): Unit = {
    this.status = status
    updateWidget()
  }

  private def updateWidget(): Unit = {
    val manager = WindowManager.getInstance()
    if (manager != null && project != null && !project.isDisposed) {
      val statusBar = manager.getStatusBar(project)
      if (statusBar != null) {
        statusBar.updateWidget(ID())
      }
    }
  }

  override def ID(): String = projectName + "_" + ext

  override def dispose(): Unit = {
    val manager = WindowManager.getInstance()
    if (manager != null && project != null && !project.isDisposed) {
      val statusBar = manager.getStatusBar(project)
      LSPServerStatusWidget.removeWidgetID(this)
      if (statusBar != null) ApplicationUtils.invokeLater(() => statusBar.removeWidget(ID()))
    }
  }
}


