public function main(string... args) {
    int[] arr = [];

    arr.forEach(function(int i) {
        userDefinedSecureOperation(foo("bar"));
    });

    return;
}

function userDefinedSecureOperation(@untainted string secureParameter) {

}


function foo(string s) returns @tainted string {
    return s;
}
