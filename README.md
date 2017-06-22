# Ballerina Composer
[![Build Status](https://wso2.org/jenkins/job/ballerinalang/job/composer/badge/icon)](https://wso2.org/jenkins/job/ballerinalang/job/composer/)  
The Ballerina Composer provides a flexible and powerful browser-based tool for creating your Ballerina programs. You can build your integrations by creating sequence diagrams, dragging elements from a tool palette onto a canvas. As you build the diagrams, the underlying code is written for you, which you can work with in the Source view. You can also use the Swagger view to define services by writing Swagger definitions. You can switch seamlessly between the Design view, Source view, and Swagger view and create your programs in the way that you like to work.

**You can create your integration in Design view:**

![alt text](./docs/images/DesignView.png?raw=true "Design view")

**And go to Source view to edit the code that's generated:**

![alt text](./docs/images/SourceView.png?raw=true "Source view")

**You can also go to Swagger view to edit the Swagger definition that's generated:**

![alt text](./docs/images/SwaggerView.png?raw=true "Swagger view")

## How to build
The build process of the composer works on Maven and Node Package Manager(npm).

### Prerequisites
*   JDK 1.8.0  
*   [NodeJS](https://nodejs.org/en/) (Version 6.x.x)   
*   Maven 3.0.5  

### Steps to build
1.  Clone(`git clone`) and build the [ballerinalang/composer](https://github.com/ballerinalang/composer) project using `mvn clean install`.
2.  Clone(`git clone`) and build [ballerinalang/tools-distribution](https://github.com/ballerinalang/tools-distribution/) project using `mvn clean install`. Find the distribution in `tools-distribution/modules/ballerina-tools/target` folder.

### Developement commands on web module(`<BALLERINA_COMPOSER>/modules/web`)
*   `npm install` - Installs all npm dependencies.
*   `npm run clean` - Deletes the `/dist`(distribution folder) in the web module.  
*   `npm run build` or `npm run webpack` - To build the web module.  
*   `npm run dev` - To start development server with hot deployment. Go to [http://localhost:8080](http://localhost:8080) or [http://127.0.0.1:8080](http://127.0.0.1:8080) afterwards.
*   `npm run test` - Executes tests. The tests requires the composer service to run.
*   `npm run lint` - Run [eslint](http://eslint.org/) on the code to find potential problems in the code.

## Running the Composer

The Composer is included in the full distribution of Ballerina Tools Distribution, which you can download from www.ballerinalang.org. After you unzip it, navigate to its `/bin` directory in the command line, and enter the following command:

For Windows
```
composer.bat
```

For Unix/Linux
```
./composer
```

The command line will display the URL you can use to access the Composer in your browser.

For complete instructions on creating your integrations and using the Composer, see the [Ballerina documentation](http://ballerinalang.org/docs/user-guide/0.8/quick-tour/#run-the-composer).
