function add (int x, int y) returns string {
    int addition = x + y;
    string result = "result is " + addition.toString();
    return result;
}

function checkAndAdd (int x, int y) returns (string|error) {
    if (x < 0 || y < 0) {
        error err = error("can't add negative values.");
        return (err);
    }
    int addition = x + y;
    string result = "result is " + addition.toString();
    return (result);
}

public function main(string... args){
    int i = 10;
    int j = 15;
    int k = -1;
    add(i , j);
    checkAndAdd(i, k);
}
