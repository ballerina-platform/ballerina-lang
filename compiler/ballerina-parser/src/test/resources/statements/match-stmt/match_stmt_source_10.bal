function foo () {
    match bar {
        {} => {}
        {a:b} => {}
        {a:b, c:d} => {}
        {a:b, c:d, ... var e} => {}
        {a:[], c:{}, ... var e} => {}
        {a:_, c:{fieldName1:_, fieldName2:{}}, ... var e} => {}
        {a: var a, c:{fieldName1:_, fieldName2:{}}} => {}
        {field1:true, nillField:(), constField:a, signField:+5, stringField:"dulmina"}=> {}
    }
}
