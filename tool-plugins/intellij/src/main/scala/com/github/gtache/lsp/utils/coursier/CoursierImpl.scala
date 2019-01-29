package com.github.gtache.lsp.utils.coursier

import java.io.File

import com.github.gtache.lsp.settings.BallerinaLSPState
import com.github.gtache.lsp.utils.{ApplicationUtils, Utils}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import coursier.ivy.IvyRepository
import coursier.maven.MavenRepository
import coursier.{Cache, Dependency, Fetch, Module, Repository, Resolution}
import scalaz.concurrent.Task

/**
  * Coursier is used to fetch the dependencies for a given server package (example : ch.epfl.lamp:dotty-language-server_0.3:0.3.0-RC2) and returning the classpath to run it using java
  */
object CoursierImpl {

  val separator = "::"

  private val LOG: Logger = Logger.getInstance(CoursierImpl.getClass)

  private val baseRepositories = Seq(Cache.ivy2Local, MavenRepository("https://repo1.maven.org/maven2"))
  private val repositories = baseRepositories ++ getAdditionalRepositories

  /**
    * Downloads the dependencies and returns the classpath for a given package
    *
    * @param toResolve The string
    * @return The full classpath string
    */
  def resolveClasspath(toResolve: String): String = {
    val parsed = parseDepString(toResolve)
    val start = Resolution(Set({
      Dependency(Module(parsed._1, parsed._2), parsed._3)
    }))
    val fetch = Fetch.from(repositories, Cache.fetch())
    var resolution: Resolution = null
    try {
      resolution = start.process.run(fetch).unsafePerformSync
    } catch {
      case e: Exception if e.getMessage.contains("No protocol found") =>
        ApplicationUtils.invokeLater(() => Messages.showWarningDialog(
          "Coursier repositories error, please check LSP settings\n" + e.getMessage, "LSP Coursier error"))
        LOG.warn(e)
        resolution = start.process.run(Fetch.from(baseRepositories, Cache.fetch())).unsafePerformSync
    }
    val localArtifacts = Task.gatherUnordered(resolution.artifacts.map(Cache.file(_).run)).unsafePerformSync
    if (!localArtifacts.forall(_.isRight)) {
      throw CoursierException("Couldn't fetch all dependencies for " + toResolve + "\nMissing " + localArtifacts.filter(_.isLeft).mkString(";"))
    } else if (localArtifacts.nonEmpty) {
      val cp = localArtifacts.map(f => f.getOrElse(new File(""))).aggregate("")((s, f) => s + File.pathSeparator + f.getAbsolutePath, (s1, s2) => s1 + File.pathSeparator + s2).tail
      LOG.info("Fetched dependencies for " + toResolve)
      cp
    } else {
      throw CoursierException("Empty classpath for " + toResolve + "\nDo you need additional Coursier repositories?")
    }
  }

  private def parseDepString(str: String): (String, String, String) = {
    val res = str.split(":")
    if (res.length != 3) {
      throw CoursierException("Unknown dependency format : " + str)
    } else {
      (res(0), res(1), res(2))
    }
  }

  def checkRepositories(s: String, showErrorMessage: Boolean = false): Boolean = {
    if (!s.isEmpty) {
      val repos = s.split("\n")
      checkRepositories(repos, showErrorMessage)
    } else true
  }

  private def getAdditionalRepositories: Iterable[Repository] = {
    import scala.collection.JavaConverters._
    val repos = BallerinaLSPState.getInstance().coursierResolvers.asScala
    if (!checkRepositories(repos, showErrorMessage = false)) {
      ApplicationUtils.invokeLater(() => Messages.showErrorDialog("Malformed Coursier repositories, please check LSP settings", "Coursier error"))
      Seq()
    } else {
      val additionalRepos = repos.map(r => r.split(separator)).partition(arr => arr(0).equalsIgnoreCase(Repositories.IVY.name()))
      additionalRepos._1.map(arr => IvyRepository.parse(arr(1)).toOption).collect { case Some(repo) => repo } ++ additionalRepos._2.map(arr => MavenRepository(arr(1)))
    }
  }

  def checkRepositories(repos: Iterable[String], showErrorMessage: Boolean): Boolean = {
    val errMsg: StringBuilder = new StringBuilder(0)
    if (showErrorMessage) {
      repos.foreach(s => {
        val arr = s.split(separator)
        if (arr.isEmpty || arr.length != 2 || !Repositories.values.map(v => v.name.toLowerCase).contains(arr(0).toLowerCase))
          errMsg.append(arr.mkString("-")).append(Utils.lineSeparator)
      })
      if (errMsg.nonEmpty) {
        ApplicationUtils.invokeLater(() => Messages.showErrorDialog(errMsg.insert(0, "The repositories syntax is incorrect : they should look like ivy::http://... or maven::http://... separated by a new line. Errors : \n").toString, "Repositories error"))
        false
      } else true
    } else {
      val res = repos.forall(s => {
        val arr = s.split(separator)
        arr.length == 2 && Repositories.values.map(v => v.name.toLowerCase).contains(arr(0).toLowerCase)
      })
      res
    }
  }
}
