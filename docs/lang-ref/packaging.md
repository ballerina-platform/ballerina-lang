# Packaging

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
The StockQuoteService entity's fully qualified name would be: `myProgram.service.StockQuoteService` 

If a package is not specified, the symbol will be in the default (unnamed) package. 

The `<program-name>` directory contains all the code that the developer writes and may have files in multiple packages (and therefore multiple directories) as in the example above. However, third-party dependencies (which are used via import statements) are discovered from a [repository](#the-ballerina-repository) and are not physically located within the program source hierarchy. 

Each Ballerina program can have at most one `main()` function, which serves as the entry point for command-line execution, and zero or more services that are exposed as network entry points when the program is run as a service. Therefore, when organizing your files under a `<program-name>` directory, be sure that there is no more than one file containing the `main()` function. 

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

If the library archive name is not specified, the current directory is assumed to be a library source directory and is inserted into the repository.

Note: Currently, all packages are inserted into the library with version "1.0.0".

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

While Ballerina programs can be executed directly from the program directory, if you want to create a self-contained package containing all the program code and third-party dependencies, you need to build the program into a packaged format. When a program is packaged using the `ballerina build` command, the resulting archive will contain not just the Ballerina files that contain the main function and/or services, but also all the Ballerina packages that are imported by all the code needed to execute the main function and/or services. When `ballerina build` is used to create a library archive, it packages the library code into the archive. 

Note: if you are running on UNIX/Linux, use `./ballerina build` instead of `ballerina build` in the following commands.

All shared Ballerina library archives will have the extension “.blz”. Use the following command to build the library archive:

TODO: don't you need to specify the packages after the lib argument? Also, this command isn't recognized in Alpha.

```
ballerina build lib [-o <library-archive-name>]
```

A Ballerina executable archive containing a `main()` function is named with the extension “.bmz”. Use the following command to build an executable archive:

```
ballerina build main <main-package-name> [-o filename] 
```

A Ballerina service archive containing one or more services is named with the extension “.bsz”. Use the following command to build a service archive:

```
ballerina build service <pkg1> [<pkg2> <pkg3> ...] [-o filename]
```

If you want to build an archive and run it in a Docker image, you can use the following command:

```
ballerina docker <package-file-path> --tag | -t <image-name> --host | -h <hostURL>
```
