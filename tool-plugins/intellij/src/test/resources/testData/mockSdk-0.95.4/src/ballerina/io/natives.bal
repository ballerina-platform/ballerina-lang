package ballerina.io;

@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public struct ByteChannel{
}

@Description {value:"Ballerina CharacterChannel represents a channel which will allow to read/write characters"}
public struct CharacterChannel{
}

@Description {value:"Ballerina TextRecordChannel represents a channel which will allow to read/write text records"}
public struct TextRecordChannel{
}

@Description {value:"Function to create a CharacterChannel from ByteChannel "}
@Param {value:"channel: The ByteChannel to be converted"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Return {value:"CharacterChannel converted from ByteChannel"}
public native function createCharacterChannel (ByteChannel byteChannel, string encoding) (CharacterChannel);

@Description {value:"Function to convert a CharacterChannel to TextRecordChannel"}
@Param {value:"channel: The CharacterChannel to be converted"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Return {value:"TextRecordChannel converted from CharacterChannel"}
public native function <CharacterChannel channel> toTextRecordChannel(string recordSeparator,
                                                                      string fieldSeparator)
                                                                      (TextRecordChannel);

@Description {value:"Function to read text records"}
@Param {value:"channel: The TextRecordChannel to read text records from"}
@Return {value:"Fields listed in the record"}
public native function <TextRecordChannel channel> readTextRecord()(string []);

@Description {value:"Function to write text records"}
@Param {value:"channel: The TextRecordChannel to write text records to"}
@Param {value:"records: Fields which are included in the record"}
public native function <TextRecordChannel channel> writeTextRecord(string [] records);

@Description{value:"Function to close the text record channel"}
@Param {value:"channel: The TextRecordChannel to be closed"}
public native function <TextRecordChannel channel> closeTextRecordChannel();

@Description {value:"Function to read characters"}
@Param {value:"channel: The CharacterChannel to read characters from"}
@Param {value:"numberOfChars: Number of characters which should be read"}
@Return {value:"The character sequence which was read"}
public native function <CharacterChannel channel> readCharacters(int numberOfChars)(string);

@Description {value:"Function to read all characters in the give I/O source"}
@Return {value:"all characters read"}
public native function <CharacterChannel channel> readAllCharacters()(string);

@Description {value:"Function to write characters"}
@Param {value:"channel: The CharacterChannel to write characters to"}
@Param {value:"content: Text content which should be written"}
@Param {value:"startOffset: If the content needs to be written with an offset, the value of that offset"}
@Return { value:"Number of characters written"}
public native function <CharacterChannel channel> writeCharacters(string content,int startOffset)(int);

@Description {value:"Function to close a character channel"}
@Param {value:"channel: The CharacterChannel to be closed"}
public native function <CharacterChannel channel> closeCharacterChannel();

@Description { value:"Function to read bytes"}
@Param {value:"channel: The ByteChannel to read bytes from"}
@Param { value:"numberOfBytes: Number of bytes which should be read" }
@Return { value:"The bytes which were read" }
@Return { value:"Number of bytes read"}
public native function <ByteChannel channel> readBytes (int numberOfBytes) (blob, int);

@Description {value:"Function to read all bytes in the given I/O source"}
@Return {value:"all bytes read from the channel"}
public native function <ByteChannel channel> readAllBytes()(blob,int);

@Description { value:"Function to write bytes"}
@Param {value:"channel: The ByteChannel to write bytes to"}
@Param { value:"content: Bytes which should be written" }
@Param { value:"startOffset: If the bytes need to be written with an offset, the value of that offset" }
@Return { value:"Number of bytes written"}
public native function <ByteChannel channel> writeBytes(blob content,int startOffset) (int);

@Description { value:"Function to close a byte channel"}
@Param {value:"channel: The ByteChannel to be closed"}
public native function <ByteChannel channel> close();

@Description {value:"Function to return a ByteChannel related to the file. This ByteChannel can then be used to read/write from/to the file."}
@Param {value:"path: The path of the file"}
@Param {value:"accessMode: Specifies whether the file should be opened for reading or writing (r/w)"}
@Return {value:"ByteChannel which will allow to perform I/O operations"}
public native function openFile (string path, string accessMode) (io:ByteChannel);
