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

documentation {
    Represents record separator of the CSV file.
}
@final string CSV_RECORD_SEPERATOR = "\n";

documentation {
    Represents colon separator which should be used to identify colon separated files.
}
@final string FS_COLON = ":";

documentation {
    Represents minimum number of headers which will be included in CSV.
}
@final int MINIMUM_HEADER_COUNT = 0;

documentation {
    Represents a CSVChannel which could be used to read/write records from CSV file.
}
public type WritableCSVChannel object {
    private WritableTextRecordChannel? dc;

    documentation {
        Constructs a CSV channel from a CharacterChannel to read/write CSV records.

        P{{channel}} ChracterChannel which will represent the content in the CSV
        P{{fs}} Field separator which will separate between the records in the CSV
    }
    public new(WritableCharacterChannel channel, Separator fs = ",") {
        if (fs == TAB){
            dc = new WritableTextRecordChannel(channel, fmt = "TDF");
        } else if (fs == COLON){
            dc = new WritableTextRecordChannel(channel, fs = FS_COLON, rs = CSV_RECORD_SEPERATOR);
        } else {
            dc = new WritableTextRecordChannel(channel, fmt = "CSV");
        }
    }

    documentation {
        Writes record to a given CSV file.

        P{{csvRecord}} A record to be written to the channel
        R{{}} Returns an error if the record could not be written properly
    }
    public function write(string[] csvRecord) returns error? {
        return dc.write(csvRecord);
    }

    documentation {
        Closes a given CSVChannel.

        R{{}} Returns if an error is encountered
    }
    public function close() returns error? {
        return dc.close();
    }
};
