function foo() {
    string country = "Sri Lanka" + "test";
    string codeLiteral = "code";
    map  <  string  >   addrMap   =   {
           line1  :    "No. 20"       ,
       city   :   "Colombo 03"  ,
           country  ,
               [   codeLiteral   ]  :   "00300"
       };
    string  ?  countryValue  =  addrMap  [ "country"  ] ;
           addrMap  [ "postalCode"   ]   =   "00300"   ;
}
