package ballerina.doc;

annotation Description {
    string value;
}

annotation Param attach resource, function, connector, action {
    string value;
}

annotation Return attach function, action {
    string value;
}

annotation Field attach struct {
    string value;
}
