class Person {
    int id;
    string name;
    int age;

    public function setDetails(string name, int age, int rank) {
        self.name = name;
        self.age = age;
        self.rank = rank;
    }
}

public function main() {
    Person person1 = new Person();
    person1.setDetails("David", age = getRandomNumber(), rank = 4);
}
