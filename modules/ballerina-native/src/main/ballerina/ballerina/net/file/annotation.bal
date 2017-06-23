package ballerina.net.file;

annotation FileSource attach service {
    string fileURI;
    string pollingInterval;
    string acknowledgementTimeOut;
    string deleteIfNotAcknowledged;
    string fileSortAttribute;
    string fileSortAscending;
}