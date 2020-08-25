@Override:annotations
public enum Color <fold text='{...}'>{
  RED = "read-color",
  GREEN = "green-color",
  @checkmising
  isMissing = true,
  attributes,
  value = +5,
  <fold text='# ...'># parents can be null</fold>
  @checknull
  parents = null,
  other
}

@Description { value:"The Databases which has direct parameter support."</fold>}
@Field <fold text='{...}'>{ value:"MYSQL: MySQL DB with connection url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"SQLSERVER: SQL Server DB with connection url in the format of jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}</fold>
@Field <fold text='{...}'>{ value:"ORACLE: Oracle DB with connection url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"SYBASE: Sybase DB with connection url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"POSTGRE: Postgre DB with connection url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"IBMDB2: IBMDB2 DB with connection url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"HSQLDB_SERVER: HSQL Server with connection url in the format of jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"HSQLDB_FILE: HSQL Server with connection url in the format of jdbc:hsqldb:file:[path]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"H2_SERVER: H2 Server DB with connection url in the format of jdbc:h2:tcp://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"H2_FILE: H2 File DB with connection url in the format of jdbc:h2:file://[path]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"DERBY_SERVER: DERBY server DB with connection url in the format of jdbc:derby://[HOST]:[PORT]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"DERBY_FILE: Derby file DB with connection url in the format of jdbc:derby://[path]/[database]"}</fold>
@Field <fold text='{...}'>{ value:"GENERIC: Custom DB connection with given connection url"}</fold>
public enum db <fold text='{...}'>{
    MYSQL,
    SQLSERVER,
    ORACLE,
    SYBASE,
    POSTGRE,
    IBMDB2,
    HSQLDB_SERVER,
    HSQLDB_FILE,
    H2_SERVER,
    H2_FILE,
    DERBY_SERVER,
    DERBY_FILE,
    GENERIC
}</fold>
