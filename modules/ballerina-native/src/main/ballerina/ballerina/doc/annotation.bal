package ballerina.doc;

annotation Description attach function, struct, connector, action {
    string value;
}

annotation Param attach function, connector, action {
    string value;
}

annotation Return attach function, action {
    string value;
}

annotation Field attach struct {
    string value;
}