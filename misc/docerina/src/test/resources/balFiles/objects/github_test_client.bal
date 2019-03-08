// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type GitHubClientConfig record {
    string repositoryOwner = "";
    string repositoryName = "";
    string issueTitle = "";
    string issueContent = "";
    string[] labelList = [];
    string[] assigneeList = [];
};

# GitHub client
# + githubClientConfiguration - GitHub client configurations (Access token, Client endpoint configurations)
public type GithubClient client object {
    public GitHubClientConfig githubClientConfiguration = {};

    # GitHub client endpoint initialization function
    # + githubClientConfig - GitHub client configuration
    public function __init(GitHubClientConfig config) {
        self.githubClientConfiguration = config;
    }

    # Test Connector action testAction
    # + return - whether successful or not
    public remote function testAction() returns boolean {
        return true;
    }

    # Test Connector 2 action testSend
    # + ep - endpoint url
    # + return - whether successful or not
    public remote function testSend(string ep) returns boolean {
        return true;
    }
};

