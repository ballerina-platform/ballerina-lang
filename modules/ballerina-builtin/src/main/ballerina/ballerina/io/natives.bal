package ballerina.io;

import ballerina.doc;
import ballerina.lang.files;


@doc:Description {value:"Ballerina ByteChannel represents an I/O channel which could be used to source/sink"}
@doc:Field {value:"channelType: specified the type of the channel i.e file/netowrk socket"}
struct ByteChannel{
    string channelType;
}

@doc:Description {value:"Ballerina CharacterChannel reprsents reading character streams from a byte channel"}
struct CharacterChannel{
}

@doc:Description {value:"Ballerina TextRecordChannel will allow reading bytes as records"}
struct TextRecordChannel{
}

@doc:Description { value:"Prepares I/O resource for reading characters"}
@doc:Param { value:"channel: Source of reading/writing bytes" }
@doc:Param {value:"encoding: encoding of the input which is read (i.e UTF-8, ASCCI)"}
@doc:Return {value:"CharacterChannel which will allow read/write characters"}
native function toCharacterChannel(ByteChannel channel,string encoding)(CharacterChannel);

@doc:Description {value:"Gets byte channel form a file"}
@doc:Param {value:"file: representation of file stored in the directory"}
@doc:Return {value:"ByteChannel: channel which will allow to source and sink"}
native function toByteChannel (files:File file) (ByteChannel);

@doc:Description {value:"Gets records from a channel"}
@doc:Param {value:"channel: source for reading/writing characters"}
@doc:Param {value:"recordSeparator: terminating expression to distingush between records"}
@doc:Param {value:"fieldSeparator: terminating expression to ditingish between feilds"}
@doc:Return {value:"TextRecordChannel which will allow reading/writing text records"}
native function toTextRecordChannel(CharacterChannel channel,string recordSeparator,string fieldSeparator)
(TextRecordChannel);

@doc:Description {value:"Reads records from channel"}
@doc:Param {value:"channel: source for reading/writing text records"}
@doc:Return {value:"list of feilds in the record"}
native function readTextRecord(TextRecordChannel channel)(string []);

@doc:Description {value:"Writes record to the channel"}
@doc:Param {value:"channel: source for reading/writing text records"}
@doc:Param {value:"records: writes the list of records"}
native function writeTextRecord(TextRecordChannel channel, string [] records);

@doc:Description {value:"Reads characters from the specfied channel"}
@doc:Param {value:"channel: source of reading characters"}
@doc:Param {value:"numberOfChars: the number of characters read"}
@doc:Return {value:"string: the characters read"}
native function readCharacters(CharacterChannel channel,int numberOfChars)(string);

@doc:Description {value:"Writes characters to specfied channel"}
@doc:Param {value:"channel: the target for writing bytes"}
@doc:Param {value:"content: the content to be written"}
@doc:Param {value:"startOffset: the start position to write bytes"}
@doc:Return {value:"int: the number of characters written"}
native function writeCharacters(CharacterChannel channel,string content,int startOffset)(int);

@doc:Description { value:"Reads bytes from a given channel"}
@doc:Param { value:"channel: Source of reading bytes" }
@doc:Param { value:"numberOfBytes: The number of bytes to be read" }
@doc:Return { value:"blob: Read bytes" }
@doc:Return {value:"int: Number of bytes read"}
native function readBytes (ByteChannel channel,int numberOfBytes) (blob, int);

@doc:Description { value:"Writes bytes to a given channel"}
@doc:Param { value:"channel: Sink bytes to given target" }
@doc:Param { value:"content: The bytes to be written" }
@doc:Param { value:"content: The start possition of the bytes" }
@doc:Return {value:"int: Number of bytes written"}
native function writeBytes(ByteChannel channel,blob content,int startOffset) (int);

@doc:Description { value:"Closes the byte channel"}
@doc:Param { value:"channel: source/sink representation" }
native function close(ByteChannel channel);