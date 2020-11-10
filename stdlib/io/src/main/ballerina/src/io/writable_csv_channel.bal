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


# Represents the record separator of the CSV file.
public const string CSV_RECORD_SEPARATOR = "\n";


# Represents the colon separator, which should be used to identify colon-separated files.
public const string FS_COLON = ":";


# Represents the minimum number of headers, which will be included in the CSV.
public const int MINIMUM_HEADER_COUNT = 0;


# Represents a WritableCSVChannel, which could be used to write records from the CSV file.
public class WritableCSVChannel {
    private WritableTextRecordChannel? dc;

    # Constructs a CSV channel from a `CharacterChannel` to read/write CSV records.
    #
    # + CharacterChannel - The `CharacterChannel`, which will represent the content in the CSV file
    # + fs - Field separator, which will separate the records in the CSV
    public function init(WritableCharacterChannel characterChannel, Separator fs = ",") {
        if (fs == TAB) {
            self.dc = new WritableTextRecordChannel(characterChannel, fmt = "TDF");
        } else if (fs == COLON) {
            self.dc = new WritableTextRecordChannel(characterChannel, FS_COLON, CSV_RECORD_SEPARATOR);
        } else if (fs == COMMA) {
            self.dc = new WritableTextRecordChannel(characterChannel, fmt = "CSV");
        } else {
            self.dc = new WritableTextRecordChannel(characterChannel, fs, CSV_RECORD_SEPARATOR);
        }
    }

# Writes the record to a given CSV file.
# ```ballerina
# io:Error err = csvChannel.write(record);
# ```
#
# + csvRecord - A record to be written to the channel
# + return - An `io:Error` if the record could not be written properly
    public function write(string[] csvRecord) returns Error? {
        if(self.dc is WritableTextRecordChannel){
            var result = <WritableTextRecordChannel> self.dc;
            return result.write(csvRecord);
        }
        return ();
    }

# Closes a given `CSVChannel`.
# ```ballerina
# io:Error? err = csvChannel.close();
# ```
#
# + return - `()` or else `io:Error` if any error occurred
    public function close() returns Error? {
        if(self.dc is WritableTextRecordChannel){
            var result = <WritableTextRecordChannel> self.dc;
            return result.close();
        }
        return ();
    }
}
