type Result record {
 (int|error) WA; 
 };

 public function main() {

    worker WA returns int|error {
        return 0;
    }
    
    future<int> fint = start getInt(); 

    Result r = wait {};
}

function getInt() returns int {
    return 0;
}
