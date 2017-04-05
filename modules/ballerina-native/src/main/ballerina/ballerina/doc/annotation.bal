package ballerina.doc;

annotation Description {
    string value;
}

annotation Param attach resource, function, connector, action, typemapper {
    string value;
}

annotation Return attach function, action, typemapper {
    string value;
}

annotation Field attach struct {
    string value;
}