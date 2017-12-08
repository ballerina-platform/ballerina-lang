package ballerina.builtin;

public annotation Description {
    string value;
}

public annotation Param attach resource, function, connector, action {
    string value;
}

public annotation Return attach function, action {
    string value;
}

public annotation Field attach annotation, enum, struct {
    string value;
}
