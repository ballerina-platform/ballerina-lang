
public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType annotationCommon;

public annotation AnnotationType a1 on service;

public annotation AnnotationType a2 on function;

annotation AnnotationType a3 on service;

public annotation AnnotationType typeAnnotation1 on type;

public const annotation typeAnnotation2 on type;

public annotation typeAnnotation3 on type;

public annotation AnnotationType classAnnotation1 on class;

public annotation classAnnotation2 on class;

public const annotation classAnnotation3 on source class;

public annotation AnnotationType functionAnnotation1 on function;

public annotation functionAnnotation2 on function;

public annotation AnnotationType objectFunctionAnnotation1 on object function;

public annotation objectFunctionAnnotation2 on object function;

public annotation AnnotationType resourceFunctionAnnotation1 on resource function;

public annotation resourceFunctionAnnotation2 on resource function;

public annotation AnnotationType parameterAnnotation1 on parameter;

public annotation parameterAnnotation2 on parameter;

public annotation AnnotationType returnAnnotation1 on return;

public annotation returnAnnotation2 on return;

public annotation AnnotationType serviceAnnotation1 on service;

public annotation serviceAnnotation2 on service;

public annotation AnnotationType fieldAnnotation1 on field;

public annotation fieldAnnotation2 on field;

public annotation AnnotationType objectFieldAnnotation1 on object field;

public annotation objectFieldAnnotation2 on object field;

public annotation AnnotationType recordFieldAnnotation1 on record field;

public annotation recordFieldAnnotation2 on record field;

public const annotation AnnotationType sourceAnnotationAnnotation1 on source annotation;

public const annotation sourceAnnotationAnnotation2 on source annotation;

public const annotation AnnotationType sourceExternalAnnotation1 on source external;

public const annotation sourceExternalAnnotation2 on source external;

public const annotation AnnotationType sourceVarAnnotation1 on source var;

public const annotation sourceVarAnnotation2 on source var;

public const annotation AnnotationType sourceConstAnnotation1 on source const;

public const annotation sourceConstAnnotation2 on source const;

public const annotation AnnotationType sourceListenerAnnotation1 on source listener;

public const annotation sourceListenerAnnotation2 on source listener;

public const annotation AnnotationType sourceWorkerAnnotation1 on source worker;

public const annotation sourceWorkerAnnotation2 on source worker;
