import ballerina/io;
import ballerina/file;
import ballerina/time;

function testAbsolutePath(string pathValue) returns (string){
    file:Path filePath = new(pathValue);
    file:Path absolutePath = filePath.toAbsolutePath();
    return absolutePath.getPathValue();
}

function testPathExistance(string pathValue) returns (boolean){
    file:Path filePath = new(pathValue);
    return file:exists(filePath);
}

function createDirectoryAndList(string pathValue) returns (string []){
    string parent = pathValue+"/parent";
    file:Path dirPath = new(parent);
    var result = file:createDirectory(dirPath);

    string dir = parent+"/child1";
    dirPath = new(dir);
    result = file:createDirectory(dirPath);

    dir = parent+"/child2";
    dirPath = new(dir);
    result = file:createDirectory(dirPath);

    dirPath = new(parent);
    io:println(dirPath.getPathValue());
    var pathArray =check file:list(dirPath);
    string [] pathValues = [];
    int pathIterations = 0;

    foreach path in pathArray{
        pathValues[pathIterations] =  path.getPathValue();
        pathIterations = pathIterations + 1;
    }

    return pathValues;
}

function testCreateFile(string pathValue) returns (string){
   file:Path dirPath = new(pathValue);
   file:Path filePath = new(pathValue + "test.txt");
   var result = file:createFile(filePath);

   string [] pathValues = [];
   var pathArray =check file:list(dirPath);
   int pathIterations = 0;

   foreach path in pathArray{
       pathValues[pathIterations] =  path.getPathValue();
       pathIterations = pathIterations + 1;
   }

   return pathValues[0];
}

function testGetFileName(string pathValue) returns (string){
    file:Path path = new (pathValue);
    return path.getName();
}

function testGetModifiedTime(string pathValue) returns (string){
    file:Path path = new(pathValue);
    time:Time modifiedTime =check file:getModifiedTime(path);
    return modifiedTime.toString();
}

function testWriteFile(string pathValue,string accessMode,byte[] content) returns (byte[]|io:IOError){
   file:Path filePath = new(pathValue);
   string absolutePath = filePath.toAbsolutePath().getPathValue();
   io:WritableByteChannel wbc =io:openWritableFile(absolutePath);
   var result = wbc.write(content,0);
   var closeResult = wbc.close();
   //Open the file again for reading
   io:ReadableByteChannel rbc =io:openReadableFile(absolutePath);
   var readResult = rbc.read(100);
   closeResult = rbc.close();
   match readResult {
       (blob,int) byteContent =>{
          var (bytes, numberOfBytes) = byteContent;
          return bytes;
       }
       io:IOError err =>{
          return err;
       }
   }
}

function testDirectoryExistance(string pathValue) returns (boolean){
    file:Path filePath = new(pathValue);
    return file:isDirectory(filePath);
}

function deleteDirectory(string pathValue){
    file:Path filePath = new(pathValue);
    var result = file:delete(filePath);
}
