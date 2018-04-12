# Package overview

The `wso2/ftp` package provides an FTP client and an FTP server listener implementation to facilitate an FTP connection to a remote location. 

## FTP client
`ftp:Client` is used to connect to an FTP server and perform various operations on the files. It supports `get`, `delete`, `put`, `append`, `mkdir`, `rmdir`, `rename`, `size`, and `list` operations.

An FTP client endpoint is defined using these parameters: `protocol`, `host`, `username`, `passPhrase`, and optionally, `port`.   

## FTP listener
`ftp:Listener` is used to listen to a remote FTP location and trigger an event of type `FileEvent` when a new file is added to the directory. The `fileResource` function is invoked when a new file is added.

An FTP listener endpoint is defined using these parameters: `protocol`, `host`, `username`,  `passPhrase`, `path`, `pollingInterval`, `sftpIdentityPassPhrase`, and `sftpUserDirIsRoot`.

# Samples

**Sample FTP client endpoint**

```ballerina
endpoint ftp:Client client {
    protocol: "sftp",
    host:"ftp.ballerina.com",
    username : "john",
    passPhrase : "password"
};
```

**Sample FTP client operations**

All of the following operations return `FTPClientError` in case of an error. 

```ballerina
// Make a directory in the remote FTP location.
ftp:FTPClientError? dirCreErr client -> mkdir("/personal/files");  

//Add Put a file to the FTP location.
io:ByteChannel bchannel = io:openFile("/home/john/files/MyFile.xml", "r");
ftp:FTPClientError? filePutErr = client -> put("/personal/files/MyFile.xml", bchannel);

// List the files in the of FTP location.
var listOrError = client -> list("/personal/files");

// Rename or move a file in the FTP location.
ftp:FTPClientError? renameErr = client -> rename("/personal/files/MyFile.xml", "/personal/New.xml");

// Read the size of a file in the FTP location.
var sizeOrError = client -> size("/personal/New.xml");

// Download a file from the FTP location.
var byteChannelOrError = client -> get("/personal/New.xml");

// Delete a file in the FTP location.
ftp:FTPClientError? fileDelErr = client -> delete("/personal/New.xml");

// Delete a directory in the FTP location.
ftp:FTPClientError? dirDelErr = client -> rmdir("/personal/files");    
```

**Sample FTP listener endpoint**

```ballerina
endpoint ftp:Listener remoteFolder {
    protocol:"sftp",
    host:"ftp.ballerina.com",
    username : "john",
    passPhrase : “password"
    path:"/personal",
    pollingInterval:"2000",
    sftpIdentities:"/home/john/.ssh/id_rsa",
    sftpIdentityPassPhrase:"",
    sftpUserDirIsRoot:"true"
};
```

**Sample service for the FTP listener endpoint**

```ballerina
service myRemoteFiles bind remoteFolder {
    fileResource (ftp:FileEvent m) {
	log:printInfo(m.uri);
	log:printInfo(m.baseName);
        	log:printInfo(m.path);
    }
}
```
