function foo() {
    int x = xml `some ${"Hello, World! "} text
                 <ns0:foo> 
                    hello ${a[b][c]} world  ${5 + 3}${xml `<baz></baz>`}
                    <bar>
                        <city>
                            Colombo
                        </city>
                    </bar>
                 </ns0:foo> `;
}
