 /*
  *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  *  WSO2 Inc. licenses this file to you under the Apache License,
  *  Version 2.0 (the "License"); you may not use this file except
  *  in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License.
  */
 package io.ballerina.runtime.values;

 /**
  * Represents a channel info.
  *
  * @since 0.995.0
  */
 public class ChannelDetails {

     public String name;
     public boolean channelInSameStrand;
     public boolean send;

     public ChannelDetails(String name, boolean channelInSameStrand, boolean send) {
         this.name = name;
         this.channelInSameStrand = channelInSameStrand;
         this.send = send;
     }

     @Override
     public String toString() {
         return name;
     }

     @Override
     public int hashCode() {
         return name.hashCode();
     }

     @Override
     public boolean equals(Object o) {
         return (o instanceof ChannelDetails) && (((ChannelDetails) o).name.equals(this.name));
     }
 }
