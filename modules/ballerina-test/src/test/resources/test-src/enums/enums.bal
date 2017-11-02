
import org.test;

enum state {
    INSTALLED,
    RESOLVED,
    UNINSTALLED,
    STARTING,
    ACTIVE,
    STOPPING
}

function testBasicEnumSupport() returns (int) {
    boolean result = testEnumAsParameter(state.ACTIVE);
    if(result) {
        return 14;
    }
    return 15;
}

function testEnumAsParameter(state st) returns (boolean) {

    if ( st == state.ACTIVE) {
        return true;
    } else {
        return false;
    }
}

struct person {
    string name;
    int age;
    state st;

}

function testEnumInStruct() returns (int) {

    person p = {name:"test", st: state.RESOLVED};
    if( p.st == state.ACTIVE) {
        return 10;
    }

    return 12;
}


function testEnumInAnotherPackage() returns (int) {

    test:kind k = test:getEnumeratorInPackage();
    if (k == test:kind.PLUS) {
        return 100;
    }

    return 200;
    
}
