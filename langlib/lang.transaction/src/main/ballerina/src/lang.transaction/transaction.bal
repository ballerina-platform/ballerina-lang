//// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
////
//// WSO2 Inc. licenses this file to you under the Apache License,
//// Version 2.0 (the "License"); you may not use this file except
//// in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing,
//// software distributed under the License is distributed on an
//// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//// KIND, either express or implied.  See the License for the
//// specific language governing permissions and limitations
//// under the License.
//
//public type Info record {|
//   // unique identifier
//   byte[] xid;
//   // non-zero means this transaction was a retry of
//   // a previous one
//   int retryNumber;
//   // probably useful for timeouts and logs
//   int startTime;
//   // maybe useful
//   Info? prevAttempt;
//|};
//
//public type CommitHandler function(Info info);
//public type RollbackHandler function(Info info, error? cause, boolean willRetry);
//
