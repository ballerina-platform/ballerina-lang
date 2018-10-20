# How to Document Ballerina Code

Ballerina has a built-in documentation framework named Docerina. The documentation framework allows you to write unstructured document with a bit of structure to enable generating HTML content as API documentation.

Developers can write the documentation inline with the Ballerina source code using the lightweight [markdown](https://daringfireball.net/projects/markdown/syntax) markup language. They can mark special occurrences such as parameters, return parameters, fields, endpoints within the documentation code using documentation attributes. Once the code is documented, developers can generate a basic HTML version of their Ballerina modules using the `ballerina doc` command. You are encouraged to have your custom themes and styles, to have a better presentation of your Ballerina documentation.

Ballerina design and usage is aligned with project and module semantics of Ballerina. You can generate documentation for the project modules using the ``ballerina doc`` command.


## Overview

* Ballerina programmers can place the documentation inline with the source code using documentation syntax.
* Ballerina records, type definitions, objects, global variables, annotations, endpoints can be documented using the documentation syntax.
* Fields, parameters, return parameters, endpoints can be marked using documentation attributes.
* HTML documents can be generated using the ``ballerina doc`` command for each Ballerina module and if you have custom handlebars templates, you can use them to generate the HTMLs.

## Writing Ballerina Documentation

Documentation is a first class syntax in the Ballerina language. The `documentation` keyword followed by curly braces denotes a documentation node.

```
documentation {
     <your markdown documentation goes here>
}
```

Within this `documentation` syntax, you can write your documentation in markdown markup language. For example:

```
documentation {
   Provides the HTTP actions for interacting with an HTTP server. Apart from the standard
   HTTP methods, `forward()` and `execute()` functions are provided. More complex and
   specific endpoint types can be created by wrapping this generic ```HTTP``` actions
   implementation.
   ...
}
```

While you have the freedom to write any markdown document within the `documentation` syntax, Docerina encourages you to have some structure within the `documentation` syntax. Recommended structure is defined below:

```
documentation {
     <description>
     <documentation_attribute>{{<documetable_object_name>}} <description> +
}
```

## Supported Documentation Attributes

`P` - Used to document a function input parameter
```
P{{id}} id of the employee
```
`R` - Used to document a function return parameter
```
R{{}} returns the name of the employee
```
`F` - Used to document a field of an object
```
F{{dob}} date of birth of the employee
```
`E` - Used to indicate an Endpoint object
```
E{{}}
```
> **NOTE:** Unlike other attributes, `E` attribute does not follow a description as it is used as an indicator.


### Sample Usage

```ballerina
documentation {
        Submits an HTTP request to a service with the specified HTTP verb.
        The `submit()` function does not give out a `Response` as the result,
        rather it returns an `HttpFuture` which can be used to do further
        interactions with the endpoint.

        Example:
         ```ballerina
               HttpFuture future = myMsg.submit("GET", "/test", req);
         ```


        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{request}} An HTTP outbound request message
        R{{}} An `HttpFuture` that represents an asynchronous service invocation,
        or an `error` if the submission fails
    }
    public function submit(@sensitive string httpVerb, string path, Request request)
         returns HttpFuture|error;
```

## Documenting A Module

A Ballerina module can have a `Module.md` file which describes the module and its usage.

A typical project structure of a Ballerina project is like this:

```
/
  .gitignore
  Ballerina.toml       # Configuration that defines project intent
  .ballerina/          # Internal cache management and contains project repository
                       # Project repository is built

  main.bal             # Part of the “unnamed” module, compiled into a main.balx
                       # You can have many files in the "unnamed" module,
                       # though unadvisable

  module1/            # The source in this directory will be named “<org-name>/module1”
    Module.md         # Optional, contains descriptive metadata for display at
                       # Ballerina Central
    *.bal              # In this dir and recursively in subdirs except tests/ and
                       # resources/
    [tests/]           # Module-specific unit and integration tests
    [resources/]       # Module-specific resources

  modules.can.include.dots.in.dir.name/
    Module.md
    *.bal
    [tests/]
    [resources/]

  [resources/]         # Resources included with every module in the project

  target/              # Compiled binaries and other artifacts end up here
      main.balx
      Ballerina.lock   # Generated during build, used to rebuild identical binary
      module1.balo
      modules.can.include.dots.in.dir.name.bal
```

`ballerina doc` command will read the `Module.md` and append it in the generated HTML file.

Please check [HTTP module documentation](https://ballerina.io/learn/api-docs/ballerina/http.html) for a sample HTML that has a `Module.md` content at the top, followed by the other module constructs.


## Generating Ballerina Documentation

Ballerina provides a `doc` command which can be executed against a given Ballerina project. This command will result in generating the Ballerina documentation as HTML files, for all the modules in the project.

First, let's create a new Ballerina project:
```bash
$ mkdir myproject
$ cd myproject
$ ballerina init -i
Create Ballerina.toml [yes/y, no/n]: (n) y
Organization name: (nirmal) y
Version: (0.0.1)
Ballerina source [service/s, main/m]: (s) s
Module for the service : (no module) math
Ballerina source [service/s, main/m, finish/f]: (f) s
Module for the service : (no module) time
Ballerina source [service/s, main/m, finish/f]: (f) f

Ballerina project initialized
```
Now, let's generate documentation of the project:
```bash
$ ballerina doc
```
Output:
```bash
docerina: API documentation generation for sources - [math, time]
HTML file written: /private/tmp/myproject/api-docs/html/math.html
HTML file written: /private/tmp/myproject/api-docs/html/index.html
HTML file written: /private/tmp/myproject/api-docs/html/module-list.html
HTML file written: /private/tmp/myproject/api-docs/html/math.html
HTML file written: /private/tmp/myproject/api-docs/html/time.html
HTML file written: /private/tmp/myproject/api-docs/html/index.html
HTML file written: /private/tmp/myproject/api-docs/html/module-list.html
```

`api-docs/html` folder would contain following files;
```bash
$ ls api-docs/html/
docerina-theme    index.html    math.html   module-list.html
time.html
```

* `index.html`  - contains an index to the ballerina project documentation
* `math.html` - contains the content of the module named `docker`
* `module-list.html` - contains the module list which will be useful to find out the list of modules.
* `docerina-theme` - folder contains basic styles shipped by default with the pack.

If you want to generate documentation for a selected Ballerina module, then you can execute the following command from the ballerina project root directory:

```bash
$ ballerina doc <module_name>
```

If you have custom Handlebars templates, you can pass them via the `doc` command:

```bash
$ ballerina doc -t <path_to_templates> <module_name>
```

If you have a custom Organization name, you can pass it using `-e` flag:

```bash
$ ballerina doc -e orgName=<your_organization_name> <module_name>
```

For other options, please run `ballerina doc --help`.