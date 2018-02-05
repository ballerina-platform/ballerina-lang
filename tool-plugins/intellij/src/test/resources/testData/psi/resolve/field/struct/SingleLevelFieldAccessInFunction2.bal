struct testStruct{
    string /*def*/s;
}

function test(){
    testStruct ts ={};
    ts./*ref*/s="";
}
