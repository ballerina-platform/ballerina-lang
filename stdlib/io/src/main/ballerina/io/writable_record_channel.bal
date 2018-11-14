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

# Represents a channel which will allow to write records through a given WritableCharacterChannel.
public type WritableTextRecordChannel object {
    private WritableCharacterChannel characterChannel;
    private string rs;
    private string fs;

    # Constructs a DelimitedTextRecordChannel from a given WritableCharacterChannel.

    # + channel - WritableCharacterChannel which will point to the input/output resource
    # + rs - Record separator (this could be a regex)
    # + fs - Field separator (this could be a regex)
    public new(characterChannel, fs = "", rs = "", string fmt = "default") {
        self.init(characterChannel, fs, rs, fmt);
    }

    # Initializes delimited record channel.

    # + cChannel - Character channel which will be used for reading/writing records
    # + fieldSeparator - Field separator which will separate between fields
    # + recordSeparator - Record separator which will separate between records
    # + fmt - Format which will be used to represent the type of record i.e csv
    extern function init(WritableCharacterChannel cChannel, string fieldSeparator,
                         string recordSeparator, string fmt);

    # Writes records to a given output resource.

    # + textRecord - List of fields to be written
    # + return - An error if the records could not be written properly
    public extern function write(string[] textRecord) returns error?;

    # Closes a given record channel.

    # + return - An error if the record channel could not be closed properly
    public extern function close() returns error?;
};
