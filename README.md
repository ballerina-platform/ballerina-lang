# Ballerina Composer

The Ballerina Composer provides a flexible and powerful browser-based tool for creating your Ballerina programs. You can build your integrations by creating sequence diagrams, dragging elements from a tool palette onto a canvas. As you build the diagrams, the underlying code is written for you, which you can work with in the Source view. You can also use the Swagger view to define services by writing Swagger definitions. You can switch seamlessly between the Design view, Source view, and Swagger view and create your programs in the way that you like to work.

**You can create your integration in Design view:**

![alt text](./docs/images/DesignView.png?raw=true "Design view")


**And go to Source view to edit the code that's generated:**

![alt text](./docs/images/SourceView.png?raw=true "Source view")

**You can also go to Swagger view to edit the Swagger definition that's generated:**

![alt text](./docs/images/SwaggerView.png?raw=true "Source view")

## How to build
The build process of the composer works with Node Package Manager(npm). Using the `npm` commands building of the workspace service is also executed.

### Prerequisites
-[Latest NPM installed](https://docs.npmjs.com/getting-started/installing-node)
-Maven

### Steps to build
-`npm install` To get all the dependencies.
-`npm run build-pack` To build the complete distribution including the site and the microservice.

### Dev commands
-`npm run build` or `npm run webpack` To build the site only.
-`npm run build-service` To build the backend microservice.
-`npm run build-dist` To build the backend microservice and build the distribution.
-`npm run dev` To build the site with hot deployment.

## Running the Composer

The Composer is included in the full distribution of Ballerina, which you can download from www.ballerinalang.org. After you unzip it, navigate to its `bin` directory in the command line, and enter the following command:

For Windows
```
composer.bat
```

For Unix/Linux
```
./composer
```

The command line will display the URL you can use to access the Composer in your browser.

For complete instructions on creating your integrations and using the Composer, see the [Ballerina documentation](http://ballerinalang.org/documentation/).
