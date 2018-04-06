
import org.test;

type state "ACTIVE" | "RESOLVED" | "INSTALLED" | "ABORTED";

public state activeState = "ACTIVE" ;
public state resolvedState = "RESOLVED" ;
public state installedState = "INSTALLED";
public state abortedState = "ABORTED" ;

function testBasicEnumSupport() returns (int) {
    boolean result = testEnumAsParameter(activeState);
    if(result) {
        return 14;
    }
    return 15;
}

function testEnumAsParameter(state st) returns (boolean) {

    if ( st == activeState) {
        return true;
    } else {
        return false;
    }
}

type person {
        string name;
        int age;
        state st;
}

function testEnumInStruct() returns (int) {
    person p = {name:"test", st: resolvedState};
    if( p.st == activeState) {
        return 10;
    }

    return 12;
}


function testEnumInAnotherPackage() returns (int) {

    test:kind k = test:getEnumeratorInPackage();
    if (k == test:plus) {
        return 100;
    }

    return 200;

}

//function testEnumToAnyCast() returns (int) {
//    state st = installedState;
//    state st1 = activeState;
//    any a = st;
//    any b = st1;
//
//    var ast = a;
//    var ast1 = b;
//
//    if ( ast == ast1) {
//        return 404;
//    } else {
//        return 201;
//    }
//}

function testEnumSameTypeCast() returns (int) {
    state st = installedState;
    state st1 = st;


    if ( st == st1) {
        return 404;
    } else {
        return 201;
    }
}
