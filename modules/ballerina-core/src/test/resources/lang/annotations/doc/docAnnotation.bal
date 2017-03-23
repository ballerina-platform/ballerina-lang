package lang.annotations.doc;

annotation Description attach service, function {
    string value = "Description of the service/function";
    int[] code;
    Param paramValue;
    QueryParam[] queryParamValue;
    QueryParam[] queryParamValue2;
    string[] paramValue2;
}

annotation Param attach service, function, connector {
    string value = "Description of the input param";
}

annotation QueryParam attach service {
    string name = "default name";
    string value = "default value";
}

annotation Doc attach service, function, connector {
    Description des;
}