import ballerina.file;

function main (string[] args) {
    //Create 'File' struct.
    string path = "/tmp/result.txt";
    file:File target = {path:path};

    //Check whether the file exists.
    boolean fileExists = target.exists();
    println(path + " exists: " + fileExists);

    //Creating the file if it does not exist. All 3 potential errors are ignored.
    if (!fileExists) {
        var created, _, _ = target.createNewFile();

        if (created) {
            println("file successfully created: '" + path + "'");
        }
    }

    //Here's how you can copy a file. Copying can result in 3 types of errors which are ignored here.
    //The boolean parameter instructs not to replace if the file already exists.
    file:File source = {path:"/tmp/result.txt"};
    file:File destination = {path:"/tmp/copy.txt"};
    _, _, _ = file:copy(source, destination, false);

    if (destination.exists()) {
        println("successfully copied '/tmp/result.txt' to '/tmp/copy.txt'");
    }

    //How to delete a file.
    _, _ = file:delete(destination);
    if (!destination.exists()) {
        println("file deleted: '/tmp/copy.txt'");
    }

    //Move source file to destination.
    //As with copy(), here, the boolean parameter instructs not to replace if the file already exists.
    destination = {path:"/tmp/move.txt"};
    _, _, _ = file:move(source, destination, false);

    if (!source.exists() && destination.exists()) {
        println("file successfully moved from '/tmp/result.txt' to '/tmp/move.txt'");
    }

    _, _ = file:delete(destination);

    //Create a directory, along with the parent directories.
    file:File dirs = {path:"/tmp/dir/abc/def"};
    var dirCreated, _, _ = dirs.mkdirs();

    //Check if a file is a directory.
    file:File possibleDir = {path:"/tmp/dir/abc"};
    println("'/tmp/dir/abc' is a directory: " + possibleDir.isDirectory());

    //Create new files inside a directory (ignoring all 3 possible return values).
    file:File newFile1 = {path:"/tmp/dir/abc/file1.txt"};
    _, _, _ = newFile1.createNewFile();

    file:File newFile2 = {path:"/tmp/dir/abc/file2.txt"};
    _, _, _ = newFile2.createNewFile();

    file:File newFile3 = {path:"/tmp/dir/abc/file3.txt"};
    _, _, _ = newFile3.createNewFile();

    //Get the list of files in a directory.
    var filesList, _, _ = possibleDir.list();

    //Print the list of files in directory "/tmp/dir/abc".
    int i = 0;
    while (i < lengthof filesList) {
        println(filesList[i]);
        i = i + 1;
    }

    //Get file meta data.
    string name = newFile1.getName();
    Time lastModifiedTime;
    lastModifiedTime, _, _ = newFile1.getModifiedTime();
    println(name + " modified at: " + lastModifiedTime.time);
    println(name + " is readable: " + newFile1.isReadable());
    println(name + " is writable: " + newFile1.isWritable());
}
