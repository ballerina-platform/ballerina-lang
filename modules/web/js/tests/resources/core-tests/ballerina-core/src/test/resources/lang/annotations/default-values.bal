import lang.annotations.doc;

@doc:Description{
                 args: @doc:Args{},
                 queryParamValue2:[@doc:QueryParam{}],
                 code:[1,2,5],paramValue:@doc:Param{}}
@Args{}
@Bar{}
@Status{}
function foo ( @Args{value:"args: input parameter : type string"}  string args) {
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