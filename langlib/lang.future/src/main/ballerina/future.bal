// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Requests cancellation of a future.
#
# This sets the cancellation flag in the strand corresponding to `f`.
# Each time that a strand yields, it will check the cancellation flag
# and terminate abnormally if the flag is set.
#
# ```ballerina
# int[] sales = [10, 13, 54, 245, 24, 29, 343, 34, 23, 45, 67, 93, 846];
# future<int?> computationFuture = start computeEntryCountForTarget(sales, 500);
# computationFuture.cancel();
#
# function computeEntryCountForTarget(int[] sales, int target) returns int? {
#     int sum = 0;
#     foreach [int, int] [index, item] in sales.enumerate() {
#         sum += item;
#
#         if sum >= target {
#             return index + 1;
#         }
#     }
#     return ();
# }
# ```
#
# + f - the future to be cancelled
public isolated function cancel(future<any|error> f) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.future.Cancel",
    name: "cancel"
} external;
