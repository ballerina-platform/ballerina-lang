public class Teenager {
    # + name - this is name
    string name;
    # This is age
    int age;

    // An `init` method may return an error.
    public function init(string name, int age) returns error? {
        if age > 18 {
            return error(string `${name} is not a teenager!`);
        }
        self.name = name;
        self.age = age;
    }

    public function getName() returns string {
        return self.name;
    }
}

public type Row record {
    // Both the field and its value are immutable.
    readonly string k;
    int value;

};

public const RANDOM = "RANDOM";
public int val = -1;

public enum Color {
    RED,
    GREEN,
    BLUE
}

public type Bar object {

};

public type InvalidIdError error<InvalidIdDetail>;

public function getFields(map<json> rec) returns [string[], string[]] {
    string[] fields = [];
    foreach var recordField in rec {
        fields[fields.length()] = recordField.toString();
    }
    return [rec.keys(), fields];
}

type InvalidIdDetail record {|
    error cause?;
    string id;
|};
