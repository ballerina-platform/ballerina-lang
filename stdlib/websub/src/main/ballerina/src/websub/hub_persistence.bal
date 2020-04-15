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

# Represents the hub persistence configuration and functions.
public type HubPersistenceStore abstract object {

# Adds or updates subscription details.
# ```ballerina
# error? result = hubPersistenceStore.addSubscription(subscriptionDetails);
# ```
#
# + subscriptionDetails - The details of the subscription to add or update
# + return - An `error` if an error occurred while adding the subscription or else `()` otherwise
    public function addSubscription(SubscriptionDetails subscriptionDetails) returns error?;

# Removes subscription details.
# ```ballerina
# error? result = hubPersistenceStore.removeSubscription(subscriptionDetails);
# ```
#
# + subscriptionDetails - The details of the subscription to remove
# + return - An `error` if an error occurred while removing the subscription or else `()` otherwise
    public function removeSubscription(SubscriptionDetails subscriptionDetails) returns error?;

# Function to add a topic.
# ```ballerina
# error? result = hubPersistenceStore.addTopic("topic");
# ```
#
# + topic - The topic to add
# + return - An `error` if an error occurred while adding the topic or else `()` otherwise
    public function addTopic(string topic) returns error?;

# Function to remove a topic.
# ```ballerina
# error? result = hubPersistenceStore.removeTopic("topic");
# ```
#
# + topic - The topic to remove
# + return - An `error` if an error occurred while removing the topic or else `()` otherwise
    public function removeTopic(string topic) returns error?;

# Function to retrieve subscription details of all subscribers.
# ```ballerina
# SubscriptionDetails[]|error result = hubPersistenceStore.retrieveAllSubscribers();
# ```
#
# + return - An array of subscriber details or else an `error` if an error occurred while retrieving
#            the subscriptions
    public function retrieveAllSubscribers() returns SubscriptionDetails[]|error;

# Function to retrieve all registered topics.
# ```ballerina
# string[]|error result = hubPersistenceStore.retrieveTopics();
# ```
#
# + return - An array of topics or else `error` if an error occurred while retrieving the topics
    public function retrieveTopics() returns string[]|error;
};
