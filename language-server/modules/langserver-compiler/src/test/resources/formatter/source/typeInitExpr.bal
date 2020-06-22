import ballerinax/java.jdbc;

type Person object {
    string name = "";

    function init(string name="" ,record {int id = 0;int address="";}  details={}) {
        self.name = name;
        var detailRec = details;
    }
};

function name() {
    jdbc:Client studentDb1 = new( {
        url: "jdbc:mysql://localhost:3306/testdb",
 username: "root",
            password: "",
      dbOptions:{useSSL: false}
           }   );

    jdbc:Client studentDb2 =new( {    url: "jdbc:mysql://localhost:3306/testdb",username:"root",password:""  ,dbOptions:{  useSSL:false }});

    jdbc:Client studentDb3 = new ({url: "jdbc:mysql://localhost:3306/testdb", username: "root", password: "",
dbOptions: {useSSL: false}});

 Person  p1=  new  Person (  "" ,{id: 0, address: ""} ) ;
                 Person  p2   =new   ;
Person p3 = new("",{  id:0,   address : ""});
Person p4 = new Person("", {
id: 0,address: ""});
Person p5 = new ("" , {id: 0,
        address: ""});

     jdbc:Client studentDb4 =
             new
               ({
                    url: "jdbc:mysql://localhost:5690/testdb",
                 username: "root",
                 password: "",
                 dbOptions: {useSSL: false}
      })
               ;
}