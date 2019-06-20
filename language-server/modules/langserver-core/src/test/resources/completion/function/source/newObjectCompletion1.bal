import ballerina/http;

function testFunction() {
    ObjectName res = 
}


type RecordName record {
    
};

type ObjectName object {
    public function __init(int arg1, string arg2, http:Response arg3) {
        
    }
};

function get() returns ObjectName {
    ObjectName o = new("arg");
    return o;
}