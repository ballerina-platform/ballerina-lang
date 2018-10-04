/* Adapted from lsp4e*/
package com.github.gtache.lsp.client.connection

import java.io.{File, IOException, InputStream, OutputStream}
import java.util.Objects

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.annotations.Nullable

/**
  * A class symbolizing a stream to a process
  *
  * @param commands   The commands to start the process
  * @param workingDir The working directory of the process
  */
class ProcessStreamConnectionProvider(private var commands: Seq[String], private var workingDir: String) extends StreamConnectionProvider {
  private val LOG: Logger = Logger.getInstance(classOf[ProcessStreamConnectionProvider])
  @Nullable private var process: Process = _

  @throws[IOException]
  override def start(): Unit = {
    if (this.workingDir == null || this.commands == null || this.commands.isEmpty || this.commands.contains(null)) throw new IOException("Unable to start language server: " + this.toString) //$NON-NLS-1$
    val builder = createProcessBuilder
    LOG.info("Starting server process with commands " + commands + " and workingDir " + workingDir)
    this.process = builder.start

    if (!process.isAlive) throw new IOException("Unable to start language server: " + this.toString) else LOG.info("Server process started " + process)
  }

  protected def createProcessBuilder: ProcessBuilder = {
    import scala.collection.JavaConverters._
    val builder = new ProcessBuilder(getCommands.asJava)
    builder.directory(new File(getWorkingDirectory))
    builder.redirectError(ProcessBuilder.Redirect.INHERIT)
    builder
  }

  @Nullable override def getInputStream: InputStream = {
    if (process == null) null
    else process.getInputStream
  }

  @Nullable override def getOutputStream: OutputStream = {
    if (process == null) null
    else process.getOutputStream
  }

  override def stop(): Unit = {
    if (process != null) process.destroy()
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: ProcessStreamConnectionProvider =>
        getCommands.size == other.getCommands.size && this.getCommands.toSet == other.getCommands.toSet && this.getWorkingDirectory == other.getWorkingDirectory
      case _ => false
    }

  }

  protected def getCommands: Seq[String] = commands

  def setCommands(commands: Seq[String]): Unit = {
    this.commands = commands
  }

  protected def getWorkingDirectory: String = workingDir

  def setWorkingDirectory(workingDir: String): Unit = {
    this.workingDir = workingDir
  }

  override def hashCode: Int = {
    Objects.hashCode(this.getCommands) ^ Objects.hashCode(this.getWorkingDirectory)
  }
}
