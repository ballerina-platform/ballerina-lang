
public type userPB record {
    int age = 0;
    string name = "";
    string address = "";
};

public function <userPB ub> userPB() {
}

public function <userPB ub> getName () returns (string) {
    return ub.name;
}

public function <userPB ub> getAge () returns (int) {
    return ub.age;
}