
public function main() {
    record {|string name; int[] marks; string id?;|} a = {
        name: "Bob",
        marks: [23, 99, 54]
    };
    
    a["id"].
}
