import lang.annotations.doc1 as doc;

@doc:Description{paramValue:@doc:Param{},
                 queryParamValue2:[@doc:QueryParam{}],
                 code:[1,2,5],
                 args: @doc:Args{}}
@Args{}
@Bar{}
@Status{}
function foo (@Args{value:"args: input parameter : type string"} string args) {
    // do nothing
}

annotation Args {
    string value = "default value for local 'Args' annotation";
}

annotation Bar {
    int a;
}

annotation Status {
    int status = 200;
}