type testRecord record {
    string /*def*/s;
}

function test(){
    testRecord ts ={};
    ts./*ref*/s="";
}
