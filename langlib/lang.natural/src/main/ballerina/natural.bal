// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/lang.'object;

# Raw template type for prompts.
public type Prompt object {
    *object:RawTemplate;

    # The fixed string parts of the template.
    public string[] & readonly strings;

    # The interpolations in the template.
    public anydata[] insertions;
};

# Type representing implementations that generate values for natural expressions.
public type Generator isolated client object {

    # Generates a value of the specified type based on the given prompt.
    #
    # + prompt - The natural language instructions in the natural expression transformed
    #           to a value of type `Prompt`.
    # + td - The type descriptor for the expected type
    # + return - Generated data of the specified type or an error if generation fails
    isolated remote function generate(
            Prompt prompt, typedesc<anydata> td) returns td|error;
};

# Denotes that the body of a function has to be generated at compile-time.
public const annotation record {|
    # The natural language description of the code to be generated.
    string prompt;
|} code on source external;
