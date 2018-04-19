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

package ballerina.io;

@Description {value:"Opens a byte channel from a specified file location"}
@Param {value:"path: path to the file location"}
@Param {value:"accessMode: whether the file should be opened for read,write or append"}
@Return {value:"Channel which will allow to source/sink"}
public native function openFile(@sensitive string path, @sensitive Mode accessMode) returns @tainted ByteChannel;

@Description {value:"Opens a socket from a specified network location"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSocket(@sensitive string host,
                                  @sensitive int port,
                                  SocketProperties options) returns @tainted Socket|error;


@Description {value:"Open a secure socket connection with a remote server"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSecureSocket(@sensitive string host,
                                        @sensitive int port,
                                        SocketProperties options) returns @tainted Socket|error;


@Description {value:"Function to create CSV channel to read CSV input"}
@Param {value:"path: Specfies the path to the CSV file"}
@Param {value:"mode: Specfies the access mode"}
@Param {value:"rf: Specifies the format of the CSV file"}
@Return {value:"DelimitedRecordChannel converted from CSV Channel"}
@Return {value:"Returns an IOError if DelimitedRecordChannel could not be created"}
public function openCsvFile(@sensitive string path,
                            @sensitive Mode mode = "r",
                            @sensitive Seperator fieldSeperator = ",",
                            @sensitive string charset = "UTF-8") returns @tainted CSVChannel|error {
    ByteChannel channel = openFile(path, mode);
    CharacterChannel charChannel = new(channel, charset);
    return new CSVChannel(charChannel, fs = fieldSeperator);
}
