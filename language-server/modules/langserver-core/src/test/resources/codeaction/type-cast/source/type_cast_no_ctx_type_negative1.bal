type Person record {
   string? id;
   int? age;
   string? name;
};

type Student record {
   string id;
};

public function add(Person p) {
   p.id + "10"
}
