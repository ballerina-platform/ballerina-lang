# Packaging and Running Programs

A Ballerina program can consist of a number of Ballerina files, which you can organize into packages simply by creating subdirectories as follows:

```
program-name/
  package-directory-1/
  package-directory-2/
  ...
```

Each Ballerina entity (function, service, etc.) has a globally unique qualified name consisting of its package name and entity name (the file name is NOT part of the entity's name) concatenated with periods. For example, let's say you have a service named StockQuoteService inside a file named `foo.bal`, which is located in the following directory structure:

```
myProgram
  resources
  services
    foo.bal
```
The StockQuoteService entity's fully qualified name would be: `myProgram.services.StockQuoteService` 

When you create a Ballerina program in the Composer, you can declare the program's package in the Package box in the upper left corner of the canvas. Or you can simply type `package <package-name>;` in the Source view. When you declare a package in the program file, be sure to save the file in the correct directory hierarchy to match that package. If a package is not specified, the program will be in the default (unnamed) package. 

**Note:** When you name your files, directories, and packages, and when you name your Ballerina elements such as services and resources, be sure to avoid using the [reserved names](lang-overview.md#reserved-names).

The `<program-name>` directory contains all the code that the developer writes and may have files in multiple packages (and therefore multiple directories) as in the example above. However, third-party dependencies (which are used via import statements as described below) are discovered from a [repository](#the-ballerina-repository) and are not physically located within the program source hierarchy. 

Each Ballerina program can have at most one `main()` function, which serves as the entry point for command-line execution, and zero or more services that are exposed as network entry points when the program is run as a service. Therefore, when organizing your files under a `<program-name>` directory, be sure that there is no more than one file containing the `main()` function. 

## Importing packages
To import a package into your program, you can use the Imports box in the upper left corner of the canvas, or simply type `import <package-name>;` in the Source view. 

All built-in Ballerina library functions and connectors are defined in the `ballerina.*` packages. You don’t need to add import statements for system packages that start with `ballerina.lang.*` because they are imported by default and can be accessed by qualifying the symbol with the last part of the package name. For example, all message data type related functions (which live in the `ballerina.lang.message` package) can be accessed as `message:FunctionName`. 

## Ballerina libraries

Collections of Ballerina code can be packaged as a library so that the resulting package can be shared. Such a library can contain code coming from one or more Ballerina packages.

A Ballerina library is organized similar to a program:

```
library-name/
  package-directory-1/
  package-directory-2/
  ...
```

To install a library into your repository, you add it as follows:

```
ballerina repository add [library-archive-name]
```

If the library archive name is not specified, the current directory is assumed to be a library source directory and is inserted into the repository. Library archives have the file extension `.blz`.

**Note**: In the current release, you can use existing libraries, but you cannot yet build your own library archives.

## The Ballerina repository

The Ballerina repository is a collection of Ballerina libraries. 

The Ballerina language distribution ships with a built-in repository containing all the core language libraries (containing the `ballerina.*` packages) as well as third-party libraries. In addition to the default repository, every developer can have a private repository. The default location of the private repository is `~/.ballerina`, but you can change the location by setting the BALLERINA_REPOSITORY environment variable.

A repository is organized as follows:

```
repository-directory/
  src/
    dir1/
      version/
        .bal files in package dir1 of the indicated version
      dir2/
        version/ # one version
          .bal files in package dir1.dir2 of the indicated version
        version/ # another version
          .bal files in package dir1.dir2 of the indicated version
      dir3/
        version/
          .bal files in ...
    dir4/
    dir5/
      version/
        .bal files in ...
```

## Creating Ballerina archives

While Ballerina programs can be executed directly from the program directory, if you want to create a self-contained package containing all the program code and third-party dependencies, you need to build the program into a packaged format. When a program is packaged using the `ballerina build` command, the resulting archive will contain not just the Ballerina files that contain the main function and/or services, but also all the Ballerina packages that are imported by all the code needed to execute the main function and/or services. 

Note: if you are running on UNIX/Linux, use `./ballerina build` instead of `ballerina build` in the following commands.

A Ballerina executable archive containing a `main()` function is named with the extension “.bmz”. Use the following command to build an executable archive:

```
ballerina build main <main-package-name> [-o filename] 
```

A Ballerina service archive containing one or more services is named with the extension “.bsz”. Use the following command to build a service archive:

```
ballerina build service <pkg1> [<pkg2> <pkg3> ...] [-o filename]
```

## Running a Ballerina program

The `ballerina` command runs a Ballerina program/service in its packaged or unpackaged format. (If you are using UNIX/Linux, type `./ballerina` instead of `ballerina`.)

To execute `main()` from a `.bal` file or a package or archive file:

```
ballerina run main (filename | packagename | mainarchive)
```

To run named services:

```
ballerina run service (filename | packagename | servicearchive)+ 
```

To run a collection of service archives from service root:

```
ballerina run service [-sr serviceroot]
```

## Creating and running a Docker image of the archive
After you have built an archive, you can create a Docker image of it and run it in the container. 

To create a Docker image from a Ballerina package, you run `ballerina docker` and provide the package name as an argument:

```
$ ./ballerina docker helloWorld.bmz
ballerina: build docker image [helloworld:latest] in docker host [localhost]? (y/n): y

ballerina: docker image helloworld:latest successfully built.

Use the following command to start a container.
        docker run --name determined_aluminum -it helloworld:latest

```

You can additionally provide a customized image name:

```
./ballerina docker helloWorld.bmz -t myhelloworld:0.1
ballerina: build docker image [myhelloworld:0.1] in docker host [localhost]? (y/n): y

ballerina: docker image myhelloworld:0.1 successfully built.

Use the following command to start a container.
        docker run --name burning_aids -it myhelloworld:0.1

```

If you want to use a remote Docker daemon, you can specify it using the -H flag so the Docker image is created at the remote end:

```
./ballerina docker helloWorld.bmz -H http://127.0.0.1:2375
ballerina: build docker image [myhelloworld:0.1] in docker host [http://127.0.0.1:2375]? (y/n): y

ballerina: docker image helloworld:latest successfully built.

Use the following command to start a container.
        docker run --name future_aquarium -it helloworld:latest
```

For more information on the usage of this command, type `ballerina docker --help`.
