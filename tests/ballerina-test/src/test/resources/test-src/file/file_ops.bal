import ballerina/io;
import ballerina/file;

function testAbsolutePath(string pathValue) returns (string){
    file:Path filePath = file:getPath(pathValue);
    file:Path absolutePath =? filePath.toAbsolutePath();
    return absolutePath.getPathValue();
}
