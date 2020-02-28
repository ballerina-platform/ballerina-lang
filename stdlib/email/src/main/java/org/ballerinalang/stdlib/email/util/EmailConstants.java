 /*
  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  * WSO2 Inc. licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */

 package org.ballerinalang.stdlib.email.util;

 import org.ballerinalang.jvm.types.BPackage;

 import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

 /**
  * Constants for email package functions.
  *
  * @since 1.1.5
  */
 public class EmailConstants {

     /**
      * Package path.
      */
     public static final BPackage EMAIL_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "email");

     public static final String MESSAGE_TO = "to";
     public static final String MESSAGE_CC = "cc";
     public static final String MESSAGE_BCC = "bcc";
     public static final String MESSAGE_SUBJECT = "subject";
     public static final String MESSAGE_MESSAGE_BODY = "body";
     public static final String MESSAGE_FROM = "from";
     public static final String MESSAGE_SENDER = "sender";
     public static final String MESSAGE_REPLY_TO = "replyTo";

     public static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";

 }
