public function main() {
    // Missing semicolon
    R a = {x: 3}
    R|error b = a;

    json j = from var r in reportStream
        select r.toJson()
    io:println(j.toJsonString());

    // Missing binary operator
    int a = 5 6;
}
