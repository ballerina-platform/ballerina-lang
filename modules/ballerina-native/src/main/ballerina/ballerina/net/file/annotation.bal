package ballerina.net.file;

annotation FileSource attach service {
    string protocol;
    string fileURI;
    string pollingInterval;
    string seek;
}
annotation OnUpdate attach resource{

}
annotation OnRotate attach resource{

}