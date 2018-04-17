## Package Overview
This package compresses and decompresses files and folders. The content is encoded when the file or folder is compressed and a .zip file is created. Similarly, when decompressing, the content of the zipped file or folder is decoded.
### Perform compressing operations  
This package provides functions to compress files and directories. Th output of the compressed folders and files can be a . zip file or a`blob`. A `blob` is a primitive type in `ballerina` and it represents a sequence of bytes.

A compressed file is created by removing the redundant data, such as the space character, or frequently used characters, from the files during compression.
### Perform decompressing operations
Retrieve a decompressed  file or a folder by decoding the .zip file or `blob` using the functions of this package.
## Samples
### Compress folders or files
The sample given  below compresses a folder or file.

```ballerina
function compressFile(string srcFolderPath, string destFilePath) returns (error) {
   // Create a Path object using the ballerina/file package.
   // The path to the folder or file that needs to be compressed.
   file:Path srcPath = new(srcFolderPath);
   // The path where the compressed .zip file or blob needs to be created.
   file:Path destPath = new(destFilePath);
   // Compress the folder/file that is in the src path and create a compressed file in destPath path.
    var result = compression:compress(srcPath, destPath);
    match result {
        compression:CompressionError err => return err;
        ()=> return;
    }
}
```


### Compress and decompress a `blob`
The sample given below compresses a folder to a `blob` and decompresses the `blob`.

```ballerina
function compressDirToBlob(string src) returns (blob|error) {
   file:Path srcPath = new(src);
   // Compress the folder/file that is in the srcPath to a blob.
   var result = compression:compressToBlob(srcPath);
   // The compressToBlob function can return an  error or blob.
   match result {
	  // If the result is an error, return the error.
       compression:CompressionError err => return err;
     	 // If the result is a blob, decompress it.
       blob b => return decompressBlob(b, "/home/User/Ballerina/testDecompress");
   }
}

function decompressBlob(blob blobContent, string destDir) returns (error) {
   file:Path dstPath = new(destDir);
  
  // Decompress the ‘blobContent’ to the ‘dstPath’.
   var result = compression:decompressFromBlob(blobContent, dstPath);
   match result {
      compression:CompressionError err => return err;
      ()=> return;
   }
}
```

### Decompress a folder
The sample given below decompresses a folder.

The `dstPath` parameter that is passed to  the `decompress()` function and `decompressFromBlob()` function needs to point to a valid folder path. Else, the functions fail and the  `CompressionError` is returned.

```ballerina
function decompressFile(string src, string destDir) returns (error) {
   file:Path srcPath = new(src);
   file:Path dstPath = new(destDir);
   // Decompresses the folder that is denoted by the ‘srcPath’ path to the ‘dstPath’.
   var result = compression:decompress(srcPath, dstPath);
   // If an error is thrown, return the error.
   match result {
        compression:CompressionError err => return err;
        ()=> return;
   }
}
