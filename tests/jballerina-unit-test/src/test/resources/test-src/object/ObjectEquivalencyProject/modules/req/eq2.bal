
public class userPFoo {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";

    public function init (int age, string name, string address) {
        self.age = age;
        self.name = name;
        self.address = address;
    }

    public function getName () returns string {
        return self.name + ":userPFoo";
    }

    public function getAge () returns int {
        return self.age;
    }
}
