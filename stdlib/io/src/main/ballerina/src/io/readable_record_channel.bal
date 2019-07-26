// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents a channel which will allow to read
public type ReadableTextRecordChannel object {

    private ReadableCharacterChannel charChannel;
    private string rs;
    private string fs;

    # Constructs a ReadableTextRecordChannel from a given ReadableCharacterChannel.
    #
    # + charChannel - CharacterChannel which will point to the input/output resource
    # + fs - Field separator (this could be a regex)
    # + rs - Record separator (this could be a regex)
    public function __init(ReadableCharacterChannel charChannel, public string fs = "", public string rs = "",
                           public string fmt = "default") {
        self.init(charChannel, fs, rs, fmt);
    }

    # Initializes delimited record channel.
    #
    # + characterChannel - Character channel which will be used for reading/writing records
    # + fieldSeparator - Field separator which will separate between fields
    # + recordSeparator - Record separator which will separate between records
    # + fmt - Format which will be used to represent the type of record i.e csv
    function init(ReadableCharacterChannel characterChannel, public string fieldSeparator,
                public string recordSeparator, public string fmt) = external;

    # Checks whether there's a record left to be read.
    #
    # + return - True if there's a record left to be read
    public function hasNext() returns boolean = external;

    # Get next record from the input/output resource.
    #
    # + return - Set of fields included in the record or `Error` if any error occurred
    public function getNext() returns @tainted string[]|Error = external;

    # Closes a given record channel.
    #
    # + return - An `Error` if the record channel could not be closed properly
    public function close() returns Error? = external;
};
