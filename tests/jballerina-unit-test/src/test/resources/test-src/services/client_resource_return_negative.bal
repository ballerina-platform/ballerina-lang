type CustomType1 float|int[]|client object{}|int|string;
type CustomType2 float|int[]|(function (int a, string b) returns int|string)|int|string;

client object{} & readonly successClient = client object {

};

function testFunction(int a, string b) returns int|string {
    return 1;
}

public function testClientServiceAccessReturnClientObjectType() {
    var successClientObject = client object {
        resource function get clientReturnPath/[int a]/foo(string name) returns client object {} {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath/[int a]/foo(string name) returns int|client object {}|
                (function (int a, string b) returns int|string)|string|record{int a;} {
            return testFunction;
        }

        resource function get readonlyClientReturnPath/[int a]/foo(string name) returns client object {} & readonly {
            return successClient;
        }

        resource function get nullableClientReturnPath/[int a]/foo(string name) returns client object {}? {
            return successClient;
        }
        resource function get nullableClientUnionFunctionReturnPath/[int a]/foo(string name)
            returns (int|client object {} |
                (function (int a, string b) returns int|string)|xml)? {
            return testFunction;
        }

        resource function get nullableReadonlyClientReturnPath/[int a]/foo(string name)
            returns client object {}? & readonly {
            return successClient;
        }
    };
}

public function testClientServiceAccessReturnFunctionType() {
    var successClientObject = client object {
        resource function get functionReturnPath/[int a]/foo(string name) returns
                function (int a, string b) returns int|string {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath/[int a]/foo(string name) returns
                function (int a, string b) returns int|string & readonly {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath/[int a]/foo(string name) returns
                (function (int a, string b) returns int|string)? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath/[int a]/foo(string name) returns
                (function (int a, string b) returns int|string)? & readonly {
            return testFunction;
        }
    };
}

public function testClientServiceAccessReturnCustomClientType() {
    var successClientObject = client object {
        resource function get clientReturnPath/[int a]/foo(string name) returns CustomType1 {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath/[int a]/foo(string name)
                returns int|CustomType1|CustomType2 {
            return testFunction;
        }

        resource function get readonlyClientReturnPath/[int a]/foo(string name) returns CustomType1 & readonly {
            return successClient;
        }
    };
}

public function testClientServiceAccessReturnCustomFunctionType() {
    var successClientObject = client object {
        resource function get functionReturnPath/[int a]/foo(string name) returns CustomType2 {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath/[int a]/foo(string name) returns CustomType2 & readonly {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath/[int a]/foo(string name) returns CustomType2? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath/[int a]/foo(string name)
                returns CustomType2? & readonly {
            return testFunction;
        }
    };
}
