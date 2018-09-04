// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Publishes data to the stream.
#
# + data - Data to be published to the stream
public extern function stream::publish(any data);

# Subscribes to data from the stream.
#
# + func - The function pointer for the subscription, which will be invoked with data published to the stream
public extern function stream::subscribe(function (any) func);

# Creates the forever runtime.
#
# + streamQuery - The siddhi query by which the siddhi app runtime is created
# + inStreamRefs - References of the input streams in the forever
# + inTableRefs - References of the input tables in the forever
# + outStreamRefs - References of the output streams in the forever
# + outTableRefs - References of the output tables in the forever
# + funcPointers - References of the functions to invoke as the streaming action
extern function startForever(string streamQuery, any inStreamRefs, any inTableRefs, any outStreamRefs,
                             any outTableRefs, any funcPointers);
