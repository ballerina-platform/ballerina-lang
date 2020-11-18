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

#
# LoadBalancerRule provides a required interfaces to implement different algorithms.
#
public type LoadBalancerRule object {

    # Provides an HTTP client which is chosen according to the algorithm.
    #
    # + loadBalanceCallerActionsArray - Array of HTTP clients which needs to be load balanced
    # + return - Chosen `Client` from the algorithm or an `http:ClientError`
    #            for the failure in the algorithm implementation
    public function getNextClient(Client?[] loadBalanceCallerActionsArray) returns Client|ClientError;
};
