Person? p1 = new;
Person? p2 = new ();

function test() {
    Person? p3 = new;
    Person? p4 = new();
    Person? p5;
    p5 = new;
    Person? p6;
    p6 = new ();
}


type Person object {
    public {
        int age,
    }
};

type Employee object {
    public {
        Person? p1 = new,
        Person? p2 = new (),
        Person? p3,
        Person? p4,
    }
    new () {
        p3 = new;
        p4 = new();
    }
};







