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
                            <outputDir>${output.dir}</outputDir>
                            <sourceDir>${generated.ballerina.source.directory}</sourceDir>
                            <templatesDir>/home/docerina/templates</templatesDir>
                            <packageFilter>org.ballerinalang.xyz</packageFilter>
                            <debugDocerina>false</debugDocerina>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
      	</plugins>
   	</build>
	  
Supported list of configuration parameters are listed below;

* `sourceDir`: Comma separated list of the paths to the directories where Ballerina source files reside or 
paths to Ballerina files which does not belong to a package.

 MANDATORY property.
 Example: `<sourceDir>/home/docerina/abc/src,/home/docerina/xyz/src</sourceDir>`
 
* `outputDir`: Points to the location where the generated API docs will be stored. 

 OPTIONAL property.
 DEFAULT value is the ${project.build.directory} which is the target directory.
 Example: `<outputDir>/home/docerina/output</outputDir>`

* `templatesDir`: Points to a custom templates directory.

 OPTIONAL property.
 DEFAULT value is the embedded templates folder.
 Example: `<templatesDir>/home/docerina/templates</templatesDir>`

* `packageFilter`: Comma separated list of package names to be filtered from the documentation.

 OPTIONAL property.
 DEFAULT value is none.
 Example: `<packageFilter>org.ballerinalang.abc.a,org.ballerinalang.xyz.x</packageFilter>`

* `generateTOC`: Generate table of content for package(s).

 OPTIONAL property.
 DEFAULT value is false.
 Example: `<generateTOC>false</generateTOC>`

* `debugDocerina`: Enable debug level logs.

 OPTIONAL property.
 DEFAULT value is false.
 Example: `<debugDocerina>true</debugDocerina>`
