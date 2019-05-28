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

# Abstract object, which should be implemented in order to create a new aggregator.
public type Aggregator abstract object {

    # Returns a copy of self, but it does not contain the current state.
    #
    # + return - A `Aggregator` object.
    public function copy() returns Aggregator;

    # Updates the aggregated value and returns the final aggregated value.
    #
    # + value - value being aggregated.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`. Based on the
    #               type of the event `value` will be added to the aggregation or removed from the aggregation.
    #
    # + return - Updated aggregated value after `value` being aggregated.
    public function process(anydata value, EventType eventType) returns anydata;

};
