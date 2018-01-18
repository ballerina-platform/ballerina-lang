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
 * PackageList.
 */
public class PackageList   {
  private Integer count = null;
  private String next = null;
  private String previous = null;

  private List<ModelPackage> list = new ArrayList<ModelPackage>();

  public PackageList count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of APIs returned.
   * @return count
  **/
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public PackageList next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned.
   * @return next
  **/
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public PackageList previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned.
   * @return previous
  **/
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public PackageList list(List<ModelPackage> list) {
    this.list = list;
    return this;
  }

  public PackageList addListItem(ModelPackage listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list.
   * @return list
  **/
  public List<ModelPackage> getList() {
    return list;
  }

  public void setList(List<ModelPackage> list) {
    this.list = list;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PackageList packageList = (PackageList) o;
    return Objects.equals(this.count, packageList.count) &&
        Objects.equals(this.next, packageList.next) &&
        Objects.equals(this.previous, packageList.previous) &&
        Objects.equals(this.list, packageList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PackageList {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

