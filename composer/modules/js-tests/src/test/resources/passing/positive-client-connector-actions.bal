import ballerina.net.ftp;
import ballerina.file;

function createFile (string url, boolean createFolder) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File newDir = {path:url};
    c.createFile(newDir, createFolder);
}

function isExist (string url) (boolean) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File textFile = {path:url};
    return c.exists(textFile);
}

function readContent (string url) (string) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File textFile = {path:url};
    blob contentB = c.readFile(textFile);
    return contentB.toString("UTF-8");
}

function copyFiles (string source, string destination) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File txtFile = {path:source};
    file:File copyOfTxt = {path:destination};
    c.copyFile(txtFile, copyOfTxt);
}

function moveFile (string source, string destination) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File txtFile = {path:source};
    file:File copyOfTxt = {path:destination};
    c.moveFile(txtFile, copyOfTxt);
}

function write (string source, string content) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File wrt = {path:source};
    blob contentD = content.toBlob("UTF-8");
    c.write(contentD, wrt);
}

function fileDelete (string source) {
    endpoint<ftp:FTPClient> c { create ftp:FTPClient();}
    file:File del = {path:source};
    c.deleteFile(del);
}
