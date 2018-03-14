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

@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public struct ByteChannel {
}

@Description {value:"Ballerina CharacterChannel represents a channel which will allow to read/write characters"}
public struct CharacterChannel {
}

@Description {value:"Ballerina DelimitedRecordChannel represents a channel which will allow to read/write text records"}
public struct DelimitedRecordChannel {
}

@Description {value:"Represents an error which will occur while performing I/O operations"}
public struct IOError {
    string message;
    error cause;
}

@Description {value:"Represents a client socket connection. This can be used to communicate with a remote machine"}
@Field {value:"channel: Connection stream that bridge the client and the server"}
@Field {value:"port: Remote server port"}
@Field {value:"localPort: Client side port that open for the communication"}
@Field {value:"address: Remote server domain/IP"}
@Field {value:"localAddress: Client domain/IP"}
public struct Socket {
    ByteChannel channel;
    int port;
    int localPort;
    string address;
    string localAddress;
}

@Description {value:"SocketProperties structs represents the properties which are used to configure TCP connection"}
@Field {value:"localPort: Client side port that open for the communication"}
@Field {value:"keyStoreFile: File path to keystore file"}
@Field {value:"keyStorePassword: The keystore password"}
@Field {value:"trustStoreFile: File path to truststore file"}
@Field {value:"trustStorePassword: The truststore password"}
@Field {value:"certPassword: The certificate password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled"}
@Field {value:"ciphers: List of ciphers to be used"}
@Field {value:"sslProtocol: The SSL protocol version"}
public struct SocketProperties {
    int localPort;
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
}

@Description {value:"Opens a byte channel from a specified file location"}
@Param {value:"path: path to the file location"}
@Param {value:"accessMode: whether the file should be opened for read,write or append"}
@Return {value:"Channel which will allow to source/sink"}
public native function openFile (string path, string accessMode) (ByteChannel);

@Description {value:"Opens a socket from a specified network location"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSocket (string host, int port, SocketProperties options) (Socket, IOError);

@Description {value:"Function to create a CharacterChannel from ByteChannel"}
@Param {value:"channel: The ByteChannel to be converted"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Return {value:"CharacterChannel converted from ByteChannel"}
@Return {value:"Returns an IOError if CharacterChannel could not be created"}
public native function createCharacterChannel (ByteChannel byteChannel, string encoding) (CharacterChannel, IOError);

@Description {value:"Function to create a CharacterChannel to DelimitedRecordChannel"}
@Param {value:"channel: The CharacterChannel to be converted"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Return {value:"DelimitedRecordChannel converted from CharacterChannel"}
@Return {value:"Returns an IOError if DelimitedRecordChannel could not be created"}
public native function createDelimitedRecordChannel (CharacterChannel channel, string recordSeparator,
                                                     string fieldSeparator) (DelimitedRecordChannel, IOError);

@Description {value:"Function to read bytes"}
@Param {value:"channel: The ByteChannel to read bytes from"}
@Param {value:"numberOfBytes: Number of bytes which should be read"}
@Return {value:"The bytes which were read"}
@Return {value:"Number of bytes read"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <ByteChannel channel> read (int numberOfBytes, int offset) (blob, int, IOError);

@Description {value:"Function to write bytes"}
@Param {value:"channel: The ByteChannel to write bytes to"}
@Param {value:"content: Bytes which should be written"}
@Param {value:"startOffset: If the bytes need to be written with an offset, the value of that offset"}
@Return {value:"Number of bytes written"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <ByteChannel channel> write (blob content, int startOffset, int numberOfBytes) (int, IOError);

@Description {value:"Function to read characters"}
@Param {value:"channel: The CharacterChannel to read characters from"}
@Param {value:"numberOfChars: Number of characters which should be read"}
@Return {value:"The character sequence which was read"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <CharacterChannel channel> readCharacters (int numberOfChars) (string, IOError);

@Description {value:"Function to write characters"}
@Param {value:"channel: The CharacterChannel which will allow writing characters"}
@Param {value:"content: Text content which should be written"}
@Param {value:"startOffset: If the content needs to be written with an offset, the value of that offset"}
@Return {value:"Number of characters written"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <CharacterChannel channel> writeCharacters (string content, int startOffset) (int, IOError);

@Description {value:"Function to check whether next record is available or not"}
@Param {value:"channel: The DelimitedRecordChannel to read text records from"}
@Return {value:"True if the channel has more records; false otherwise"}
public native function <DelimitedRecordChannel channel> hasNextTextRecord () (boolean);

@Description {value:"Function to read text records"}
@Param {value:"channel: The DelimitedRecordChannel to read text records from"}
@Return {value:"Fields listed in the record"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <DelimitedRecordChannel channel> nextTextRecord () (string[], IOError);

@Description {value:"Function to write text records"}
@Param {value:"channel: The DelimitedRecordChannel to write records"}
@Param {value:"records: Fields which are included in the record"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <DelimitedRecordChannel channel> writeTextRecord (string[] records) (IOError);

@Description {value:"Function to close a byte channel"}
@Param {value:"channel: The ByteChannel to be closed"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <ByteChannel channel> close () (IOError);

@Description {value:"Function to close the text record channel"}
@Param {value:"channel: The DelimitedRecordChannel to be closed"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <DelimitedRecordChannel channel> closeDelimitedRecordChannel () (IOError);

@Description {value:"Function to close a character channel"}
@Param {value:"channel: The CharacterChannel to be closed"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function <CharacterChannel channel> closeCharacterChannel () (IOError);

@Description {value:"Open a secure socket connection with a remote server"}
@Param {value:"host: Remote server domain/IP"}
@Param {value:"port: Remote server port"}
@Param {value:"options: Connection stream that bridge the client and the server"}
@Return {value:"Socket which will allow to communicate with a remote server"}
@Return {value:"Returns an IOError if unable to open the socket connection"}
public native function openSecureSocket (string host, int port, SocketProperties options) (Socket, IOError);

@Description {value:"Close the socket connection with the remote server"}
@Param {value:"socket: The client socket connection to be to be closed"}
@Return {value:"Returns an IOError if socket could not be closed"}
public native function <Socket socket> closeSocket () (IOError);

@Description {value:"Function to load delimited records to in-memory table"}
@Param {value:"filePath: Path to delimited file"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Param {value:"headerLineIncluded: To indicate whether given file included the header line or not"}
@Param {value:"structType: Name of the struct that each record need to populate"}
@Return {value:"table of delimited values"}
@Return {value:"Returns if there's any error while performaing I/O operation"}
public native function loadToTable(string filePath, string recordSeparator, string fieldSeparator,
                                   string encoding, boolean headerLineIncluded, type structType) (table, IOError);
