# Using the Docerina Maven Plugin

The Docerina Maven plugin is used to generate Ballerina API documentation at the build time of a Ballerina Package.

## Introduction

The Docerina Maven plugin is a Maven plugin, which is used within the Ballerina language eco-system. The Maven goals that are achieved through this plugin are explained below:

* `docerina`: For generating Ballerina API documentation. See the instructions on [configuring the `docerina` Maven goal](#configuring-the-docerina-maven-goal). 

This goal is executed during the compile phase in the default life cycle of the Maven build. You have the flexibility to configure the behavior of the plugin by passing the relevant parameters to this Maven goal.

### Configuring the docerina Maven goal

A sample pom.xml file configuration of the docerina Maven goal is shown below.

   	<build>
    	<plugins>
        	<plugin>
                <groupId>org.ballerinalang</groupId>
                <artifactId>docerina-maven-plugin</artifactId>
                <version>${docerina.maven.plugin.version}</version>
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>docerina</goal>
                        </goals>
                        <configuration>
                            <templatesDir>${templates.dir}</templatesDir>
                            <outputDir>${output.dir}</outputDir>
                            <sourceDir>${generated.ballerina.source.directory}</sourceDir>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
      	</plugins>
   	</build>
	  
Supported list of configuration parameters are listed below;

* `templatesDir`: Points to the location of the templates directory. Templates are required to generate HTML files.

 MANDATORY property.
 Example: `<templatesDir>/home/docerina/templates/html</templatesDir>`

 * `sourceDir`: Points to the location of the Ballerina source file/directory.

 MANDATORY property.
 Example: `<templatesDir>/home/docerina/ballerina/src</templatesDir>`
 
* `outputDir`: Points to the location where the generated API docs will be stored. 

 OPTIONAL property.
 DEFAULT value is the ${project.build.directory} which is the target directory.
 Example: `<outputDir>/home/docerina/output</outputDir>`.