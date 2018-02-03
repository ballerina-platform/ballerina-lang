@Description {value:"Global variable declaration."}
int total = 98;

string content = "";

function main (string[] args) {

    //Accessing a global variable.
    println(total);

    content = content + "This is a sample text\n";
    println(content);
}
