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

documentation {
    Represents a channel which will allow to read/write records through a given CharacterChannel.
}
public type DelimitedTextRecordChannel object {
    private CharacterChannel charChannel;
    private string rs;
    private string fs;

    documentation {
        Constructs a DelimitedTextRecordChannel from a given CharacterChannel.

        P{{characterChannel}} CharacterChannel which will point to the input/output resource
        P{{rs}} Record separator (this could be a regex)
        P{{fs}} Field separator (this could be a regex)
    }
    public new(characterChannel, fs = "", rs = "", string fmt = "default") {
        init(charChannel, fs, rs, fmt);
    }

    documentation {
        Initializes delimited record channel.

        P{{characterChannel}} Character channel which will be used for reading/writing records
        P{{fieldSeparator}} Field separator which will separate between fields
        P{{recordSeparator}} Record separator which will separate between records
        P{{fmt}} Format which will be used to represent the type of record i.e csv
    }
    extern function init(CharacterChannel characterChannel, string fieldSeparator, string recordSeparator, string fmt);

    documentation {
        Checks whether there's a record left to be read.

        R{{}} True if there's a record left to be read
    }
    public extern function hasNext() returns boolean;

    documentation {
        Get next record from the input/output resource.

        R{{}} Set of fields included in the record or an error
    }
    public extern function getNext() returns @tainted string[]|error;

    documentation {
        Writes records to a given input/output resource.

        P{{textRecord}} List of fields to be written
        R{{}} An error if the records could not be written properly
    }
    public extern function write(string[] textRecord) returns error?;

    documentation {
        Closes a given record channel.

        R{{}} An error if the record channel could not be closed properly
    }
    public extern function close() returns error?;
};
