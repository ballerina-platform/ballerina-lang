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

package org.wso2.ballerina.tooling.service.workspace.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;;
import io.swagger.annotations.ApiModelProperty;

/**
 * Auto generated service class.
 * Service
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-01-27T14:09:52.367Z")
public class Service   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("swaggerDefinition")
  private String swaggerDefinition = null;

  @JsonProperty("ballerinaDefinition")
  private String ballerinaDefinition = null;

  public Service name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorService", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Service description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(example = "A calculator service that supports basic operations", value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Service swaggerDefinition(String swaggerDefinition) {
    this.swaggerDefinition = swaggerDefinition;
    return this;
  }

   /**
   * Swagger definition of the API which contains details about URI templates and scopes 
   * @return swaggerDefinition
  **/
  @ApiModelProperty(example = "{&quot;paths&quot;:{&quot;/substract&quot;" +
          ":{&quot;get&quot;:{&quot;x-auth-type&quot;:&quot;Application &amp; " +
          "Application User&quot;,&quot;x-throttling-tier&quot;:&quot;Unlimited&quot;" +
          ",&quot;parameters&quot;:[{&quot;name&quot;:&quot;x&quot;,&quot;required&quot;:" +
          "true,&quot;type&quot;:&quot;string&quot;,&quot;in&quot;:&quot;query&quot;},{&quot;" +
          "name&quot;:&quot;y&quot;,&quot;required&quot;:true,&quot;type&quot;:&quot;string&quot;" +
          ",&quot;in&quot;:&quot;query&quot;}],&quot;responses&quot;:{&quot;200&quot;:{}}}},&quot;" +
          "/add&quot;:{&quot;get&quot;:{&quot;x-auth-type&quot;:&quot;Application &amp; " +
          "Application User&quot;,&quot;x-throttling-tier&quot;:&quot;Unlimited&quot;" +
          ",&quot;parameters&quot;:[{&quot;name&quot;:&quot;x&quot;,&quot;required&quot;" +
          ":true,&quot;type&quot;:&quot;string&quot;,&quot;in&quot;:&quot;query&quot;}" +
          ",{&quot;name&quot;:&quot;y&quot;,&quot;required&quot;:true,&quot;type&quot;" +
          ":&quot;string&quot;,&quot;in&quot;:&quot;query&quot;}],&quot;responses&quot;" +
          ":{&quot;200&quot;:{}}}}},&quot;swagger&quot;:&quot;2.0&quot;,&quot;info&quot;" +
          ":{&quot;title&quot;:&quot;CalculatorAPI&quot;,&quot;version&quot;:&quot;1.0.0&quot;}}",
          value = "Swagger definition of the API which contains details about URI templates and scopes ")
  public String getSwaggerDefinition() {
    return swaggerDefinition;
  }

  public void setSwaggerDefinition(String swaggerDefinition) {
    this.swaggerDefinition = swaggerDefinition;
  }

  public Service ballerinaDefinition(String ballerinaDefinition) {
    this.ballerinaDefinition = ballerinaDefinition;
    return this;
  }

   /**
   * Ballerina definition of the API which contains details about URI templates and scopes 
   * @return ballerinaDefinition
  **/
  @ApiModelProperty(example = "import ballerina.net.http;@BasePath(&quot;/echo&quot;) service echo " +
          "{@POST resource echo(message m) {http:convertToResponse(m );reply m;}}",
          value = "Ballerina definition of the API which contains details about URI templates and scopes ")
  public String getBallerinaDefinition() {
    return ballerinaDefinition;
  }

  public void setBallerinaDefinition(String ballerinaDefinition) {
    this.ballerinaDefinition = ballerinaDefinition;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Service service = (Service) o;
    return Objects.equals(this.name, service.name) &&
        Objects.equals(this.description, service.description) &&
        Objects.equals(this.swaggerDefinition, service.swaggerDefinition) &&
        Objects.equals(this.ballerinaDefinition, service.ballerinaDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, swaggerDefinition, ballerinaDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Service {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    swaggerDefinition: ").append(toIndentedString(swaggerDefinition)).append("\n");
    sb.append("    ballerinaDefinition: ").append(toIndentedString(ballerinaDefinition)).append("\n");
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
}

