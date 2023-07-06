import ballerina/module1;

function getListener() returns module1:Listener {
     return new module1:Listener(9090);
}

function getInt() returns int {
     return 123
}
// Test whether the type-cast expression completions can be fulfilled
public listener module1:Listener listener1 = new(9090);
public listener module1:Listener listener2 = <>
