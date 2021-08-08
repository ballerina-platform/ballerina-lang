function foo () {
    match bar {
        { => {}
        {a:b => {}
        {a:b, c:d}} => {}
        {a b, c d}} => {}
        {a:b, c:d}&} => {}
        {,,a:b, c:d, ... var e} => {}
        {a:[] c:{} ... var e} => {}
        {... var e, a:[], c:{}} => {}
        ... var e, a:[], c:{}} => {}
        {a:_, c:{fieldName1:_, fieldName2:[}], ... var e] => {}
        {a: var, c:{fieldName1:_, fieldName2:{} => {}
        {field1:true, nillField:(), constField:, signField:++5, stringField:::"dulmina"}=> {}
    }
}
