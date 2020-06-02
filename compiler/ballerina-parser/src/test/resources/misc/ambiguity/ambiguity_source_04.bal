function foo() {
    {} = x;
    {a, b:c} = x;
    {...b} = x;
    {a, b:{p, q:r, ...s}, ...c} = x;
    
    {} -> x;
    {a, b:c} -> x;
    {...b} -> x;
    {a, b:{p, q:r, ...s}, ...c} -> x;
}
