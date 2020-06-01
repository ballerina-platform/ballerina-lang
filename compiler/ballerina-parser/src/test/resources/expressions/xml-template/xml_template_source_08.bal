function foo() {
    int x = xml `<foo> 
                    ${ xml `<baz</baz>` }
                 </foo> `;
}
