package com.github.gtache.lsp.settings.gui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.{JComboBox, JComponent, JPanel, JTextField}

import scala.collection.mutable

/**
  * Class representing a row in the server settings window
  *
  * @param panel  The row as a JPanel
  * @param typ    The typ of the row
  * @param fields The fields of the row
  */
case class ServersGUIRow(panel: JPanel, typ: String, fields: mutable.LinkedHashMap[String, JComponent]) {
  private val LOG: Logger = Logger.getInstance(classOf[ServersGUIRow])

  /**
    * @return the type of the row (Artifact, exe or command)
    */
  def getTyp: String = typ

  /**
    * @param label The label corresponding to the text field
    * @return The content of the text field
    */
  def getText(label: String): String = {
    fields.get(label).fold("") {
      case t: JTextField => t.getText().trim
      case tb: TextFieldWithBrowseButton => tb.getText.trim
      case b: JComboBox[String@unchecked] => b.getSelectedItem.asInstanceOf[String]
      case u: JComponent => LOG.error("Unknown JComponent : " + u)
        ""
    }
  }

  /**
    * @return A string array representing this row
    */
  def toStringArray: Array[String] = {
    Array(typ) ++ fields.values.map {
      case t: JTextField => t.getText().trim
      case tb: TextFieldWithBrowseButton => tb.getText.trim
      case b: JComboBox[String@unchecked] => b.getSelectedItem.asInstanceOf[String]
      case u: JComponent => LOG.error("Unknown JComponent : " + u)
        ""
    }
  }
}
