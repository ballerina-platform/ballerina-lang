public function (string a, int b) returns (string) glf1 = concat;

public function (string a, boolean b) returns (string) glf2 = (string a, boolean b) => (string) {
                                                               return a + b;
                                                           };

public function (string, int) returns (string) glf3 = (string a, int b) => (string) {
    return "llll";
};

function concat (string x, int y) returns (string) {
    string result = x + y;
    return result;
}