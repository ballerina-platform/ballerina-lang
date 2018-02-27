@Description { value:"The Databases which has direct parameter support."}
@Field { value:"MYSQL: MySQL DB with connection url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
@Field { value:"SQLSERVER: SQL Server DB with connection url in the format of jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
@Field { value:"ORACLE: Oracle DB with connection url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
@Field { value:"SYBASE: Sybase DB with connection url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
@Field { value:"POSTGRE: Postgre DB with connection url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
@Field { value:"IBMDB2: IBMDB2 DB with connection url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
@Field { value:"HSQLDB_SERVER: HSQL Server with connection url in the format of jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
@Field { value:"HSQLDB_FILE: HSQL Server with connection url in the format of jdbc:hsqldb:file:[path]/[database]"}
@Field { value:"H2_SERVER: H2 Server DB with connection url in the format of jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
@Field { value:"H2_FILE: H2 File DB with connection url in the format of jdbc:h2:file://[path]/[database]"}
@Field { value:"DERBY_SERVER: DERBY server DB with connection url in the format of jdbc:derby://[HOST]:[PORT]/[database]"}
@Field { value:"DERBY_FILE: Derby file DB with connection url in the format of jdbc:derby://[path]/[database]"}
@Field { value:"GENERIC: Custom DB connection with given connection url"}
public enum db {
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
}
