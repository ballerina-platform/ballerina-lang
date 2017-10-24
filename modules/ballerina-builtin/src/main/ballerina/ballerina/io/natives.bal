package ballerina.io;

import ballerina.doc;


@doc:Description {value:"Ballerina ByteChannel represents an I/O channel which could be used to source/sink"}
public struct ByteChannel{
}

@doc:Description {value:"Ballerina CharacterChannel reprsents reading character streams from a byte channel"}
public struct CharacterChannel{
}

@doc:Description {value:"Ballerina TextRecordChannel will allow reading bytes as records"}
public struct TextRecordChannel{
}

@doc:Description {value:"Ballerina Delimited Binary Record Channel will allow writing binary records"}
public struct DBRecordChannel{
}

@doc:Description { value:"Prepares I/O resource for reading characters"}
@doc:Param {value:"encoding: encoding of the input which is read (i.e UTF-8, ASCCI)"}
@doc:Return {value:"CharacterChannel which will allow read/write characters"}
public native function <ByteChannel channel> toCharacterChannel(string encoding)(CharacterChannel);

@doc:Description {value:"Prepares I/O resource for reading delimited binary records"}
@doc:Param {value:"recordSeparator:dlimited of the record"}
@doc:Return{value:"DBRecordChannel which will allow reding delimited binary records"}
public native function <ByteChannel channel> toDBRecordChannel(blob recordSeparator)(DBRecordChannel);

@doc:Description {value:"Gets records from a channel"}
@doc:Param {value:"recordSeparator: terminating expression to distingush between records"}
@doc:Param {value:"fieldSeparator: terminating expression to ditingish between feilds"}
@doc:Return {value:"TextRecordChannel which will allow reading/writing text records"}
public native function <CharacterChannel channel> toTextRecordChannel(string recordSeparator,string fieldSeparator)
(TextRecordChannel);

@doc:Description {value:"Reads records from channel"}
@doc:Return {value:"list of feilds in the record"}
public native function <TextRecordChannel channel> readTextRecord()(string []);

@doc:Description {value:"Writes record to the channel"}
@doc:Param {value:"records: writes the list of records"}
public native function <TextRecordChannel channel> writeTextRecord(string [] records);

@doc:Description {value:"Reads characters from the specfied channel"}
@doc:Param {value:"numberOfChars: the number of characters read"}
@doc:Return {value:"string: the characters read"}
public native function <CharacterChannel channel> readCharacters(int numberOfChars)(string);

@doc:Description {value:"Writes characters to specfied channel"}
@doc:Param {value:"content: the content to be written"}
@doc:Param {value:"startOffset: the start position to write bytes"}
@doc:Return {value:"int: the number of characters written"}
public native function <CharacterChannel channel> writeCharacters(string content,int startOffset)(int);

@doc:Description{value:"Read delimited byte record"}
@doc:Return{value:"Read delimited byte record"}
public native function <DBRecordChannel channel> readDBRecord()(blob);

@doc:Description{value:"Write delimited byte record"}
@doc:Return{value:"Record which should be written"}
public native function <DBRecordChannel channel> writeDBRecord(blob record);

@doc:Description { value:"Reads bytes from a given channel"}
@doc:Param { value:"numberOfBytes: The number of bytes to be read" }
@doc:Return { value:"blob: Read bytes" }
@doc:Return {value:"int: Number of bytes read"}
public native function <ByteChannel channel> readBytes (int numberOfBytes) (blob, int);

@doc:Description { value:"Writes bytes to a given channel"}
@doc:Param { value:"content: The bytes to be written" }
@doc:Param { value:"content: The start possition of the bytes" }
@doc:Return {value:"int: Number of bytes written"}
public native function <ByteChannel channel> writeBytes(blob content,int startOffset) (int);

@doc:Description { value:"Closes the byte channel"}
public native function <ByteChannel channel> close();
