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

# Singleton Instance of SubscriptionManager.
StreamManager streamManager = new;

# Represent the stream subscription manager for different streams.
type StreamManager object {

    # Add a new subscription function to the given stream.
    # + strm - The stream to which the subscription is added
    # + func - The subscription function to be added
    public function addSubscriptionFunc(stream<PureType> strm, function (PureType) func) = external;

    # Return subscription functions as an array when the stream is given.
    # + strm - input stream
    # + return - subcription functions and number of subscription functions
    public function getSubscriptionFuncs(stream<PureType> strm) returns function (PureType)[] = external;
};
