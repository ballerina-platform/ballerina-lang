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


@Description {value:"Ballerina DelimitedRecordChannel represents a channel which will allow to read/write text records"}
public type DelimitedTextRecordChannel object {
    private {
        CharacterChannel channel;
        string rs;
        string fs;
    }

    new(channel, fs = "", rs = "", string fmt = "default") {
        init(channel, fs, rs, fmt);
    }

    @Description {value : "Initialize delimited record channel"}
    @Param {value : "channel : character channel which will be used for reading/writing records"}
    @Param {value : "fs : Feild separator which will separate between fields"}
    @Param {value : "rs : Record separator which will separate between records"}
    @Param {value : "fmt : Format which will be used to represent the type of record i.e csv"}
    native function init(CharacterChannel channel, string fs, string rs, string fmt);

    @Description {value:"Function to check whether next record is available or not"}
    @Return {value:"True if the channel has more records; false otherwise"}
    public native function hasNext() returns boolean;

    @Description {value:"Function to read text records"}
    @Return {value:"Fields listed in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function getNext() returns @tainted string[]|error;

    @Description {value:"Function to write text records"}
    @Param {value:"records: Fields which are included in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function write(string[] record) returns error?;

    @Description {value:"Function to close the text record channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function close() returns error?;
};
