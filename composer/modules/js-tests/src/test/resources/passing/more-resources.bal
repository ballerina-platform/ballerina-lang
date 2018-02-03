import ballerina.net.ftp;

@ftp:configuration {
    dirURI:"ftp://baluser@localhost/folder",
    pollingInterval:"2000",
    actionAfterProcess:"NONE",
    parallel:"false",
    createMoveDir:"true"
}
service<ftp> ftpServerConnector {
    resource fileResource (ftp:FTPServerEvent m) {
        println(m.name);
    }
    resource fileResource2 (ftp:FTPServerEvent m) {
        println(m.name);
    }
    resource fileResource3 (ftp:FTPServerEvent m) {
        println(m.name);
    }
}
