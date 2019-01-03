public type State "on"|"off";

public type NumberSet 1|2|3|4|5;

public type StringOrInt int|string;

public type Int int;

public type POrInt Person|int;

public type Person record {
   string name;
};

public type PreparedResult "ss"|"sss"|"qqq";

public type Channel object {

    public State? b;

    public function __init(State b = "off", boolean a = true){
        self.b = b;
        State o =  "on";
        if(b == o) {
           int i = 4;
        }
    }
};

public type CombinedState "on"|"off"|int;

public type TypeAliasOne Person;

public type TypeAliasTwo TypeAliasOne;

public type TypeAliasThree TypeAliasTwo;

public type MyType int|string;

public type ParamTest string|int;

public type ArrayCustom int[];

public type ByteType byte;

public type ByteArrayType byte[];

public type BFType byte|float;

public type BFuncType function (string) returns int;

public type BFuncType2 (function (string) returns int)|string;
