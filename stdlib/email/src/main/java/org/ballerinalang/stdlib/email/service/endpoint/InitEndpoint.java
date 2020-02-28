// /*
//  * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//  *
//  * WSO2 Inc. licenses this file to you under the Apache License,
//  * Version 2.0 (the "License"); you may not use this file except
//  * in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing,
//  * software distributed under the License is distributed on an
//  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  * KIND, either express or implied.  See the License for the
//  * specific language governing permissions and limitations
//  * under the License.
//  */

// package org.ballerinalang.stdlib.email.service.endpoint;

// import org.ballerinalang.jvm.values.ObjectValue;
// import org.ballerinalang.stdlib.email.service.DirectoryListenerConstants;
// import org.ballerinalang.stdlib.email.util.EmailConstants;
// import org.ballerinalang.stdlib.email.util.EmailUtils;

// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;

// /**
//  * Initialize endpoints.
//  */

// public class InitEndpoint {

//     public static Object initEndpoint(ObjectValue listener) {
//         final String path = listener.getMapValue(DirectoryListenerConstants.SERVICE_ENDPOINT_CONFIG).
//                 getStringValue(DirectoryListenerConstants.ANNOTATION_PATH);
//         if (path == null || path.isEmpty()) {
//             return EmailUtils.getBallerinaError(EmailConstants.EMAIL_SYSTEM_ERROR, "'path' field is empty");
//         }
//         final Path dirPath = Paths.get(path);
//         if (Emails.notExists(dirPath)) {
//             return EmailUtils.getBallerinaError(EmailConstants.EMAIL_SYSTEM_ERROR, "Folder does not exist: " + path);
//         }
//         if (!Emails.isDirectory(dirPath)) {
//             return EmailUtils.getBallerinaError(EmailConstants.EMAIL_SYSTEM_ERROR, "Unable to find a directory: "
//                     + path);
//         }
//         return null;
//     }
// }
