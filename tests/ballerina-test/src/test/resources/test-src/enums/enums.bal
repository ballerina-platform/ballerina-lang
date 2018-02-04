
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

function testEnumToAnyCast() returns (int) {
    state st = state.INSTALLED;
    state st1 = state.ACTIVE;
    any a = st;
    any b = (any)st1;

    var ast, _ = (state) a;
    var ast1, _ = (state) b;

    if ( ast == ast1) {
        return 404;
    } else {
        return 201;
    }
}

function testEnumSameTypeCast() returns (int) {
    state st = state.INSTALLED;
    state st1 = (state)st;


    if ( st == st1) {
        return 404;
    } else {
        return 201;
    }
}
