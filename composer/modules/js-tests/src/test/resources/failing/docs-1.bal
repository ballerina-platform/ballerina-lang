package ballerina.builtin;

public annotation Description {
    string value;
}

public annotation Param attach resource, function, connector, action, typemapper {
    string value;
}

public annotation Return attach function, action, typemapper {
    string value;
}

public annotation Field attach struct {
    string value;
}
