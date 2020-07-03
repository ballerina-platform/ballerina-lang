import ballerina/http;

function testFunction() {
    ObjectName res = 
}


type RecordName record {
    
};

type ObjectName object {
    public function init(int arg1, string arg2, http:Response arg3) {
        
    }
};

function get() returns ObjectName {
    http:Response res = new();
    ObjectName o = new(1, "hello", res);
    return o;
}