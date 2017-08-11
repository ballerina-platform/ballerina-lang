function test () {
      map     propertiesMap   =      {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR", "username":"SA",
                             "password"  :     ""   ,  "maximumPoolSize"        :   1   }   ;
    sql :    ClientConnector  testDB   =    create     sql     :    ClientConnector (    propertiesMap   )  ;
      sql     :        Parameter       [      ]      parameters   =     [   ]     ;
    transaction {
         int     insertCount  =    sql   :      ClientConnector       .update        (testDB,        "Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')", parameters);

        insertCount     =   sql   :      ClientConnector       .      update        (testDB,        "Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')", parameters);
        int      i    =  0  ;
          if      (i      ==        0      )       {
                  abort       ;//transaction can be aborted based on a condition
        }
    }  failed{
      system:println         (       "The transaction is failed"       )  ;
      retry   4  ;
        }      aborted        {
         system:println         (       "The transaction is aborted"       )  ;
         }       committed        {
      system:println         (       "The transaction is committed"       )  ;
        }
        sql :    ClientConnector .        close       (      testDB      )       ;
}
