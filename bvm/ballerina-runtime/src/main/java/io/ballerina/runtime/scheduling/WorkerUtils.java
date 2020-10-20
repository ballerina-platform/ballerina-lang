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
 package io.ballerina.runtime.scheduling;

 import io.ballerina.runtime.TypeChecker;
 import io.ballerina.runtime.api.PredefinedTypes;
 import io.ballerina.runtime.values.ChannelDetails;
 import io.ballerina.runtime.values.ErrorValue;
 import io.ballerina.runtime.values.RefValue;

 /**
  * Worker related utility methods for jBallerina runtime.
  *
  * @since 0.995.0
  */
 public class WorkerUtils {

     /**
      * Notify worker data channels if this is a error return in a union type.
      * @param value return value
      * @param strand current strand
      * @param channels worker date channels that current worker interacts
      */
     public static void handleWorkerError(RefValue value, Strand strand, ChannelDetails[] channels) {
         if (TypeChecker.checkIsType(value, PredefinedTypes.TYPE_ERROR)) {
             strand.handleChannelError(channels, (ErrorValue) value);
         }
     }

 }
