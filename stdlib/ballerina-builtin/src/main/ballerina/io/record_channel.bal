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

documentation{
    Represents a channel which will allow to read/write records through a given CharacterChannel
}
public type DelimitedTextRecordChannel object {
    private {
        CharacterChannel channel;
        string rs;
        string fs;
    }

    documentation{
        Constructs a DelimitedTextRecordChannel from a given CharacterChannel

        P{{channel}} - CharacterChannel which will point to the input/output resource
        P{{rs}} - Record separator (this could be a regex)
        P{{fs}} - Field separator (this could be a regex)
    }
    public new(channel, fs = "", rs = "", string fmt = "default") {
        init(channel, fs, rs, fmt);
    }

    documentation{
        Initializes delimited record channel

        P{{channel}} - Character channel which will be used for reading/writing records
        P{{fs}} - Field separator which will separate between fields
        P{{rs}} - Record separator which will separate between records
        P{{fmt}} - Format which will be used to represent the type of record i.e csv
    }
    native function init(CharacterChannel channel, string fs, string rs, string fmt);

    documentation{
        Checks whether there's a record left to be read

        R{{}} - True if there's a record left to be read
    }
    public native function hasNext() returns boolean;

    documentation{
        Get next record from the input/output resource

        R{{}} - Set of fields included in the record or an error
    }
    public native function getNext() returns @tainted string[]|error;

    documentation{
        Writes records to a given input/output resource

        P{{record}} - list of fields which should be written
        R{{}} - An error if the records could not be written properly
    }
    public native function write(string[] record) returns error?;

    documentation{
        Closes a given record channel

        R{{}} - An error if the record channel could not be closed properly
    }
    public native function close() returns error?;
};
