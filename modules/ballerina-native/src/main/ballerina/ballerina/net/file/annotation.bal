package ballerina.net.file;

annotation FileSource attach service {
    string protocol;
    string path;
    string pollingInterval;
    string startPosition;
    string maxLinesPerPoll;
}
annotation OnUpdate attach resource{

}
annotation OnRotate attach resource{

}