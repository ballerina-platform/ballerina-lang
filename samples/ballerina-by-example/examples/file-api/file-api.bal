import ballerina.file;
import ballerina.lang.time;

function main (string[] args) {
    //Create 'File' struct and open for writing.
    file:File target = {path:"/tmp/result.txt"};
    target.open(file:W);

    //Close the file once done.
    target.close();

    //Check whether the file exists.
    boolean b = target.exists();
    println("file exists: " + b);

    //Here's how you can copy a file.
    file:File source = {path:"/tmp/result.txt"};
    file:File destination = {path:"/tmp/copy.txt"};
    file:copy(source, destination);
    println("file copied: /tmp/result.txt to /tmp/copy.txt");

    //How to delete a file.
    destination.delete();
    println("file deleted: /tmp/copy.txt");

    //Move source file to destination.
    destination = {path:"/tmp/move.txt"};
    file:move(source, destination);
    println("file moved: /tmp/result.txt to /tmp/move.txt");

    destination.delete();
    println("file deleted: /tmp/move.txt");

    //Create a directory, along with the parent directories
    file:File dirs = {path:"/tmp/dir/abc/def"};
    var dirCreated, _, _ = dirs.mkdirs();

    //Check if a file is a directory
    file:File possibleDir = {path:"/tmp/dir/abc"};
    println("file is a directory: " + possibleDir.isDirectory());

    //Create new files inside a directory (ignoring all 3 possible return values)
    file:File newFile1 = {path:"/tmp/dir/abc/file1.txt"};
    _,_,_ = newFile1.createNewFile();

    file:File newFile2 = {path:"/tmp/dir/abc/file2.txt"};
    _,_,_ = newFile2.createNewFile();

    file:File newFile3 = {path:"/tmp/dir/abc/file3.txt"};
    _,_,_ = newFile3.createNewFile();

    //Get the list of files in a directory
    var filesList, _, _ = possibleDir.list();

    //Print the list of files in directory "/tmp/dir/abc"
    int i=0;
    while (i < lengthof filesList) {
        println(filesList[i]);
        i = i + 1;
    }

    //Get file meta data
    string name = newFile1.getName();
    time:Time lastModifiedTime;
    lastModifiedTime, _, _ = newFile1.getModifiedTime();
    println(name + " modified at: " + lastModifiedTime.time);
    println(name + " is readable: " + newFile1.isReadable());
    println(name + " is writable: " + newFile1.isWritable());
}
