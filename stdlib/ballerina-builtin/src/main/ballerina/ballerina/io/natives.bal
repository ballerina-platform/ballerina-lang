package ballerina.io;

@Description {value:"Ballerina ByteChannel represents a channel which will allow I/O operations to be done"}
public struct ByteChannel{
}

@Description {value:"Ballerina CharacterChannel represents a channel which will allow to read/write characters"}
public struct CharacterChannel{
}

@Description {value:"Ballerina DelimitedRecordChannel represents a channel which will allow to read/write text records"}
public struct DelimitedRecordChannel {
}

@Description { value:"Function to convert a ByteChannel to CharacterChannel"}
@Param {value:"channel: The ByteChannel to be converted"}
@Param {value:"encoding: The charset/encoding of the content (i.e UTF-8, ASCII)"}
@Return {value:"CharacterChannel converted from ByteChannel"}
documentation {
Function to convert a ByteChannel to CharacterChannel.
- #channel The ByteChannel to be converted
- #encoding The charset/encoding of the content (i.e UTF-8, ASCII)
- #charChannel CharacterChannel converted from ByteChannel
}
public native function <ByteChannel channel> toCharacterChannel(string encoding)(CharacterChannel charChannel);

@Description {value:"Function to convert a CharacterChannel to DelimitedRecordChannel"}
@Param {value:"channel: The CharacterChannel to be converted"}
@Param {value:"recordSeparator: Terminating expression to distinguish between records"}
@Param {value:"fieldSeparator: Terminating expression to distinguish between fields"}
@Return {value:"DelimitedRecordChannel converted from CharacterChannel"}
documentation {
Function to convert a CharacterChannel to DelimitedRecordChannel.
- #channel The CharacterChannel to be converted
- #recordSeparator Terminating expression to distinguish between records
- #fieldSeparator Terminating expression to distinguish between fields
- #dRcdChannel DelimitedRecordChannel converted from CharacterChannel
}
public native function <CharacterChannel channel> toTextRecordChannel(string recordSeparator,
                                                                      string fieldSeparator)
(DelimitedRecordChannel dRcdChannel);

@Description {value:"Function to read text records"}
@Param {value:"channel: The DelimitedRecordChannel to read text records from"}
@Return {value:"Fields listed in the record"}
documentation {
Function to read text records.
- #channel The DelimitedRecordChannel to read text records from
- #txtRecord Fields listed in the record
}
public native function <DelimitedRecordChannel channel> nextTextRecord () (string[]);

@Description {value:"Function to write text records"}
@Param {value:"channel: The DelimitedRecordChannel to write records"}
@Param {value:"records: Fields which are included in the record"}
documentation {
Function to write text records.
- #channel The DelimitedRecordChannel to write text records to
- #records Fields which are included in the record
}
public native function <DelimitedRecordChannel channel> writeTextRecord (string[] records);

@Description{value:"Function to close the text record channel"}
@Param {value:"channel: The DelimitedRecordChannel to be closed"}
documentation {
Function to close the text record channel.
- #channel The DelimitedRecordChannel to be closed
}
public native function <DelimitedRecordChannel channel> closeDelimitedRecordChannel();

@Description {value:"Function to read characters"}
@Param {value:"channel: The CharacterChannel to read characters from"}
@Param {value:"numberOfChars: Number of characters which should be read"}
@Return {value:"The character sequence which was read"}
documentation {
Function to read characters.
- #channel The CharacterChannel to read characters from
- #numberOfChars Number of characters which should be read
- #chars The character sequence which was read
}
public native function <CharacterChannel channel> readCharacters(int numberOfChars)(string chars);

@Description {value:"Function to read all characters in the give I/O source"}
@Return {value:"all characters read"}
documentation {
Function to read all characters in the give I/O source.
- #channel The CharacterChannel to read characters from
- #chars all characters read
}
public native function <CharacterChannel channel> readAllCharacters()(string chars);

@Description {value:"Function to write characters"}
@Param {value:"channel: The CharacterChannel to write characters to"}
@Param {value:"content: Text content which should be written"}
@Param {value:"startOffset: If the content needs to be written with an offset, the value of that offset"}
@Return { value:"Number of characters written"}
documentation {
Function to write characters.
- #channel The CharacterChannel to write characters to
- #content Text content which should be written
- #startOffset If the content needs to be written with an offset, the value of that offset
- #numOfChars Number of characters written
}
public native function <CharacterChannel channel> writeCharacters(string content,int startOffset)(int numOfChars);

@Description {value:"Function to close a character channel"}
@Param {value:"channel: The CharacterChannel to be closed"}
documentation {
Function to close a character channel.
- #channel The CharacterChannel to be closed
}
public native function <CharacterChannel channel> closeCharacterChannel();

@Description { value:"Function to read bytes"}
@Param {value:"channel: The ByteChannel to read bytes from"}
@Param { value:"numberOfBytes: Number of bytes which should be read" }
@Return { value:"The bytes which were read" }
@Return { value:"Number of bytes read"}
documentation {
Function to read bytes.
- #channel The ByteChannel read bytes from
- #numberOfBytes Number of bytes which should be read
- #bytes The bytes which were read
- #numOfBytesRead Number of bytes read
}
public native function <ByteChannel channel> readBytes (int numberOfBytes) (blob bytes, int numOfBytesRead);

@Description {value:"Function to read all bytes in the given I/O source"}
@Return {value:"all bytes read from the channel"}
documentation {
Function to read all bytes in the given I/O source.
- #channel The ByteChannel to be converted
- #bytes All bytes read from the channel
- #numOfBytesRead Number of bytes read
}
public native function <ByteChannel channel> readAllBytes()(blob bytes,int numOfBytesRead);

@Description { value:"Function to write bytes"}
@Param {value:"channel: The ByteChannel to write bytes to"}
@Param { value:"content: Bytes which should be written" }
@Param { value:"startOffset: If the bytes need to be written with an offset, the value of that offset" }
@Return { value:"Number of bytes written"}
documentation {
Function to write bytes.
- #channel The ByteChannel to write bytes to
- #content Bytes which should be written
- #startOffset If the bytes need to be written with an offset, the value of that offset
- #numOfBytes Number of bytes written
}
public native function <ByteChannel channel> writeBytes(blob content,int startOffset) (int numOfBytes);

@Description { value:"Function to close a byte channel"}
@Param {value:"channel: The ByteChannel to be closed"}
documentation {
Function to close a byte channel
- #channel The ByteChannel to be closed
}
public native function <ByteChannel channel> close();

@Description {value:"Function to check whether next record is available or not"}
@Param {value:"channel: The DelimitedRecordChannel to read text records from"}
@Return {value:"True if the channel has more records; false otherwise"}
documentation {
Function to check whether next record is available or not
- #channel The DelimitedRecordChannel to read text records from
- #hasNext True if the channel has more records; false otherwise
}
public native function <DelimitedRecordChannel channel> hasNextTextRecord () (boolean hasNext);
