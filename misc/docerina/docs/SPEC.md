## Ballerina API Doc generator

  * [Background](#background)
  * [Output formats](#output-formats)
  * [Documenting Ballerina programs](#documenting-ballerina-programs)
  * [Doc generation](#doc-generation)
  * [Documentation Layout](#documentation-layout)
    + [Project Page](#project-page)
    + [Module Page](#module-page)
    + [Construct Page](#construct-page)
    + [Single Construct Pages](#single-construct-pages)
      - [Record Page](#record-page)
      - [Object Page](#object-page)
      - [Client Page](#client-page)
      - [Listener Page](#listener-page)
      - [Function Page](#function-page)
    + [Multi Construct Pages](#multi-construct-pages)
      - [Annotations Page](#annotations-page)
      - [Errors Page](#errors-page)
      - [Type Definitions Page](#type-definitions-page)
      - [Constants Page](#constants-page)
      - [Primitive Page](#primitive-page)
    + [General Requirements for pages](#general-requirements-for-pages)
    + [Cross module referencing](#cross-module-referencing)
    + [How to provide examples in documentation?](#how-to-provide-examples-in-documentation-)

### Background

Ballerina API Doc Generator will process ballerina source files and generate API documentation for the relevant source. This documentation will act as the specification for that tool. 

The documentation syntax will be covered in the language spec. Here, we will discuss how those documentation blocks will be converted to publishable API Documentation. 

Documentation generator will be a cli tool which will process the source files and generate a set of markdown files as the portable documentation format. 

This intermediate format will be portable across ballerina.io, central, etc. Depending on the application, this intermediate format can be processed with custom tools (eg: Docusaurus, mk docs, etc.) to produce custom HTML as needed.

We have two options to serve as HTML.

- Generate static HTML from markdown.
- Use a SPA to process the markdown files and generate HTML on demand. 

### Output formats

Ballerina build command will generate docs in the markdown format and it will be the format in which docs are pushed to central - alongside balos.

Ballerina doc command will generate docs in HTML format where the user is able to open them locally in browser (it will allow opting out to md format as well). It’s only for local usage.

After pulling a package from central, IDE tools can use their own tools to generate HTML from markdown (or a common tool may reside within Ballerina distribution it self for this purpose) or use a SPA to serve them on demand.

Ballerina Central can do the same to display docs in central website. 


### Documenting Ballerina programs

A comprehensive article on documenting Ballerina programs is available on ballerina.io.
“How to Document Ballerina Code” (https://ballerina.io/learn/how-to-document-ballerina-code/)

It provides the guidance on [documenting language constructs](https://ballerina.io/learn/how-to-document-ballerina-code/#writing-ballerina-documentation) in a program 
as well as [documenting a module](https://ballerina.io/learn/how-to-document-ballerina-code/#documenting-a-module).
Furthermore, there’s an explanation on [cli tool usage] (https://ballerina.io/learn/how-to-document-ballerina-code/#generating-ballerina-documentation).

Apart from currently available options, we will introduce a new project level documentation file (eg: Project.md) to capture the details on overall project.
Information on this file will be visible in Project page (described below).
For example, for the stdlib project, Project.md file can be used to give an overall description about all the available modules in std lib.

### Doc generation

* Do we allow to generate for a single bal file?
    
       We allow to generate for a project or for a selected set of modules in a project.

* Where will the docs be generated for a single file and a project?

       Docs will be generated at <project-root>/target/docs by default. Under that root directory, a sub directory will be created for each module.

### Documentation Layout

#### Project Page

- If there is more than one module per project we will generate an index file. 
- Index file will contain a list of modules in the project with their summary. 
- It will have a search box to search within modules. 
- Display version number of the project and modules.

#### Module Page

- Layout of the page will be two columns, where left column will have a set of navigation menus, and right column will have full module documentation.
- Left and right columns should be independently scrolled. 
- Left Column
     - Module title should be visible on top of the navigation page possibly we can have how to import. 
     - Top there will be a link to Project page ( where it lists all Modules )
     - Next there will be a navigation menu for the top level language construct categories.  ie. Clients, Listeners, Records, Objects, Functions, Type Definitions, Constants, Annotations, Errors. 
     - If a module does not have at least a single construct under a given category, we will refrain from showing that particular category in the navigation menu.
     - If a module does not have any public language construct, we will hide the navigation menu for the current module 
     - After navigation menu, we will list all the primitives, where the list is collapsible and by default collapsed.

- Right Column

     - Have the module title at the top followed by the description in the Module.md
     - Below the module description, list of all constructs in the module - grouped by their category, in same order as the category are mentioned in navigation menu, will be displayed.
     - Each item in a list mentioned above, will be linked to the dedicated page for that particular construct (which is described below)

#### Construct Page

- Each individual construct will get an its own dedicated page (except for constants)
- Layout of the page will be two columns, where left column will have a navigation menu, and right column will have construct documentation.
- Left and right columns can be independently scrolled. 
- Left Column
    - Top there will be links to module and project pages
    - Next there will be a navigation menu for sub elements - if any - of this construct (eg: fields in a record, methods of an Object, etc), anchor linked to particular section on right column
    - Next there will be a navigation menu to browse through all constructs in current module, grouped by their type. (current construct will be marked as the active)
    - Navigation menus for current construct vs current module will be clearly separated.
- Right Column
    - Top will be the title of the construct followed by the description
    - The next section will be customized depending on the construct type, to cater the best experience for that particular type. (discussed next)

#### Single Construct Pages

Pages where only a single construct of a particular group will be shown.

##### Record Page

- Left column (while having links to module & project pages) will have a menu to jump between different fields in right column 
- Right column top will be the title of the filed with its description 
- List fields (filed name, type, default value & description)
- Types will be linked doc page for that particular type (include cross modules references)

##### Object Page

- Left column (while having links to module & project pages) will have a menu to jump between different fields, methods in right column 
- Right column top will be the title of the construct with its description 
- List fields (filed name, type, default value & description)
Next will be the details of _init method, including args, etc. (Title can be new()).
- If there’s possibility an error be thrown, we should indicate that
- List all the remaining methods
- Parameters should show the name, type, default value & description
- Return section should show the return type & description
- Types of parameters and return types will be linked doc page for that particular type (include cross modules references)

##### Client Page

- Client page will be similar to Object Page except remote methods will have its own section at right column with a prominency over normal methods to highlight them

##### Listener Page

- Listener page will be similar to  Object Page.
- We will not show common life cycle methods __attach, __start and __stop
- We will not show any extern methods (should we do this for all objects?)

##### Function Page

- This page is full displaying a function which is in module level
- Object methods will not have it’s own page rather they stay in Object page itself
- Page Layout will two column
- Left column
   - top will have back to module/project page links 
   - module navigation menu
   - Project navigation menu
-  Right column
    - Top will be the function title (eg: Function time:format)
    - Next will be a code block showing function signature
    - Function description
    - Errors it can return in a separate section
    - Examples (we need to figure out where we want to keep examples? Doc comment itself?)

#### Multi Construct Pages

Page where all the constructs of a given group in the module will be listed.

##### Annotations Page

- Annotation page will list all the annotations available in the module 
- The list will show the name, attachment point, description and the data type which will be linked to particular record page.

##### Errors Page

- This page will list all the error types in a given module.
- Error identifier, description and the data type - linked to record page - will be displayed in the list

##### Type Definitions Page

- This page will list all the finite types in a given module.
- The list will include type name, possible value types and the description.

##### Constants Page

- All the constants in the module will be listed here along with their description

##### Primitive Page

#### General Requirements for pages

- Users should be able to get a sharable link to any of the sections in the API Docs. 
- All the examples should be syntax highlighted.

#### Cross module referencing

If type X from module A is being set as the return type of function Y in module B, Return type of function Y should be linked to documentation of type X under module A.

- If the module A is not coming from current project, the link will be pointed to ballerina-central documentation for that particular module. 
- Link may or may not work depending on the availability of that particular module on central
- Nevertheless we generate the link on a agreed format

#### How to provide examples in documentation?








