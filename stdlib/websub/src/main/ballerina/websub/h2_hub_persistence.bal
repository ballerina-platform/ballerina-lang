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

import ballerina/h2;
import ballerina/log;

const string CREATE_TOPICS_TABLE = "CREATE TABLE IF NOT EXISTS topics (topic VARCHAR(255), PRIMARY KEY (topic))";
const string INSERT_INTO_TOPICS = "INSERT INTO topics (topic) VALUES (?)";
const string DELETE_FROM_TOPICS = "DELETE FROM topics WHERE topic=?";
const string SELECT_ALL_FROM_TOPICS = "SELECT * FROM topics";

const string CREATE_SUBSCRIPTIONS_TABLE = "CREATE TABLE IF NOT EXISTS subscriptions
                                            (topic VARCHAR(255), callback VARCHAR(255), secret VARCHAR(255),
                                            lease_seconds BIGINT, created_at BIGINT, PRIMARY KEY (topic, callback))";
const string INSERT_INTO_SUBSCRIPTIONS_TABLE = "INSERT INTO subscriptions
                                                (topic,callback,secret,lease_seconds,created_at) VALUES (?,?,?,?,?)";
const string DELETE_FROM_SUBSCRIPTIONS = "DELETE FROM subscriptions WHERE topic=? AND callback=?";
const string SELECT_FROM_SUBSCRIPTIONS = "SELECT topic, callback, secret, lease_seconds, created_at FROM subscriptions";

# Represents H2 based hub persistence configuration and functions.
public type H2HubPersistenceStore object {

    // TODO: make private
    public h2:Client subscriptionDbClient;

    public function __init(h2:Client subscriptionDbClient) {
        self.subscriptionDbClient = subscriptionDbClient;
        var updateResult = self.subscriptionDbClient->update(CREATE_TOPICS_TABLE);
        if (updateResult is error) {
            panic updateResult;
        }

        updateResult = self.subscriptionDbClient->update(CREATE_SUBSCRIPTIONS_TABLE);
        if (updateResult is error) {
            panic updateResult;
        }
    }

    # Function to add or update subscription details.
    #
    # + subscriptionDetails - The details of the subscription to add or update
    public function addSubscription(SubscriptionDetails subscriptionDetails) {
        sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: subscriptionDetails.topic };
        sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: subscriptionDetails.callback };
        sql:Parameter para3 = { sqlType: sql:TYPE_VARCHAR, value: subscriptionDetails.secret };
        sql:Parameter para4 = { sqlType: sql:TYPE_BIGINT, value: subscriptionDetails.leaseSeconds };
        sql:Parameter para5 = { sqlType: sql:TYPE_BIGINT, value: subscriptionDetails.createdAt };

        var rowCount = self.subscriptionDbClient->update(DELETE_FROM_SUBSCRIPTIONS, untaint para1, untaint para2);
        if (rowCount is int) {
            log:printDebug("Successfully removed " + rowCount + " entries for existing subscription");
        } else {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred deleting subscription data: " + errCause);
        }

        rowCount = self.subscriptionDbClient->update(INSERT_INTO_SUBSCRIPTIONS_TABLE, untaint para1, untaint para2,
                                                    untaint para3, untaint para4, untaint para5);
        if (rowCount is int) {
            log:printDebug("Successfully updated " + rowCount + " entries for subscription");
        } else {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating subscription data: " + errCause);
        }
    }

    # Function to remove subscription details.
    #
    # + subscriptionDetails - The details of the subscription to remove
    public function removeSubscription(SubscriptionDetails subscriptionDetails) {
        sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: subscriptionDetails.topic };
        sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: subscriptionDetails.callback };
        var rowCount = self.subscriptionDbClient->update(DELETE_FROM_SUBSCRIPTIONS, untaint para1, untaint para2);

        if (rowCount is int) {
            log:printDebug("Successfully updated " + rowCount + " entries for unsubscription");
        } else {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating unsubscription data: " + errCause);
        }
    }

    # Function to add a topic.
    #
    # + topic - The topic to add
    public function addTopic(string topic) {
        sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: topic };
        var rowCount = self.subscriptionDbClient->update(INSERT_INTO_TOPICS, para1);
        if (rowCount is int) {
            log:printDebug("Successfully updated " + rowCount + " entries for topic registration");
        } else {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating topic registration data: " + errCause);
        }
    }

    # Function to remove a topic.
    #
    # + topic - The topic to remove
    public function removeTopic(string topic) {
        sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: topic };
        var rowCount = self.subscriptionDbClient->update(DELETE_FROM_TOPICS, para1);
        if (rowCount is int) {
            log:printDebug("Successfully updated " + rowCount + " entries for topic unregistration");
        } else {
            string errCause = <string> rowCount.detail().message;
            log:printError("Error occurred updating topic unregistration data: " + errCause);
        }
    }

    # Function to retrieve all registered topics.
    #
    # + return - An array of topics
    public function retrieveTopics() returns string[] {
        string[] topics = [];
        int topicIndex = 0;
        var dbResult = self.subscriptionDbClient->select(SELECT_ALL_FROM_TOPICS, TopicRegistration);
        if (dbResult is table<record {}>) {
            while (dbResult.hasNext()) {
                var registrationDetails = trap <TopicRegistration>dbResult.getNext();
                if (registrationDetails is TopicRegistration) {
                    topics[topicIndex] = registrationDetails.topic;
                    topicIndex += 1;
                } else {
                    string errCause = <string> registrationDetails.detail().message;
                    log:printError("Error retreiving topic registration details from the database: " + errCause);
                }
            }
        } else {
            string errCause = <string> dbResult.detail().message;
            log:printError("Error retreiving data from the database: " + errCause);
        }
        return topics;
    }

    # Function to retrieve subscription details of all subscribers.
    #
    # + return - An array of subscriber details
    public function retrieveAllSubscribers() returns SubscriptionDetails[] {
        SubscriptionDetails[] subscriptions = [];
        int subscriptionIndex = 0;
        var dbResult = self.subscriptionDbClient->select(SELECT_FROM_SUBSCRIPTIONS, SubscriptionDetails);
        if (dbResult is table<record {}>) {
            while (dbResult.hasNext()) {
                var subscriptionDetails = trap <SubscriptionDetails>dbResult.getNext();
                if (subscriptionDetails is SubscriptionDetails) {
                    subscriptions[subscriptionIndex] = subscriptionDetails;
                    subscriptionIndex += 1;
                } else {
                    string errCause = <string> subscriptionDetails.detail().message;
                    log:printError("Error retreiving subscription details from the database: " + errCause);
                }
            }
        } else {
            string errCause = <string> dbResult.detail().message;
            log:printError("Error retreiving data from the database: " + errCause);
        }
        return subscriptions;
    }
};
