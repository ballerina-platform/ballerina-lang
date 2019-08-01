## Guidelines for Introducing New Integration Tests

#### Introducing a New Group/Test

- Services that can be started up independently are grouped into a ballerina package, which will be
 run using ballerina `run` at the start of each test group (e.g., `http`, `grpc`, etc)
    1. add the services to a package, with the correct name
    2. add a `.ballerina` folder (with a `.gitignore` file), `.gitignore` file and a `Ballerina.toml` file with
        ```toml
        [project]
        org-name = "ballerina-test"
        version = "0.0.1"
        ```
        
- Introduce a BaseTest for the group:
    1. with `@BeforeGroup` and `@AfterGroup` methods with the proper group names (e.g., `value = "http-test"`)
        ```java
        @BeforeGroups(value = "http-test", alwaysRun = true)
        public void start() throws BallerinaTestException {
        
        }
        ```
    2. start the services in the package introduced, in the `@BeforeGroup` method
    3. each test in the group depending on this package/base test should extend the base test and specify the group 
    they belong to as an annotation:
        ```java
        @Test(groups = "http-test")
        public class CompressionTestCase extends HttpBaseTest {
        
        }
        ```
    4. the ports used by all the services in the package need to be specified as required ports when starting the 
    server
    
#### Ballerina file naming and port order

When introducing new service bal files to a package:
1. use the next available port(s) in the group
2. add the port(s) at the end of the required ports list
3. prepend the test bal file with the next index 

    e.g., When the required ports array is `[9090, 9091, 9092]`, where `9090` is used in the file `abc.bal`, 
    and ports `9091` and `9092` are used in the file `def.bal`, test file names would be as follows:
    
        abc.bal → 01_abc.bal
        def.bal → 02_def.bal

4. When adding a new service in a file `ghi.bal` using two ports:
    - pick the next two ports - `9093` and `9094`
    - add the ports to the required ports array - `[9090, 9091, 9092, 9093, 9094]`
    - name the test file prepending the next index - `03_ghi.bal` 

