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

# Represents a channel which will allow to read/write records through a given CharacterChannel.
public type DelimitedTextRecordChannel object {
    private CharacterChannel charChannel;
    private string rs;
    private string fs;

    # Constructs a DelimitedTextRecordChannel from a given CharacterChannel.
    #
    # + charChannel - CharacterChannel which will point to the input/output resource
    # + rs - Record separator (this could be a regex)
    # + fs - Field separator (this could be a regex)
    public new(charChannel, fs = "", rs = "", string fmt = "default") {
        init(charChannel, fs, rs, fmt);
    }

    # Initializes delimited record channel.
    #
    # + characterChannel - Character channel which will be used for reading/writing records
    # + fieldSeparator - Field separator which will separate between fields
    # + recordSeparator - Record separator which will separate between records
    # + fmt - Format which will be used to represent the type of record i.e csv
    extern function init(CharacterChannel characterChannel, string fieldSeparator, string recordSeparator, string fmt);

    # Checks whether there's a record left to be read.
    #
    # + return - True if there's a record left to be read
    public extern function hasNext() returns boolean;

    # Get next record from the input/output resource.
    #
    # + return - Set of fields included in the record or an error
    public extern function getNext() returns @tainted string[]|error;

    # Writes records to a given input/output resource.
    #
    # + textRecord - List of fields to be written
    # + return - An error if the records could not be written properly
    public extern function write(string[] textRecord) returns error?;

    # Closes a given record channel.
    #
    # + return - An error if the record channel could not be closed properly
    public extern function close() returns error?;
};
