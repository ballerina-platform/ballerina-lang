type Result record {
 (int|error) a; 
 };

 public function main() {

    worker WA returns int|error {
        return 0;
    }
    
    future<int|error> a = start getInt(); 

    Result r = wait {};
}

function getInt() returns int {
    return 0;
}

