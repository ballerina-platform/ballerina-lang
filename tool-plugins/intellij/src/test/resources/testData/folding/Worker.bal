import <fold text='...'>ballerina/io;</fold>

public function main() <fold text='{...}'>{
    worker w1 <fold text='{...}'>{
        io:println("Hello, World! #m");
    }</fold>

    worker w2 <fold text='{...}'>{
        io:println("Hello, World! #n");
    }</fold>

    worker w3 <fold text='{...}'>{
        io:println("Hello, World! #k");
    }</fold>
}</fold>
