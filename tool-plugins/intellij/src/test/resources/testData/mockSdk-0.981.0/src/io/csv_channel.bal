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
public type CSVChannel object {
    private DelimitedTextRecordChannel? dc;

    documentation {
        Constructs a CSV channel from a CharacterChannel to read/write CSV records.

        P{{charChannel}} ChracterChannel which will represent the content in the CSV
        P{{fs}} Field separator which will separate between the records in the CSV
        P{{nHeaders}} Number of headers which should be skipped prior to reading records
    }
    public new(CharacterChannel charChannel, Separator fs = ",", int nHeaders = 0) {
        if (fs == TAB){
            dc = new DelimitedTextRecordChannel(charChannel, fmt = "TDF");
        } else if (fs == COLON){
            dc = new DelimitedTextRecordChannel(charChannel, fs = FS_COLON, rs = CSV_RECORD_SEPERATOR);
        } else {
            dc = new DelimitedTextRecordChannel(charChannel, fmt = "CSV");
        }
        skipHeaders(nHeaders);
    }

    documentation {
        Skips the given number of headers.

        P{{nHeaders}} Number of headers which should be skipped
    }
    function skipHeaders(int nHeaders) {
        int count = MINIMUM_HEADER_COUNT;
        while (count < nHeaders){
            var result = getNext();
            count = count + 1;
        }
    }

    documentation {
        Indicates whether there's another record which could be read.

        R{{}} True if there's a record
    }
    public function hasNext() returns boolean {
        match dc{
            DelimitedTextRecordChannel delimitedChannel=>{
                return delimitedChannel.hasNext();
            }
            () =>{
                error e = {message: "Channel not initialized"};
                throw e;
            }
        }
    }

    documentation {
        Gets the next record from the CSV file.

        R{{}} List of fields in the CSV or error
    }
    public function getNext() returns @tainted string[]|error? {
        return dc.getNext();
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

    documentation {
        Returns a table which coresponds to the CSV records.

        P{{structType}} The object the CSV records should be deserialized
        R{{}} Table which represents CSV records or error
    }
    public extern function getTable(typedesc structType) returns @tainted table|error;
};
