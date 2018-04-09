import ballerina/file;
import ballerina/io;
import ballerina/time;

function main (string[] args) {
    //Create 'File' struct and open for writing.
    file:File target = {path:"/tmp/result.txt"};
    target.open(file:W);

    //Close the file once done.
    target.close();

    //Check whether the file exists.
    boolean b = target.exists();
    io:println("file exists: " + b);

    //Here's how you can copy a file.
    file:File source = {path:"/tmp/result.txt"};
    file:File destination = {path:"/tmp/copy.txt"};
    file:copy(source, destination);
    io:println("file copied: /tmp/result.txt to /tmp/copy.txt");

    //How to delete a file.
    destination.delete();
    io:println("file deleted: /tmp/copy.txt");

    //Move source file to destination.
    destination = {path:"/tmp/move.txt"};
    file:move(source, destination);
    io:println("file moved: /tmp/result.txt to /tmp/move.txt");

    destination.delete();
    io:println("file deleted: /tmp/move.txt");

    //Create a directory, along with the parent directories.
    file:File dirs = {path:"/tmp/dir/abc/def"};
    _ = dirs.mkdirs();

    //Check if a file is a directory.
    file:File possibleDir = {path:"/tmp/dir/abc"};
    io:println("file is a directory: " + possibleDir.isDirectory());

    //Create new files inside a directory (ignoring all 3 possible return values).
    file:File newFile1 = {path:"/tmp/dir/abc/file1.txt"};
    _ = newFile1.createNewFile();

    file:File newFile2 = {path:"/tmp/dir/abc/file2.txt"};
    _ = newFile2.createNewFile();

    file:File newFile3 = {path:"/tmp/dir/abc/file3.txt"};
    _ = newFile3.createNewFile();

    //Get the list of files in a directory.
    var filesList = check possibleDir.list();

    //Print the list of files in directory "/tmp/dir/abc".
    int i=0;
    while (i < lengthof filesList) {
        io:println(filesList[i]);
        i = i + 1;
    }

    //Get file meta data.
    string name = newFile1.getName();
    time:Time lastModifiedTime = check newFile1.getModifiedTime();
    io:println(name + " modified at: " + lastModifiedTime.time);
    io:println(name + " is readable: " + newFile1.isReadable());
    io:println(name + " is writable: " + newFile1.isWritable());
}
