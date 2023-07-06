public function main() {

    worker WA returns int|error {
        return 0;
    }
  
    worker WB returns string|error {
        return "";
    }
    
    future<int> fint = start getInt();
    future<string> fstr = start getString(); 

    map<string|error> myVar = wait {fieldName: };
}

function getString() returns string {
    return "";
}

function getInt() returns int {
    return 0;
}
