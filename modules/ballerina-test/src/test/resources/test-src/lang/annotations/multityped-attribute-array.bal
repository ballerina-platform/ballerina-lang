import lang.annotations.doc;

@doc:Description{queryParamValue: [@doc:QueryParam{
                    name:"paramName", 
                    value:"paramValue"}, "hello"]
                }
function foo (string args) {
    // do nothing
}
