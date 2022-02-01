class Person {
    int id;
    string name;
    int age;

    function init(string name, int age, int rank) {
        self.name = name;
        self.age = age;
        self.rank = rank;
    }

    public function setId(int id) {
        self.id = id;
    }
}

public function main() {
    Person person1 = new Person("David", age = getRandomAge(), rank = 4);
}
