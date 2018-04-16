package ballerina.compression;

import ballerina/file;

@Description {value:"Represent all compression related errors"}
@Field {value:"message: The error message"}
@Field {value:"cause: The error which caused the compression error"}
public type CompressionError  {
    string message,
    error? cause,
};

@Description {value:"Decompresses a blob into a directory"}
@Param {value:"content: Blob of the compressed file"}
@Param {value:"destDir: Path of the directory to decompress the file"}
@Return {value:"An error if an error occurs during the decompression process"}
public native function decompressFromBlob(blob content, file:Path destDir) returns error|();

@Description {value:"Decompresses a compressed file"}
@Param {value:"content: Path of the compressed file"}
@Param {value:"destDir: Path of the directory to decompress the file"}
@Return {value:"An error if an error occurs during the decompression process"}
public native function decompress(file:Path dirPath, file:Path destDir) returns error|();

@Description {value:"Compresses a directory"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Param {value:"destDir: Path of the directory to place the compressed file"}
@Return {value:"An error if an error occurs during the compression process"}
public native function compress(file:Path dirPath, file:Path destDir) returns error|();

@Description {value:"Compresses a directory into a blob"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Return {value:"Compressed blob of the file"}
@Return {value:"An error if an error occurs during the compression process"}
public native function compressToBlob(file:Path dirPath) returns blob|error;