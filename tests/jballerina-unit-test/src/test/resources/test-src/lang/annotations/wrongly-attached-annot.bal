import lang.annotations.doc1 as doc;

@doc:Description{queryParamValue: [@doc:QueryParam{
                    name:"paramName", 
                    value:"paramValue"}]
                }
@Bar{value:"bar value"}
function foo (string args) {
    // do nothing
}

annotation Bar attach service {
    string value;
}
