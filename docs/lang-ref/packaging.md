# Packaging

A Ballerina program consists of a number of Ballerina files, which may be in one or more packages. Ballerina uses a modular approach for managing names and organizing code into files. In summary, Ballerina entities (functions, services, etc.) all have globally unique qualified names consisting of their package name and the entity name. The package name is a collection of simple names concatenated with “.”, and they are stored in the file system using a subdirectory structure with each splitting at the “.”. If a package is not specified, the symbol will be in the default (unnamed) package. The name of the file that the entity resides in plays no part in the name of the entity.  

The following sections describes how Ballerina packaging works to enable self-contained execution and how the runtime searches for and discovers imported packages.

## Ballerina programs

The source directory contains all the code that the developer writes and may have files in multiple packages (and therefore multiple directories). However, third-party dependencies (which are used via import statements) are discovered from a repository (see below) and are not physically located within the program source hierarchy. The directory structure will be as follows:

```
program-name/
  package-directory-1/
  package-directory-2/
  ...
```

Such a program may have at most one `main()` function, which serves as the entry point for command-line execution, and zero or more services that are exposed as network entry points when the program is run as a service. 

## Ballerina libraries

Collections of Ballerina code can be packaged as a library so that the resulting package can be shared. Such a library can contain code coming from one or more Ballerina packages.

A Ballerina library is organized similar to a program:

```
library-name/
  package-directory-1/
  package-directory-2/
  ...
```

To install a library into your repository, you can simply add the library to your repository as follows:

```
ballerina repository add [library-archive-name]
```

If the library archive name is not specified, the current directory is assumed to be a library source directory and is inserted into the repository.

Note that all packages will be inserted into the library with version 1.0.0 at this time. Later we will support a way to indicate the version number per package.

Note also that we will later add sub-commands like “list”, “delete”, “search” etc. to the repository command.

## Ballerina repository

The Ballerina repository is a collection of Ballerina libraries. 

The language distribution ships with a built-in repository containing all the core language library (containing the `ballerina.*` packages) as well as third-party libraries. The initial distribution only contains code from WSO2, but we expect that to change over time.

In addition to the default repository, every developer can have a private repository. The default location of the private repository is `~/.ballerina` but can be changed by setting the BALLERINA_REPOSITORY environment variable.

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

## Building Ballerina programs

While Ballerina programs can be executed directly from the program directory, if you want create a self-contained package containing all the program code and third-party dependencies, you need to build the program into a packaged format. When a program is packaged using the `ballerina build` command, the resulting archive will contain not just the Ballerina files that contain the main function and/or services, but also all the Ballerina packages that are imported by all the code needed to execute the main function and/or services. When `ballerina build` is used to create a library archive, it packages the library code into the archive.

All shared Ballerina library archives will have the extension “.blz”. Use the following command to build the library archive:

```
ballerina build lib [-o library-archive-name] [DockerOptions]
```

A Ballerina executable archive containing a `main()` that is to be executed is named with the extension “.bmz”. Use the following command to build an executable archive:

```
ballerina build main main-package-name [-o filename] \
    [DockerOptions]
```

A Ballerina service archive containing one or more services is named with the extension “.bsz”. Use the following command to build a service archive:

```
ballerina build service [pkg1 pkg2 ..] [-o filename] \
    [DockerOptions]
```

In the above, `[DockerOptions]` refers to the options that instruct the build command to optionally create a Docker image for the `.bmz` or `.bsz` file. For details, see the *Ballerina Docker Architecture* document.

TODO: link to this file once it's checked in.

Note that Ballerina programs in the default package cannot be built, as these are meant primarily for simple demo type programs.
