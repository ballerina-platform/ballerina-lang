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

// package org.ballerinalang.stdlib.email.service;

// import org.ballerinalang.jvm.BRuntime;
// import org.ballerinalang.jvm.BallerinaValues;
// import org.ballerinalang.jvm.types.AttachedFunction;
// import org.ballerinalang.jvm.values.MapValue;
// import org.ballerinalang.jvm.values.ObjectValue;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemEvent;
// import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemListener;

// import java.util.Map;

// import static org.ballerinalang.stdlib.email.service.DirectoryListenerConstants.EMAIL_SYSTEM_EVENT;
// import static org.ballerinalang.stdlib.email.util.EmailConstants.EMAIL_EVENT_NAME;
// import static org.ballerinalang.stdlib.email.util.EmailConstants.EMAIL_EVENT_OPERATION;
// import static org.ballerinalang.stdlib.email.util.EmailConstants.EMAIL_PACKAGE_ID;

// /**
//  * File System connector listener for Ballerina.
//  */
// public class FSListener implements LocalFileSystemListener {

//     private static final Logger log = LoggerFactory.getLogger(FSListener.class);
//     private BRuntime runtime;
//     private ObjectValue service;
//     private Map<String, AttachedFunction> attachedFunctionRegistry;

//     public FSListener(BRuntime runtime, ObjectValue service, Map<String, AttachedFunction> resourceRegistry) {
//         this.runtime = runtime;
//         this.service = service;
//         this.attachedFunctionRegistry = resourceRegistry;
//     }

//     @Override
//     public void onMessage(LocalFileSystemEvent emailEvent) {
//         Object[] parameters = getJvmSignatureParameters(emailEvent);
//         AttachedFunction resource = getAttachedFunction(emailEvent.getEvent());
//         if (resource != null) {
//             runtime.invokeMethodAsync(service, resource.getName(), new DirectoryCallback(), parameters);
//         } else {
//             log.warn(String.format("EmailEvent received for unregistered resource: [%s] %s", fileEvent.getEvent(),
//                     emailEvent.getEmailName()));
//         }
//     }

//     private Object[] getJvmSignatureParameters(LocalEmailSystemEvent emailEvent) {
//         MapValue<String, Object> eventStruct = BallerinaValues
//             .createRecordValue(EMAIL_PACKAGE_ID, EMAIL_SYSTEM_EVENT);
//         eventStruct.put(FILE_EVENT_NAME, emailEvent.getFileName());
//         eventStruct.put(FILE_EVENT_OPERATION, emailEvent.getEvent());
//         return new Object[] { eventStruct, true };
//     }

//     private AttachedFunction getAttachedFunction(String event) {
//         return attachedFunctionRegistry.get(event);
//     }
// }
