package com.github.gtache.lsp.requests

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.jsonrpc.validation.NonNull

/**
  * Object used to process Hover responses
  */
object HoverHandler {

  /**
    * Returns the hover string corresponding to an Hover response
    *
    * @param hover The Hover
    * @return The string response
    */
  def getHoverString(@NonNull hover: Hover): String = {
    import scala.collection.JavaConverters._
    if (hover != null && hover.getContents != null) {
      val hoverContents = hover.getContents
      if (hoverContents.isLeft) {
        val contents = hoverContents.getLeft.asScala
        if (contents == null || contents.isEmpty) "" else {
          val stuff = contents.map(c => {
            if (c.isLeft) {
              val options = new MutableDataSet()
              val parser = Parser.builder(options).build()
              val renderer = HtmlRenderer.builder(options).build()
              val string = c.getLeft
              if (!string.isEmpty) "<html>" + renderer.render(parser.parse(string)) + "</html>" else ""
            }
            else if (c.isRight) {
              val options = new MutableDataSet()
              val parser = Parser.builder(options).build()
              val renderer = HtmlRenderer.builder(options).build()
              val markedString = c.getRight
              val string = if (markedString.getLanguage != null && !markedString.getLanguage.isEmpty)
                s"""```${markedString.getLanguage} ${markedString.getValue} ```""" else markedString.getValue
              if (!string.isEmpty) "<html>" + renderer.render(parser.parse(string)) + "</html>" else ""
            } else ""
          }).filter(s => !s.isEmpty)
          if (stuff.isEmpty) {
            ""
          } else {
            stuff.reduce((a, b) => a + "\n\n" + b)
          }
        }
      } else if (hoverContents.isRight) {
        hoverContents.getRight.getValue //TODO
      } else ""
    } else ""
  }
}
