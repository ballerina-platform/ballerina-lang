package ballerina.compression;

@Description {value:"Function to decompress compressed bytes"}
@Param {value:"content: Byte array of the compressed file"}
@Param {value:"destDir: Destination directory to place the decompressed file"}
@Param {value:"folderToUnzip: Folder to unzip from the compressed bytes"}
public native function unzipBytes (blob content, string destDir, string folderToUnzip);

@Description {value:"Function to decompress a zipped file"}
@Param {value:"content: Path of the zipped file"}
@Param {value:"destDir: Destination directory to place the decompressed file"}
@Param {value:"folderToUnzip: Folder to unzip from the compressed bytes"}
public native function unzipFile (string dirPath, string destDir, string folderToUnzip);

@Description {value:"Function to compress a folder"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Param {value:"destDir: Destination directory to place the compressed file"}
public native function zipFile (string dirPath, string destDir);

@Description {value:"Function to compress bytes"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Return {value:"Compressed bytes of the file"}
public native function zipToBytes (string dirPath) returns (blob);
