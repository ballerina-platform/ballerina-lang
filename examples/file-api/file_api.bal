import ballerina/file;
import ballerina/io;
import ballerina/time;

function main(string... args) {
    string srcDirPath = "./tmp/src";
    string dstDirPath = "./tmp/dst";
    string rootDirPath = "./tmp";
    string fileLocation = "./tmp/src/test.txt";

    //This creates a directory.
    file:Path srcDir = new(srcDirPath);
    file:Path dstDir = new(dstDirPath);
    var result = file:createDirectory(srcDir);
    result = file:createDirectory(dstDir);

    //This creates a file.
    file:Path filePath = new(fileLocation);
    result = file:createFile(filePath);

    //This checks whether the file exists.
    boolean fileExist = file:exists(filePath);
    if (fileExist){
        //This fetches the absolute path of the file.
        string absolutePath = filePath.toAbsolutePath().getPathValue();
        io:println("File exists in " + absolutePath);
    }

    //This lists out the content of the directory.
    var pathArray = check file:list(new file:Path(rootDirPath));

    io:println("Iterating through directory content");

    //This iterates through the elements.
    foreach path in pathArray{
        io:println(path.getPathValue());
    }

    //This prints the meta data of the file.
    string fileName = filePath.getName();
    time:Time modifiedTime = check file:getModifiedTime(filePath);
    io:println("File Details, " + fileName + " " + modifiedTime.toString());

    //This deletes the folder.
    file:Path deleteFolderPath = new(rootDirPath);
    result = file:delete(deleteFolderPath);

    if (!file:exists(deleteFolderPath)){
        io:println("Folder deleted successfully");
    }
}
