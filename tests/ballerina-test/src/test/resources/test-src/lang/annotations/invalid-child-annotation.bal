import lang.annotations.doc1 as doc;

@doc:Description{args: @Args{}}
@Args{value:"args: input parameter"}
function foo (@Args{} string args) {
    // do nothing
}

annotation Args {
    string value;
}