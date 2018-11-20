Person? p1 = new;
Person? p2 = new ();

function test() {
    Person? p3 = new;
    Person? p4 = new();
    Person? p5 = ();
    p5 = new;
    Person? p6 = ();
    p6 = new ();
}


type Person object {

    public int age = 0;

};

type Employee object {

    public Person? p3 = new;
    public Person? p4 = new ();
    public Person? p5 = ();
    public Person? p6 = ();

    new () {
        self.p5 = new;
        self.p6 = new();
    }
};







