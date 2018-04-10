import ballerina/io;
import ballerina/file;

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

function testWriteFile(string pathValue,string accessMode,blob content) returns (blob|io:IOError){
   file:Path filePath = new(pathValue);
   string absolutePath = filePath.toAbsolutePath().getPathValue();
   io:ByteChannel channel =io:openFile(absolutePath,accessMode);
   var result = channel.write(content,0);
   var closeResult = channel.close();
   //Open the file again for reading
   channel =io:openFile(absolutePath,"r");
   var readResult = channel.read(100);
   closeResult = channel.close();
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
