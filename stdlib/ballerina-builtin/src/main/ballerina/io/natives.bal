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

@Description {value:"Describes access mode for reading"}
@final public string MODE_R = "r";

@Description {value:"Describes access mode for writing"}
@final public string MODE_W = "w";

@Description {value:"Describes acces mode for reading and writing"}
@final public string MODE_RW = "rw";

@Description {value:"Describes access mode for append"}
@final public string MODE_A = "a";

@Description {value:"Describes default format to open CSV"}
@final public string FORMAT_DEFAULT = "default";

@Description {value:"Describes RFC4180 format to open CSV"}
@final public string FORMAT_RFC4180 = "RFC4180";

@Description {value:"Describes TDF format to open CSV"}
@final public string FORMAT_TDF = "TDF";

@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public type ByteChannel object {
    @Description {value:"Function to read bytes"}
    @Param {value:"nBytes: Number of bytes which should be read"}
    @Return {value:"The bytes which were read"}
    @Return {value:"Number of bytes read"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function read (@sensitive int nBytes) returns ((blob, int) | IOError);

    @Description {value:"Function to write bytes"}
    @Param {value:"content: Bytes which should be written"}
    @Param {value:"offset: If the bytes need to be written with an offset, the value of that offset"}
    @Return {value:"Number of bytes written"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function write (blob content, int offset) returns (int | IOError);

    @Description {value:"Function to close a byte channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function close () returns (IOError | ());
};

@Description {value:"Ballerina CharacterChannel represents a channel which will allow to read/write characters"}
public type CharacterChannel object {
    @Description {value:"Function to read characters"}
    @Param {value:"numberOfChars: Number of characters which should be read"}
    @Return {value:"The character sequence which was read"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function readCharacters (@sensitive int numberOfChars) returns (string | IOError);

    @Description {value:"Function to write characters"}
    @Param {value:"content: Text content which should be written"}
    @Param {value:"startOffset: If the content needs to be written with an offset, the value of that offset"}
    @Return {value:"Number of characters written"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function writeCharacters (string content, int startOffset) returns (int | IOError);

    @Description {value:"Function to close a character channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function closeCharacterChannel () returns (IOError | ());

    @Description {value:"Function to convert a character channel to a JSON"}
    @Return {value:"Returns A JSON"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function readJson () returns (json|IOError);

    @Description {value:"Function to convert a character channel to a XML"}
    @Return {value:"Returns A XML"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function readXml () returns (xml|IOError);

    @Description {value:"Writes json through a given character channel"}
    public native function writeJson(json content) returns (IOError | ());

    @Description {value:"Writes xml through a given character channel"}
    public native function writeXml(xml content) returns (IOError | ());
};

@Description {value:"Ballerina DelimitedRecordChannel represents a channel which will allow to read/write text records"}
public type DelimitedRecordChannel object {
    @Description {value:"Function to check whether next record is available or not"}
    @Return {value:"True if the channel has more records; false otherwise"}
    public native function hasNextTextRecord () returns (boolean);

    @Description {value:"Function to read text records"}
    @Return {value:"Fields listed in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function nextTextRecord () returns (string[] | IOError);

    @Description {value:"Function to write text records"}
    @Param {value:"records: Fields which are included in the record"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function writeTextRecord (string[] records) returns (IOError);

    @Description {value:"Function to close the text record channel"}
    @Return {value:"Returns if there's any error while performaing I/O operation"}
    public native function closeDelimitedRecordChannel () returns (IOError | ());
};

documentation {
    Represents an error which will occur while performing I/O operations
}
public type IOError {
    string message;
    error? cause;
};

documentation {
    Represetns a TCP socket.
}
public type Socket object {
    public {
        ByteChannel channel;
        int port;
        int localPort;
        string address;
        string localAddress;
    }
    @Description {value:"Close the socket connection with the remote server"}
    @Return {value:"Returns an IOError if socket could not be closed"}
    public native function closeSocket () returns (IOError);
};

documentation {
    SocketProperties structs represents the properties which are used to configure TCP connection.
}
public type SocketProperties {
        int localPort;
        string keyStoreFile;
        string keyStorePassword;
        string trustStoreFile;
        string trustStorePassword;
        string certPassword;
        string sslEnabledProtocols;
        string ciphers;
        string sslProtocol;
};

@Description {value:"Opens a byte channel from a specified file location"}
@Param {value:"path: path to the file location"}
@Param {value:"accessMode: whether the file should be opened for read,write or append"}
@Return {value:"Channel which will allow to source/sink"}
public native function openFile (@sensitive string path, @sensitive string accessMode) returns (ByteChannel);

@Description {value:"Opens a socket from a specified network location"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSocket (@sensitive string host, @sensitive int port,
                                   SocketProperties options) returns (Socket | IOError);

@Description {value:"Function to create a CharacterChannel from ByteChannel"}
@Param {value:"channel: The ByteChannel to be converted"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Return {value:"CharacterChannel converted from ByteChannel"}
@Return {value:"Returns an IOError if CharacterChannel could not be created"}
public native function createCharacterChannel (ByteChannel byteChannel,
                                               string encoding) returns (CharacterChannel | IOError);

@Description {value:"Function to create a CharacterChannel to DelimitedRecordChannel"}
@Param {value:"channel: The CharacterChannel to be converted"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Return {value:"DelimitedRecordChannel converted from CharacterChannel"}
@Return {value:"Returns an IOError if DelimitedRecordChannel could not be created"}
public native function createDelimitedRecordChannel (CharacterChannel channel, string recordSeparator,
                                                     string fieldSeparator) returns (DelimitedRecordChannel | IOError);

@Description {value:"Function to create CSV channel to read CSV input"}
@Param {value:"path: Specfies the path to the CSV file"}
@Param {value:"mode: Specfies the access mode"}
@Param {value: "rf: Specifies the format of the CSV file"}
@Return {value:"DelimitedRecordChannel converted from CSV Channel"}
@Return {value:"Returns an IOError if DelimitedRecordChannel could not be created"}
public native function createCsvChannel (string path, string mode="rw", string rf="DEFAULT",
string charset="UTF-8") returns (DelimitedRecordChannel | IOError);

@Description {value:"Open a secure socket connection with a remote server"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSecureSocket (@sensitive string host, @sensitive int port, SocketProperties options)
returns (Socket | IOError);

@Description {value:"Function to load delimited records to in-memory table"}
@Param {value:"filePath: Path to delimited file"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Param {value:"headerLineIncluded: To indicate whether given file included the header line or not"}
@Param {value:"structType: Name of the struct that each record need to populate"}
@Return {value:"table of delimited values"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function loadToTable (@sensitive string filePath, string recordSeparator, string fieldSeparator,
                                    string encoding, boolean headerLineIncluded, typedesc structType)
returns (table | IOError);
