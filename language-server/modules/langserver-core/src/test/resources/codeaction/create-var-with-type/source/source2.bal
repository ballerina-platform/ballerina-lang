type Rec1 record {|
    int id;
|};

type Rec2 record {|
    string msg;
|};

type Rec3 record {|
    boolean value;
|};

type Error1 distinct error;

type Error2 error<Rec2>;

type TargetType1 typedesc<Rec1|Rec2>;

type TargetType2 typedesc<Rec2|Rec3>;

type UnionType string|boolean;

client class MyClient {
    resource function get v1/values/[string id](TargetType1 targetType = <>)
        returns targetType = external;

    resource function get v2/values/[string id](typedesc<Rec1|string> targetType = <>)
        returns targetType|Error2 = external;

    resource function get v3/values/[string id](TargetType2 targetType = <>)
        returns targetType|error = external;

    resource function get v4/values/[string id](TargetType2 targetType = <>)
        returns targetType|Error1|Error2 = external;

    resource function get v5/values/[string id](typedesc<Rec1|Rec2|Rec3> targetType = <>)
        returns targetType|Error1 = external;

    resource function get v6/values/[string id](typedesc<Rec1|Rec2|Error1> targetType = <>)
        returns targetType = external;

    remote function delete(typedesc<Rec1|Rec2|Error1> targetType = <>)
        returns targetType = external;

    remote function put(Rec1 rec, TargetType2 targetType = <>)
        returns targetType|error = external;
}

public function main() returns error? {
    MyClient myCl = new;
    myCl->/v1/values/path();
    myCl->/v2/values/path();
    myCl->/v3/values/path();
    myCl->/v4/values/path();
    myCl->/v5/values/path();
    myCl->/v6/values/path();
    myCl->delete();
    myCl->put({id: 0});
}
