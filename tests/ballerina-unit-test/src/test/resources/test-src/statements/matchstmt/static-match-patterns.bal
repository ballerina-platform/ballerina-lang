function testStaticMatchPatternsBasic() returns string[] {
    string | int | boolean a1 =  12;
    string | int | boolean a2 =  "Hello";
    string | int | boolean a3 =  true;

    string | int | boolean a4 =  15;
    string | int | boolean a5 =  "HelloAgain";
    string | int | boolean a6 =  false;

    string | int | boolean a7 =  "NothingToMatch";

    string[] result = [foo(a1), foo(a2), foo(a3), foo(a4), foo(a5), foo(a6), foo(a7)];

    return result;
}

function foo(string | int | boolean a) returns string {
    match a {
        12 => return "Value is '12'";
        "Hello" => return "Value is 'Hello'";
        15 => return "Value is '15'";
        true => return "Value is 'true'";
        false => return "Value is 'false'";
        "HelloAgain" => return "Value is 'HelloAgain'";
    }

    return "Value is 'Default'";
}
