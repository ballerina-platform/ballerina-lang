package com.github.gtache.lsp.client.languageserver.serverdefinition

import com.intellij.openapi.diagnostic.Logger

object UserConfigurableServerDefinition extends UserConfigurableServerDefinitionObject {

  private val LOG: Logger = Logger.getInstance(this.getClass)

  /**
    * Transforms a (java) Map<String, ServerDefinitionExtensionPointArtifact> to a Map<String, String[]>
    *
    * @param map A java map
    * @return the transformed java map
    */
  def toArrayMap(map: java.util.Map[String, UserConfigurableServerDefinition]): java.util.Map[String, Array[String]] = {
    import scala.collection.JavaConverters._
    map.asScala.map(e => (e._1, e._2.toArray)).asJava
  }

  /**
    * Transforms a (java) Map<String, String[]> to a Map<String, ServerDefinitionExtensionPointArtifact>
    *
    * @param map A java map
    * @return the transformed java map
    */
  def fromArrayMap(map: java.util.Map[String, Array[String]]): java.util.Map[String, UserConfigurableServerDefinition] = {
    import scala.collection.JavaConverters._
    map.asScala.map(e => (e._1, fromArray(e._2))).asJava
  }

  override def fromArray(arr: Array[String]): UserConfigurableServerDefinition = {
    val filteredArr = arr.filter(s => s != null && s.trim() != "")
    val artifact = ArtifactLanguageServerDefinition.fromArray(filteredArr)
    if (artifact == null) {
      CommandServerDefinition.fromArray(filteredArr)
    } else {
      artifact
    }
  }

  override def typ: String = "userConfigurable"

  override def getPresentableTyp: String = "Configurable"
}

/**
  * A UserConfigurableServerDefinition is a server definition which can be manually entered by the user in the IntellliJ settings
  */
trait UserConfigurableServerDefinition extends LanguageServerDefinition {

  private val LOG: Logger = Logger.getInstance(classOf[UserConfigurableServerDefinition])

}
