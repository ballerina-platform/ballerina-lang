package com.github.gtache.lsp.client.languageserver.serverdefinition

import com.github.gtache.lsp.utils.LSPException

case class BadServerDefinitionException(m: String) extends LSPException(m) {

}
