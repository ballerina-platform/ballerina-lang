type TestRecord record {
    int id = 0;
    string name;
};

type City record {
    string name = "";
    string lat = "";
    string lng = "";
};

type Cities record {
    City[] cities = [];
};

function name () {
    TestRecord rec = {
        id: 0,
        name: "marcus"
    };

string nameTest = rec.name;

Cities cities = {
            cities: [{
                name: "London",
                lat: "51.5073219",
                lng: "-0.1276474"
            }]
    };

json | error  payload = cities . cloneWithType ( json );
    string  nameTest1  =  rec . name . indexOf ( "mar" ) ;
      json   |   error      payload1 =  cities . cloneWithType ( json ) . toJsonString ( ) ;

      string str = "";
       foreach int i in 0 ..< 5 {
             str =str  .concat(   " "  )  ;
       }

       if(payload is json) {
                map<anydata>|   error  sd =payload . cloneWithType ( map < anydata > ) ;
           }
}