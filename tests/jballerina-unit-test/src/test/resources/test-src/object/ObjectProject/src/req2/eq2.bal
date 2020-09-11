
public class userPB {
    public int age = 0;
    public string name = "";
    public string address = "";

    function init () {}

    public function getName () returns (string) {
        return self.name;
    }

    public function getAge () returns (int) {
        return self.age;
    }
}
