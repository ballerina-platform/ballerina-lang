public function foo() {json j1 = "Apple";json j2 = 5.36;
   json j3=true;
   json    j5    =   null  ;
   json j6 = {  name  :     "apple",   color  : "red",   price   : j2  };

   json   j8   =checkpanic   j5.mergeJson(j7)  ;
   json j9 = {
       name:          "Anne",       age: null, marks: {   math: 90, language: 95}  }  ;
   json j10 = {  name: (  ), age:   20, marks: {
       physics: 85     }};json   j11   =  checkpanic   j9.mergeJson(j10)  ;
   json   |    error   j12=   j2.mergeJson(j3);
   string s = j6.toJsonString();json j13   =
   checkpanic s.fromJsonString();
}
