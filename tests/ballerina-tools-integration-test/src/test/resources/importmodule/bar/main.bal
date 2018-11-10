import integrationtests/abc;

public function greeting() returns string {
    string name = "Hello" + abc:name() + "!!!! Have a good day!!!";
    return name;	
}
