package ballerina.lang.doc;

annotation Description attach function, struct, connector, action, typemapper {
    string value;
}

annotation Param attach function, connector, action, typemapper {
    string value;
}

annotation Return attach function, action, typemapper {
    string value;
}

annotation Field attach struct {
    string value;
}