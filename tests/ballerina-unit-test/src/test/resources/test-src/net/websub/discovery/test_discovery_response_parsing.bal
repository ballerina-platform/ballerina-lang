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

import ballerina/http;
import ballerina/websub;

@final string HUB_ONE = "https://hub.ballerina.com";
@final string HUB_TWO = "https://two.hub.ballerina.com";
@final string HUB_THREE = "https://three.hub.ballerina.com";

@final string TOPIC_ONE = "https://topic.ballerina.com";

function testTopicAndSingleHubAsSingleLinkHeader() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\", <" + TOPIC_ONE + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testTopicAndMultipleHubsAsSingleLinkHeader() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\", <" + HUB_TWO + ">; rel=\"hub\", <" + HUB_THREE +
            ">; rel=\"hub\", <" + TOPIC_ONE + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testTopicAndSingleHubAsMultipleLinkHeaders() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\"");
    response.addHeader("Link", "<" + TOPIC_ONE + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testTopicAndMultipleHubsAsMultipleLinkHeaders() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\"");
    response.addHeader("Link", "<" + TOPIC_ONE + ">; rel=\"self\"");
    response.addHeader("Link", "<" + HUB_TWO + ">; rel=\"hub\"");
    response.addHeader("Link", "<" + HUB_THREE + ">; rel=\"hub\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMissingTopicWithSingleLinkHeader() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\", <" + TOPIC_ONE + ">; rel=\"not_self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMissingTopicWithMultipleLinkHeaders() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"hub\"");
    response.addHeader("Link", "<" + TOPIC_ONE + ">; rel=\"not_self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMissingHubWithSingleLinkHeader() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"not_hub\", <" + TOPIC_ONE + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMissingHubWithMultipleLinkHeaders() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"not_hub\"");
    response.addHeader("Link", "<" + TOPIC_ONE + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMissingLinkHeader() returns (string, string[])|error {
    http:Response response = new;
    return websub:extractTopicAndHubUrls(response);
}

function testSingleLinkHeaderWithMultipleTopics() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"not_hub\", <" + TOPIC_ONE + ">; rel=\"self\", <" +
            HUB_TWO + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}

function testMultipleLinkHeadersWithMultipleTopics() returns (string, string[])|error {
    http:Response response = new;
    response.addHeader("Link", "<" + HUB_ONE + ">; rel=\"not_hub\"");
    response.addHeader("Link", "<" + TOPIC_ONE + ">; rel=\"self\"");
    response.addHeader("Link", "<" + HUB_TWO + ">; rel=\"self\"");
    return websub:extractTopicAndHubUrls(response);
}
