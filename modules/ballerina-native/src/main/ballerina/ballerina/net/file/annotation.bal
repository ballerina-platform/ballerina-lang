package ballerina.net.file;

annotation FileSource attach service {
    string protocol;
    string fileURI;
    string pollingInterval;
    string seek;
    string maxLinesPerPoll;
}
annotation OnUpdate attach resource{

}
annotation OnRotate attach resource{

}