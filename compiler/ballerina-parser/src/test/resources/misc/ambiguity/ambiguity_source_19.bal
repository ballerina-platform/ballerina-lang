function foo() {
    { readonly a:b} -> w;
    
    { readonly "a":b} -> w;
    
    { readonly a = b;}
}
