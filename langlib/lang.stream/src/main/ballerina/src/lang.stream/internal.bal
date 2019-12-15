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
import ballerina/lang.'array as arrays;
import ballerina/lang.'value as values;

# Singleton Instance of SubscriptionManager.
StreamManager streamManager = new StreamManager();
int lockObj = 0;

# Represent the stream subscription manager for different streams.
type StreamManager object {

    # Add a new subscription function to the given stream.
    # + strm - The stream to which the subscription is added
    # + subscription - The subscription object to be added
    # + func - The subscription function to be added
    function addSubscriptionFunc(stream<PureType> strm, Subscription subscription, any func) = external;

    # Return subscription functions as an array when the stream is given.
    # + strm - input stream
    # + return - subcription functions and number of subscription functions
    function getSubscriptionFuncs(stream<PureType> strm) returns Subscription[] = external;
};

type Subscription object {
    private PureType[] queue = [];
    private boolean status = false;
    public function(any | error) func;

    public function __init() {
        self.func = function (any|error msg) {

        };
    }

    function publishMessage(PureType message) {
        lock {
            lockObj = 0;
            arrays:push(self.queue, message);
            if (self.status == false) {
                self.status = true;
                _ = @concurrent{} start self.startProcessingEvents(arrays:remove(self.queue, 0));
            }
        }
    }

    function startProcessingEvents(PureType message) {
        var func = self.func;
        func(message);
        lock {
            lockObj = 0;
            if (self.status) {
                while (arrays:length(self.queue) != 0) {
                    func(arrays:remove(self.queue, 0));
                }
                self.status = false;
            }
        }
    }
};
