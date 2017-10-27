package ballerina.io;

import ballerina.doc;

@doc:Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public struct ByteChannel{
}

@doc:Description {value:"Ballerina CharacterChannel reprsents a channel which will allow reading/writing characters"}
public struct CharacterChannel{
}

@doc:Description {value:"Ballerina TextRecordChannel represents a channel which will allow read/write text records"}
public struct TextRecordChannel{
}

@doc:Description { value:"Function to convert ByteChannel to CharacterChannel"}
@doc:Param {value:"encoding: the charset/encoding of the content (i.e UTF-8, ASCCI)"}
@doc:Return {value:"CharacterChannel converted from ByteChannel"}
public native function <ByteChannel channel> toCharacterChannel(string encoding)(CharacterChannel);

@doc:Description {value:"Function to convert CharacterChannel to TextRecordChannel"}
@doc:Param {value:"recordSeparator: terminating expression to distingush between records"}
@doc:Param {value:"fieldSeparator: terminating expression to ditingish between feilds"}
@doc:Return {value:"TextRecordChannel converted from CharacterChannel"}
public native function <CharacterChannel channel> toTextRecordChannel(string recordSeparator,
                                                                      string fieldSeparator)
                                                                      (TextRecordChannel);

@doc:Description {value:"Function to read text records"}
@doc:Return {value:"Fields listed in the record"}
public native function <TextRecordChannel channel> readTextRecord()(string []);

@doc:Description {value:"Function to write text records"}
@doc:Param {value:"records: feilds which are included in the record"}
public native function <TextRecordChannel channel> writeTextRecord(string [] records);

@doc:Description {value:"Function to read characters"}
@doc:Param {value:"numberOfChars: number of characters which should be read"}
@doc:Return {value:"the characters which will be read"}
public native function <CharacterChannel channel> readCharacters(int numberOfChars)(string);

@doc:Description {value:"Function to write characters"}
@doc:Param {value:"content: text content which should be written"}
@doc:Param {value:"startOffset: if the content needs to be written with an offset"}
@doc:Return {value:"int: number of characters written"}
public native function <CharacterChannel channel> writeCharacters(string content,int startOffset)(int);

@doc:Description { value:"Function to read bytes"}
@doc:Param { value:"numberOfBytes: number of bytes which should be read" }
@doc:Return { value:"blob: the bytes which were read" }
@doc:Return {value:"int: number of bytes which were read"}
public native function <ByteChannel channel> readBytes (int numberOfBytes) (blob, int);

@doc:Description { value:"Function to write bytes"}
@doc:Param { value:"content: bytes which should be written" }
@doc:Param { value:"content: whether the bytes should be writtne with an offset" }
@doc:Return {value:"int: number of bytes written"}
public native function <ByteChannel channel> writeBytes(blob content,int startOffset) (int);

@doc:Description { value:"Function to close the byte channel"}
public native function <ByteChannel channel> close();
