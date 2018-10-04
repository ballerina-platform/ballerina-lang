package com.github.gtache.lsp.utils.coursier

import com.github.gtache.lsp.utils.LSPException

case class CoursierException(msg: String) extends LSPException(msg) {

}
