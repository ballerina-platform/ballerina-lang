
import org.foo;

public struct userA {
    int age;
    string name;
}

public struct userB {
    int age;
    string name;
    string address;
    private:
        string zipcode;
}

public function testRuntimeStructEqNegative() returns (string) {
    foo:user u = foo:newUser();

    // This is a safe cast
    var uA = (userA) u;

    // This is a unsafe cast
    var uB, err = (userB) uA;
    if (err != null) {
        return err.message;
    }
    return uB.zipcode;
}
