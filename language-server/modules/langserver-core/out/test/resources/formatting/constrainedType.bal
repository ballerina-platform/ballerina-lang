   map<string?> values = {
    "key1": "value1",
    "key2": ()
};

function name1() {map< string > words = {
        a: "apple",
        b: "banana",
        c: "cherry"
    };
}

function name2(map < string > passed ) {
map< string > words = passed;
}

function name3() {
    name2({a: "apple", b: "banana",c:
    "cherry"});
}

function  tableReturnFunction ()   returns    (   table<any>  )   {
    return;
}

public function main() {
    string text = "Sample String";
    int number = 34;
    string anotherText = <       @untainted   string>    text   ;
    int anotherNumber = <   @untainted   int      >   number   ;
}

public function test1() {
    string text = "Sample String";
    int number = 34;
    string anotherText = <
@untainted     string
               >
  text   ;
           int anotherNumber =
             <
         @untainted
 int
         >
  number
       ;
}