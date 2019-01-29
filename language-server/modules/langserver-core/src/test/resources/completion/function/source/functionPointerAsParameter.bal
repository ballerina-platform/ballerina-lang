function foo(int x, function (int, string) returns (float) bar) 
             returns (float) {
    
    return x * bar(10, "2");
}