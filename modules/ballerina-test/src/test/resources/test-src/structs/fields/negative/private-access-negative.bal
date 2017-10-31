package negative;

import foo;

function test1 () {
    foo:TestStruct test = {aInt:10, aChild:{}};
    foo:TestAnonymousStruct anon = {publicStruct:{cValue:"d", bValue:"e"}, privateStruct:{}};
}
