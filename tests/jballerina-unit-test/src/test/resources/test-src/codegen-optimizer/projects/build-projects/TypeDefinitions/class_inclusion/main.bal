
type Cloneable object {
    function clone() returns Cloneable;
};

type Person object {
    *Cloneable;
    string name;

    function getName() returns string;
};

class Engineer {
    *Person;

    function init(string name) {
        self.name = name;
    }

    function clone() returns Engineer {
        return new (self.name);
    }

    function getName() returns string {
        return self.name;
    }
}

public function main() {
    Engineer e1 = new ("Alice");
}
