import ballerina/file;
import ballerina/filepath;
import ballerina/io;

public function main() {

    // Get the current directory path.
    io:println("Current directory: " + file:getCurrentDirectory());  // e.g. “/home/john/work”

    // Create a new directory.
    string|error createDirResults = file:createDir("foo");
    if (createDirResults is string) {
        io:println("Created directory path: " + createDirResults);
    }

    // Create a new directory with any non existence parents.
    string dirPath = checkpanic filepath:build("foo", "bar");
    createDirResults = file:createDir(dirPath, true);
    if (createDirResults is string) {
        io:println("Created nested directory path: " + createDirResults);
    }

    // Create a file in given file path.
    string|error createFileResults = file:createFile("bar.txt");
    if (createFileResults is string) {
        io:println("Created file path: " + createFileResults);
    }

    // Get metadata information of the file.
    file:FileInfo|error fileInfoResults = file:getFileInfo("bar.txt");
    if (fileInfoResults is file:FileInfo) {
        io:println("File name: " + fileInfoResults.getName());
        io:println("File size: " + fileInfoResults.getSize().toString());
        io:println("Is directory: " + fileInfoResults.isDir().toString());
        io:println("Modified at " + fileInfoResults.getLastModifiedTime().toString());

    }

    // Check whether file or directory of the provided path exists.
    boolean fileExists = file:exists("bar.txt");
    io:println("bar.txt file exists: " + fileExists.toString());

    // Copy file or directory to new path.
    string filePath = checkpanic filepath:build("foo", "bar", "bar.txt");
    error? copyDirResults = file:copy("bar.txt", filePath, true);
    if (copyDirResults is ()) {
        io:println("bar.txt file is copied to new path " + filePath);
    }

    // Rename(Move) file or directory to new path.
    string newFilePath = checkpanic filepath:build("foo", "bar1.txt");
    error? renameResults = file:rename("bar.txt", newFilePath);
    if (renameResults is ()) {
        io:println("bar.txt file is moved to new path " + newFilePath);
    }

    // Get default directory use for temporary files.
    string tempDirPath = file:tempDir();
    io:println("Temporary directory: " + tempDirPath);

    // Get list of files in the directory.
    file:FileInfo[]|error readDirResults = file:readDir("foo");

    // Remove file or directory in specified file path.
    error? removeResults = file:remove(newFilePath);
    if (removeResults is ()) {
        io:println("Remove file at " + newFilePath);
    }

    // Remove directory in specified file path with all children.
    removeResults = file:remove("foo", true);
    if (removeResults is ()) {
        io:println("Remove foo directory with all child elements.");
    }
}
