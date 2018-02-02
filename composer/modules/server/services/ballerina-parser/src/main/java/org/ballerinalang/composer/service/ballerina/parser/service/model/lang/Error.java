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
 * Error.
 */
public class Error   {
  private Long code = null;

  private String message = null;

  private String description = null;

  private String moreInfo = null;

  private List<ErrorListItem> error = new ArrayList<ErrorListItem>();

  public Error code(Long code) {
    this.code = code;
    return this;
  }

   /**
   * Get code.
   * @return code
  **/
  public Long getCode() {
    return code;
  }

  public void setCode(Long code) {
    this.code = code;
  }

  public Error message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Error message.
   * @return message
  **/
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Error description(String description) {
    this.description = description;
    return this;
  }

   /**
   * A detail description about the error message. 
   * @return description
  **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Error moreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
    return this;
  }

   /**
   * Preferably an url with more details about the error. 
   * @return moreInfo
  **/
  public String getMoreInfo() {
    return moreInfo;
  }

  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  public Error error(List<ErrorListItem> error) {
    this.error = error;
    return this;
  }

  public Error addErrorItem(ErrorListItem errorItem) {
    this.error.add(errorItem);
    return this;
  }

   /**
   * If there are more than one error list them out. For example, list out validation errors by each field. 
   * @return error
  **/
  public List<ErrorListItem> getError() {
    return error;
  }

  public void setError(List<ErrorListItem> error) {
    this.error = error;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.code, error.code) &&
        Objects.equals(this.message, error.message) &&
        Objects.equals(this.description, error.description) &&
        Objects.equals(this.moreInfo, error.moreInfo) &&
        Objects.equals(this.error, error.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, description, moreInfo, error);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    moreInfo: ").append(toIndentedString(moreInfo)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
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

