package ballerina.io;

@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public struct ByteChannel{
}

@Description {value:"Ballerina CharacterChannel reprsents a channel which will allow reading/writing characters"}
public struct CharacterChannel{
}

@Description {value:"Ballerina TextRecordChannel represents a channel which will allow read/write text records"}
public struct TextRecordChannel{
}

@Description { value:"Function to convert ByteChannel to CharacterChannel"}
@Param {value:"encoding: the charset/encoding of the content (i.e UTF-8, ASCCI)"}
@Return {value:"CharacterChannel converted from ByteChannel"}
public native function <ByteChannel channel> toCharacterChannel(string encoding)(CharacterChannel);

@Description {value:"Function to convert CharacterChannel to TextRecordChannel"}
@Param {value:"recordSeparator: terminating expression to distingush between records"}
@Param {value:"fieldSeparator: terminating expression to ditingish between feilds"}
@Return {value:"TextRecordChannel converted from CharacterChannel"}
public native function <CharacterChannel channel> toTextRecordChannel(string recordSeparator,
                                                                      string fieldSeparator)
                                                                      (TextRecordChannel);

@Description {value:"Function to read text records"}
@Return {value:"Fields listed in the record"}
public native function <TextRecordChannel channel> readTextRecord()(string []);

@Description {value:"Function to write text records"}
@Param {value:"records: feilds which are included in the record"}
public native function <TextRecordChannel channel> writeTextRecord(string [] records);

@Description {value:"Function to read characters"}
@Param {value:"numberOfChars: number of characters which should be read"}
@Return {value:"the characters which will be read"}
public native function <CharacterChannel channel> readCharacters(int numberOfChars)(string);

@Description {value:"Function to write characters"}
@Param {value:"content: text content which should be written"}
@Param {value:"startOffset: if the content needs to be written with an offset"}
@Return {value:"int: number of characters written"}
public native function <CharacterChannel channel> writeCharacters(string content,int startOffset)(int);

@Description { value:"Function to read bytes"}
@Param { value:"numberOfBytes: number of bytes which should be read" }
@Return { value:"blob: the bytes which were read" }
@Return {value:"int: number of bytes which were read"}
public native function <ByteChannel channel> readBytes (int numberOfBytes) (blob, int);

@Description { value:"Function to write bytes"}
@Param { value:"content: bytes which should be written" }
@Param { value:"content: whether the bytes should be writtne with an offset" }
@Return {value:"int: number of bytes written"}
public native function <ByteChannel channel> writeBytes(blob content,int startOffset) (int);

@Description { value:"Function to close the byte channel"}
public native function <ByteChannel channel> close();
