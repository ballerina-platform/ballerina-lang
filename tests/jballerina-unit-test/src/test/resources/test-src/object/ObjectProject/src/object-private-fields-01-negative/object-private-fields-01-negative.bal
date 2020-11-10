import org.foo;

public class userA {
    public int age = 0;
    public string name = "";
}

public class userB {
    public int age = 0;
    public string name = "";
    public string address = "";

    string zipcode = "";
}

public function testRuntimeObjEqNegative() returns (string|error) {
    foo:user u = foo:newUser();

    // This is a safe assignment
    userA uA = u;
    any a = uA;
    // This is a unsafe cast
    userB uB = check trap <userB> a;
    return uB.zipcode;
}
