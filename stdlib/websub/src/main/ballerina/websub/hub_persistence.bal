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

    # Function to add or update subscription details.
    #
    # + subscriptionDetails - The details of the subscription to add or update
    public function addSubscription(SubscriptionDetails subscriptionDetails);

    # Function to remove subscription details.
    #
    # + subscriptionDetails - The details of the subscription to remove
    public function removeSubscription(SubscriptionDetails subscriptionDetails);

    # Function to add a topic.
    #
    # + topic - The topic to add
    public function addTopic(string topic);

    # Function to remove a topic.
    #
    # + topic - The topic to remove
    public function removeTopic(string topic);

    # Function to retrieve all registered topics.
    #
    # + return - An array of topics
    public function retrieveTopics() returns string[];

    # Function to retrieve subscription details of all subscribers.
    #
    # + return - An array of subscriber details
    public function retrieveAllSubscribers() returns SubscriptionDetails[];
};
