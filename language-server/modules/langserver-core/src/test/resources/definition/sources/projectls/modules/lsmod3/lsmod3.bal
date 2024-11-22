import ballerina/module1;
import projectls.lsmod1;

public function mod3Function1() {
}

public type MyType lsmod1:MyType;
public type MyType2 MyType;

public type Human record {|
    readonly int id;
    string name;
|};
