## Module Overview

This module is designed to support input and output operations via channels in a canonical way, either in a blocking,
or non-blocking manner.

![architecture](resources/package-architecture.svg)

### Channels
A channel represents an I/O source or sink of some bytes, characters, or records that are opened for reading, or
writing, respectively.

### Byte channels
The most primitive channel is the `ByteChannel` which reads and writes 8-bit bytes. For an example on `ByteChannel` see [Byte I/O Example](https://ballerina.io/v1-2/learn/by-example/byte-io.html).

### Character channels
The `CharacterChannel` is used to read and write characters. The charset encoding is specified when creating the `CharacterChannel`. For example on `CharacterChannel` see [Read/Write Files Example](https://ballerina.io/v1-2/learn/by-example/character-io.html).

If a `ReadableCharacterChannel` points to a JSON or XML source, it can be read and then written, directly into a variable of
the respective type. For examples on reading/writing JSON or XML sources see [JSON I/O](https://ballerina.io/v1-2/learn/by-example/json-io.html), and [XML I/O](https://ballerina.io/v1-2/learn/by-example/xml-io.html).

### Record channels

Ballerina also supports I/O for delimited records. For example on reading on records in text file see [Record I/O](https://ballerina.io/v1-2/learn/by-example/record-io.html).

A `.CSV` file can be read and written directly into a `CSVChannel`, as shown in this [CSV I/O Example](https://ballerina.io/v1-2/learn/by-example/csv-io.html).

### Data Channels
Ballerina supports performing data i/o operations

Person object could be serialized into a file or a network socket as seen in [Data I/O Example](https://ballerina.io/v1-2/learn/by-example/data-io.html).


