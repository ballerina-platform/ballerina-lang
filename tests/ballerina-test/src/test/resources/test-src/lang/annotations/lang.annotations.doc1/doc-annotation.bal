package lang.annotations.doc1;

@Description{
    value:"Self annotating an annotation",
    paramValue:{
        value:"some parameter value"
    },
    queryParamValue:[{
        name:"first query param name",
        value:"first query param value"
    }],
    queryParamValue2:[{}],
    code:[7,8,9],
    args: {}
}
public annotation <service, resource, function, streamlet, struct, annotation, enum, parameter, transformer, endpoint> Description Desc;

struct Desc {
    string value = "Description of the service/function";
    int[] code;
    Param paramValue;
    QueryParam[] queryParamValue;
    QueryParam[] queryParamValue2;
    string[] paramValue2;
    Args args;
}

public struct Param {
    string value = "Description of the input param";
}

public struct QueryParam {
    string name = "default name";
    string value = "default value";
}

public struct Doc {
    Desc des;
}

public struct Args {
    string value = "default value for 'Args' annotation in doc package";
}