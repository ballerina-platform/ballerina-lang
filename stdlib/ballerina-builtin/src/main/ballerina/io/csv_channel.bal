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


@Description {value : "Character which will be used to separate betweeen the records"}
@final string CSV_RECORD_SEPERATOR = "\n";

@Description {value : "Character which will be used to separate colon delimited records"}
@final string FS_COLON = ":";

@Description {value:"Ballerina DelimitedRecordChannel represents a channel which will allow to read/write text records"}
public type CSVChannel object {
    public {
        DelimitedTextRecordChannel? dc;
    }

    public new(CharacterChannel channel, Seperator fs = ",", boolean hasHeader = true) {
        if (fs == TAB){
            dc = new DelimitedTextRecordChannel(channel, fmt = "TDF");
        } else if (fs == COLON){
            dc = new DelimitedTextRecordChannel(channel, fs = FS_COLON, rs = CSV_RECORD_SEPERATOR);
        } else {
            dc = new DelimitedTextRecordChannel(channel, fmt = "CSV");
        }
    }

    @Description {value:"Function to check whether next record is available or not"}
    @Return {value:"True if the channel has more records; false otherwise"}
    public function hasNext() returns boolean? {
        return dc.hasNext();
    }

    @Description {value:"Function to read text records"}
    @Return {value:"Fields listed in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public function getNext() returns @tainted string[]|error? {
        return dc.getNext();
    }

    @Description {value:"Function to write text records"}
    @Param {value:"records: Fields which are included in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public function write(string[] record) returns error? {
        return dc.write(record);
    }

    @Description {value:"Function to close the text record channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public function close() returns error? {
        return dc.close();
    }

    @Description {value:"Function to load delimited records to in-memory table"}
    @Param {value:"structType: Name of the struct that each record need to populate"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function getTable(typedesc structType) returns @tainted table|error;
};
