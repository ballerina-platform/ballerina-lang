function testIntermediateClauseCompletions() {
    string[] result = from int a in [1,2,3] let int b = a * a 
    from string d in ["one","two","three"] let string power = string `${d}:${b}` 
    where d.length() > 3 order by a 
    from xml 'xml in xml `xmlItem` 
    limit 1 
    select string `${b} ${d}`;
}
