# Annotations

> NOTE: Annotations have not yet been implemented using this construct in v0.8.0. The current annotations are handled in a more ad hoc manner and are not validated by the parser or semantic analyzer. As such this is more a design note and feedback is welcome.

Ballerina uses annotations to provide information which is at a meta level or auxiliary to the core functionality of the program being written. These include the following:
- Structured documentation about the program
- Deployment related information or how the Ballerina program attaches to various transports and related configuration. For HTTP in particular this includes all the information necessary to generate a Swagger description of a Ballerina service.
- Information needed to present a better graphical representation of Ballerina, especially for when connectors are dynamically loaded into the editor.

Ballerina provides a syntax for defining new annotations and uses that syntax to define all the annotations that are used by various Ballerina packages.

At this time annotations can only be accessed using native code; i.e., we currently do not offer a Ballerina API to access annotation information at runtime. We will add that in the future if there is a clear need for such access from Ballerina programs. What that means is while anyone can define annotations, that is really only useful for Ballerina runtime implementors at this time.

## Structure of Annotations

An annotation has the following components:

- Name - this is a package qualified name like all other names in Ballerina. However, all annotations defined in the `ballerina.lang.annotations` package are used without qualification or importing. Thus they are in effect built-in annotations.
- Attachment information - indication of what language constructs the annotation can attach to.
- Content model - indication of what can go inside an annotation. An annotation can have zero or more attributes. Attributes may be of type string, int, double, boolean, or can be another annotation (i.e., one annotation can have an attribute which is another annotation).

## Defining Annotations

Annotations are defined using the following syntax:

```
annotation AnnotationName [attach TargetName1, TargetName2, ...] {
   (TypeName | @AnnotationName) AttributeName [= DefaultValue];*
}
```

TypeName can be any value type (string, int, double, boolean) or an array of them. AttributeName is the identifier used to set the attribute. The TargetName above can be any of: “service”, “resource”, “connector”, “action”, “function”, “struct”, “const” or “parameter”.

> NOTE: We are considering having a hierarchical syntax to allow one to constrain annotations to be attachable to resource parameters only (for example) by using a syntax such as "resource/parameter".

## Applying Annotations

Annotations must appear immediately before the construct that it is meant to attach to. If there are multiple annotations being applied the order of the annotations is not significant. The syntax for applying an annotation in its general form is as follows:

```
@AnnotationName (AttributeName = Value,
                 AttributeName = @AnnotationName (AttributeName = Value, ...),
                 ...)
```

If the annotation has no content then only the name needs to be written. Similarly, if there is only one attribute, the name can be skipped and the value given directly. Also, not all attributes need to be assigned a value and only those being assigned a value need to be mentioned when an annotation is applied.
