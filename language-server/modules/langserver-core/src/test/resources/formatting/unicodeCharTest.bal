import ballerina/io;

map<string> defaultChars = { "top": "═" ,  "top-mid": "╤" ,  "top-left": "╔" ,  "top-right": "╗"
, "bottom": "═" , "bottom-mid": "╧" , "bottom-left": "╚" ,  "bottom-right": "╝"
        ,  "left":  "║" ,  "left-mid": "╟" , "mid": "─" ,  "mid-mid": "┼"
                ,  "right":  "║" ,  "right-mid": "╢" ,  "middle": "│" };

public function main(string... args) {
    io:println("╢");
    io:println(defaultChars["left-mid"]);
}
