/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.ballerina.parser.service.model.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ModelPackage.
 */
public class ModelPackage   {
  private String name = null;
  private String description = null;
  private String version = null;
  private List<Connector> connectors = new ArrayList<Connector>();
  private List<Function> functions = new ArrayList<Function>();
  private List<Struct> structs = new ArrayList<Struct>();
  private List<AnnotationDef> annotations = new ArrayList<AnnotationDef>();
  private List<Enum> enums = new ArrayList<Enum>();
  private List<ObjectModel> objects = new ArrayList<>();
  private List<Endpoint> endpoints = new ArrayList<>();

  public ModelPackage name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name.
   * @return name
  **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ModelPackage description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description.
   * @return description
  **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ModelPackage version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Get version.
   * @return version
  **/
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ModelPackage connectors(List<Connector> connectors) {
    this.connectors = connectors;
    return this;
  }

  public ModelPackage addConnectorsItem(Connector connectorsItem) {
    this.connectors.add(connectorsItem);
    return this;
  }

   /**
   * List of connectors available in the package.
   * @return connectors
  **/
  public List<Connector> getConnectors() {
    return connectors;
  }

  public void setConnectors(List<Connector> connectors) {
    this.connectors = connectors;
  }

  public ModelPackage functions(List<Function> functions) {
    this.functions = functions;
    return this;
  }

  public ModelPackage addFunctionsItem(Function functionsItem) {
    this.functions.add(functionsItem);
    return this;
  }

  public ModelPackage structs(List<Struct> structs) {
    this.structs = structs;
    return this;
  }

  public ModelPackage addStructsItem(Struct structItem) {
    this.structs.add(structItem);
    return this;
  }

  public ModelPackage enums(List<Enum> enums) {
    this.enums = enums;
    return this;
  }

  public ModelPackage addEnumItem(Enum enumItem) {
    this.enums.add(enumItem);
    return this;
  }

  public ModelPackage annotations(List<AnnotationDef> annotations) {
    this.annotations = annotations;
    return this;
  }

  public ModelPackage addAnnotationsItem(AnnotationDef annotationItem) {
    this.annotations.add(annotationItem);
    return this;
  }

   /**
   * List of **public** functions available in the package.
   * @return functions
  **/
  public List<Function> getFunctions() {
    return functions;
  }

  public void setFunctions(List<Function> functions) {
    this.functions = functions;
  }

  public List<Struct> getStructs() {
    return structs;
  }

  public void setStructs(List<Struct> structs) {
    this.structs = structs;
  }

  public List<Enum> getEnums() {
    return enums;
  }

  public void setEnums(List<Enum> enums) {
    this.enums = enums;
  }

  public List<AnnotationDef> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(List<AnnotationDef> annotations) {
    this.annotations = annotations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelPackage modelPackage = (ModelPackage) o;
    return Objects.equals(this.name, modelPackage.name) &&
        Objects.equals(this.description, modelPackage.description) &&
        Objects.equals(this.version, modelPackage.version) &&
        Objects.equals(this.connectors, modelPackage.connectors) &&
        Objects.equals(this.functions, modelPackage.functions) &&
            Objects.equals(this.structs, modelPackage.structs) &&
            Objects.equals(this.annotations, modelPackage.annotations) &&
            Objects.equals(this.enums, modelPackage.enums);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, version, connectors, functions, structs, annotations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelPackage {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    connectors: ").append(toIndentedString(connectors)).append("\n");
    sb.append("    functions: ").append(toIndentedString(functions)).append("\n");
    sb.append("    structs: ").append(toIndentedString(structs)).append("\n");
    sb.append("    annotations: ").append(toIndentedString(annotations)).append("\n");
    sb.append("    enums: ").append(toIndentedString(enums)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public List<ObjectModel> getObjects() {
    return objects;
  }

  public void setObjects(List<ObjectModel> objects) {
    this.objects = objects;
  }

  public void addObjectItem(ObjectModel object) {
    this.objects.add(object);
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }

  public void addEndpointItem(Endpoint endpoint) {
    this.endpoints.add(endpoint);
  }
}

