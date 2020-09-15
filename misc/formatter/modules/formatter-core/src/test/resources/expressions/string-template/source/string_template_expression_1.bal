public function foo() {
   string name = "Ballerina";
   string   template   =  string   `Hello ${  name  }!!!`  ;
   string s1 = "foo";
   string s2 = "s";
   string s3 = string
   `${  string   `hello ${   string    `${ s1}${  s2}`
   } world`
   }`
   ;
}
