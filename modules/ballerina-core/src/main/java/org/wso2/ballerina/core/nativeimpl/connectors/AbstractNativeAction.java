package org.wso2.ballerina.core.nativeimpl.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.Interpreter;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.NativeConstruct;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.Utils;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents Native Ballerina Action.
 */
public abstract class AbstractNativeAction implements Action, NativeConstruct, Interpreter {
    protected NativeConnector connector;

    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeAction.class);
    private String packageName, actionName;
    private SymbolName symbolName;
    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Type> returnTypes;
    private List<Const> constants;

    public AbstractNativeAction() {
        parameters = new ArrayList<>();
        returnTypes = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();

    }

    /*
     * Build Native Action Model using Java annotation.
     */
    private void buildModel() {
        BallerinaAction action = this.getClass().getAnnotation(BallerinaAction.class);
        packageName = action.packageName();
        actionName = action.actionName();
        symbolName = new SymbolName(actionName, SymbolName.SymType.CALLABLE_UNIT);
        Arrays.stream(action.args()).
                forEach(argument -> {
                    try {
                        parameters.add(new Parameter(TypeC.getType(argument.type().getName()),
                                new SymbolName(argument.name())));
                    } catch (RuntimeException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.warn("Error while processing Parameters for Native ballerina action {}:{}.", packageName,
                                actionName, e);
                    }
                });
        Arrays.stream(action.returnType()).forEach(returnType -> {
            try {
                returnTypes.add(TypeC.getType(returnType.getName()));
            } catch (RuntimeException e) {
                // TODO: Fix this when TypeC.getType method is improved.
                log.warn("Error while processing ReturnTypes for Native ballerina action {}:{}.", packageName,
                        actionName, e);
            }
        });
        Arrays.stream(action.consts()).forEach(constant -> {
            try {
                constants.add(Utils.getConst(constant));
            } catch (MalformedEntryException e) {
                log.warn("Error while processing pre defined const {} for Native ballerina action {}:{}.",
                        constant.identifier(), packageName, actionName, e);
            }
        });
        // TODO: Handle Ballerina Annotations.
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getName() {
        return symbolName.getName();
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public Type[] getReturnTypes() {
        return returnTypes.toArray(new Type[returnTypes.size()]);
    }

    @Override
    public void interpret(Context ctx) {
        connector.init();
    }

    /**
     * Get Argument by index.
     *
     * @param context current {@code {@link Context}} instance.
     * @param index   index of the parameter.
     * @return BValue;
     */
    public BValueRef getArgument(Context context, int index) {
        if (index > -1 && index < parameters.size()) {
            return context.getControlStack().getCurrentFrame().values[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    public abstract void execute(Context context);



}
