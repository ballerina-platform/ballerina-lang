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

package org.ballerinalang.composer.service.workspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Annotation.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-01-27T07:45:46.625Z")
public class Annotation   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("value")
  private String value = null;

  @JsonProperty("attachmentPoints")
  private List<String> attachmentPoints = new ArrayList<String>();

  public Annotation name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name.
   * @return name
  **/
  @ApiModelProperty(required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Annotation value(String value) {
    this.value = value;
    return this;
  }

  public Annotation attachmentPoints(List<String> attachmentPoints) {
    this.attachmentPoints = attachmentPoints;
    return this;
  }

  public Annotation addAttachmentPointsItem(String attachmentPoint) {
    this.attachmentPoints.add(attachmentPoint);
    return this;
  }

  public void setAttachmentPoints(List<String> attachmentPoints){
    this.attachmentPoints = attachmentPoints;
  }

  public List<String> getAttachmentPoints() {
    return this.attachmentPoints;
  }

   /**
   * Get value.
   * @return value
  **/
  @ApiModelProperty(required = true, value = "")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Annotation annotation = (Annotation) o;
    return Objects.equals(this.name, annotation.name) &&
        Objects.equals(this.value, annotation.value) &&
            Objects.equals(this.attachmentPoints, annotation.attachmentPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, attachmentPoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Annotation {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

