import ballerina.net.ftp;

service<ftp> ftpServerConnector {
    resource fileResource (ftp:FTPServerEvent m) {
        println(m.name);
    }
}
