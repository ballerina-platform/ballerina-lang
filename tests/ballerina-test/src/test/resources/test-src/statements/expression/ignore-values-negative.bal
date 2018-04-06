function add (int x, int y) returns string {
    string result = "result is " + (x + y);
    return result;
}

function checkAndAdd (int x, int y) returns (string|error) {
    if (x < 0 || y < 0) {
        error err = {message:"can't add negative values."};
        return (err);
    }
    string result = "result is " + (x + y);
    return (result);
}

function main(string[] args){
    int i = 10;
    int j = 15;
    int k = -1;
    add(i , j);
    checkAndAdd(i, k);
}
