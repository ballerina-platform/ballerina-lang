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
   Represents the set of permissions supported to open file.

   READ - open the file in read mode
   WRITE - open the file in write mode
   READ/WRITE - open the file either to read or write
   APPEND - append to existing file instead of replacing
}
public type Mode "r"|"w"|"rw"|"a";
@final public Mode READ = "r";
@final public Mode WRITE = "w";
@final public Mode RW = "rw";
@final public Mode APPEND = "a";

documentation {
    Retrieves a ByteChannel from a given file path.

    P{{path}} Relative/absolute path string to locate the file
    P{{accessMode}} Permission to open the file
    R{{}} ByteChannel representation of the file resource
}
public extern function openFile(@sensitive string path, @sensitive Mode accessMode) returns @tainted ByteChannel;

documentation {
    Opens a secure socket connection with a remote server.

    P{{host}} Remote server domain/IP
    P{{port}} Remote server port
    P{{options}} Mata data to initialize the connection(i.e security information)
    R{{}} Socket which will represent the network object or an error
}
public extern function openSecureSocket(@sensitive string host,
                                        @sensitive int port,
                                        SocketProperties options) returns @tainted Socket|error;

documentation {
    Creates an in-memory channel which will reference stream of bytes.

    P{{content}} Content which should be exposed as channel
    R{{}} ByteChannel represenation to read the memory content
}
public extern function createMemoryChannel(byte[] content) returns ByteChannel;

documentation {
    Retrieves a CSV channel from a give file path.

    P{{path}} File path which describes the location of the CSV
    P{{mode}} Permission which should be used to open CSV file
    P{{fieldSeparator}} CSV record seperator (i.e comma or tab)
    P{{charset}} Encoding characters in the file represents
    P{{skipHeaders}} Number of headers which should be skipped
    R{{}} CSVChannel which could be used to iterate through the CSV records
}
public function openCsvFile(@sensitive string path,
                            @sensitive Mode mode = "r",
                            @sensitive Separator fieldSeparator = ",",
                            @sensitive string charset = "UTF-8",
                            @sensitive int skipHeaders = 0) returns @tainted CSVChannel {
    ByteChannel channel = openFile(path, mode);
    CharacterChannel charChannel = new(channel, charset);
    return new CSVChannel(charChannel, fs = fieldSeparator, nHeaders = skipHeaders);
}
