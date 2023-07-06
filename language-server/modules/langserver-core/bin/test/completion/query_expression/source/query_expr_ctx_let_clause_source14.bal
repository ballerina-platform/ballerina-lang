import ballerina/module1;

type Person record {
 string first;
 string last;
};

public function main() {
    Person[] persons = [
        {first: "Melina", last: "Kodel"}
    ];

    string[] names = from var {first, last} in persons
                     let int len1 = first.length() 
}
