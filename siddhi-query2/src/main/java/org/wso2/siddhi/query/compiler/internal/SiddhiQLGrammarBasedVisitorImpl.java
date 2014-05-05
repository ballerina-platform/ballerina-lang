package org.wso2.siddhi.query.compiler.internal;

import org.antlr.v4.runtime.misc.NotNull;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.input.TransformedStream;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.handler.Transformer;
import org.wso2.siddhi.query.api.query.input.handler.Window;
import org.wso2.siddhi.query.api.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;
import org.wso2.siddhi.query.compiler.SiddhiQLGrammarBaseVisitor;
import org.wso2.siddhi.query.compiler.SiddhiQLGrammarParser;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.List;

public class SiddhiQLGrammarBasedVisitorImpl extends SiddhiQLGrammarBaseVisitor {


    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitDefinitionStream(@NotNull SiddhiQLGrammarParser.DefinitionStreamContext ctx) {

        StreamDefinition streamDefinition = QueryFactory.createStreamDefinition();
        streamDefinition.name(ctx.source().getText());

        List<Attribute> attributeList = (List<Attribute>) visitDefinition(ctx.definition());
        if (attributeList != null) {
            for (Attribute attribute : attributeList) {
                streamDefinition.attribute(attribute.getName(), attribute.getType());
            }
        } else {
            throw new SiddhiParserException("Operation not supported :fromSource and likeSource");
        }
        return streamDefinition;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitDefinitionTable(@NotNull SiddhiQLGrammarParser.DefinitionTableContext ctx) {
        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name(ctx.source().getText());

        List<Attribute> attributeList = (List<Attribute>) visitDefinition(ctx.definition());
        if (attributeList != null) {
            for (Attribute attribute : attributeList) {
                tableDefinition.attribute(attribute.getName(), attribute.getType());
            }
        } else {
            throw new SiddhiParserException("Operation not supported :fromSource and likeSource");
        }

        for (int i = 0; i < ctx.parameterName().size(); i++) {
            tableDefinition.fromParameter((String) visit(ctx.parameterName(i)), (String) visit(ctx.parameterValue(i)));
        }
        return tableDefinition;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitDefinition(@NotNull SiddhiQLGrammarParser.DefinitionContext ctx) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (int i = 0; i < ctx.attributeName().size(); i++) {
            attributeList.add(new Attribute((String) visit(ctx.attributeName(i)), (Attribute.Type) visit(ctx.type(i))));
        }
        return attributeList;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitQuery(@NotNull SiddhiQLGrammarParser.QueryContext ctx) {
        Stream inputStream = (Stream) visitQueryInput(ctx.queryInput());
        OutStream queryOutput = (OutStream) visitQueryOutput(ctx.queryOutput());
        return QueryFactory.createQuery().from(inputStream).outStream(queryOutput);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStandardStream(@NotNull SiddhiQLGrammarParser.StandardStreamContext ctx) {
        TransformedStream stream = QueryFactory.inputStream((String) visitSource(ctx.source()));
        for (int i = 2; i < ctx.getChildCount(); i++) {
            Object handler = visit(ctx.getChild(i));
            if (handler instanceof Filter) {
                ((BasicStream) stream).setFilter((Filter) handler);
            } else if (handler instanceof Transformer) {
                ((BasicStream) stream).setTransformer((Transformer) handler);
            } else if (handler instanceof Window) {
                stream.window((Window) handler);
            } else {
                throw new SiddhiParserException("Unsupported operation " + ctx.getChild(i).getText());
            }

        }
        return stream;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFilter(@NotNull SiddhiQLGrammarParser.FilterContext ctx) {
        return new Filter((org.wso2.siddhi.query.api.condition.Condition) visitCondition(ctx.condition()));
    }



    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitQueryOutput(@NotNull SiddhiQLGrammarParser.QueryOutputContext ctx) {
        if ("insert".equals(ctx.getChild(0).getText())) {
            InsertIntoStream insertIntoStream = new InsertIntoStream((String) visitTarget(ctx.target()));
            insertIntoStream.setOutputEventsFor((OutStream.OutputEventsFor) visitOutputEventType(ctx.outputEventType()));
            return insertIntoStream;
        } else {
            throw new SiddhiParserException("Operation not supported yet:" + ctx.getChild(0).getText());
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitOutputEventType(@NotNull SiddhiQLGrammarParser.OutputEventTypeContext ctx) {
        if ("expired-events".equals(ctx.getText())) {
            return OutStream.OutputEventsFor.EXPIRED_EVENTS;
        } else if ("all-events".equals(ctx.getText())) {
            return OutStream.OutputEventsFor.ALL_EVENTS;
        } else {
            return OutStream.OutputEventsFor.CURRENT_EVENTS;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitId(@NotNull SiddhiQLGrammarParser.IdContext ctx) {
        return ctx.getText();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIntType(@NotNull SiddhiQLGrammarParser.IntTypeContext ctx) {
        return Attribute.Type.INT;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitLongType(@NotNull SiddhiQLGrammarParser.LongTypeContext ctx) {
        return Attribute.Type.LONG;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBoolType(@NotNull SiddhiQLGrammarParser.BoolTypeContext ctx) {
        return Attribute.Type.BOOL;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStringType(@NotNull SiddhiQLGrammarParser.StringTypeContext ctx) {
        return Attribute.Type.STRING;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFloatType(@NotNull SiddhiQLGrammarParser.FloatTypeContext ctx) {
        return Attribute.Type.FLOAT;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitDoubleType(@NotNull SiddhiQLGrammarParser.DoubleTypeContext ctx) {
        return Attribute.Type.DOUBLE;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitObjectType(@NotNull SiddhiQLGrammarParser.ObjectTypeContext ctx) {
        return Attribute.Type.OBJECT;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitDoubleVal(@NotNull SiddhiQLGrammarParser.DoubleValContext ctx) {
        Double aDouble = Double.parseDouble(ctx.POSITIVE_DOUBLE_VAL().getText());
        if (ctx.negative != null) {
            aDouble = aDouble * -1;
        }
        return aDouble;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitLongVal(@NotNull SiddhiQLGrammarParser.LongValContext ctx) {
        Long aLong = Long.parseLong(ctx.POSITIVE_LONG_VAL().getText());
        if (ctx.negative != null) {
            aLong = aLong * -1;
        }
        return aLong;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFloatVal(@NotNull SiddhiQLGrammarParser.FloatValContext ctx) {
        Float aFloat = Float.parseFloat(ctx.POSITIVE_FLOAT_VAL().getText());
        if (ctx.negative != null) {
            aFloat = aFloat * -1;
        }
        return aFloat;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIntVal(@NotNull SiddhiQLGrammarParser.IntValContext ctx) {
        Integer integer;
        if (ctx.NUMBER() != null) {
            integer = Integer.parseInt(ctx.NUMBER().getText());
        } else {
            integer = Integer.parseInt(ctx.POSITIVE_INT_VAL().getText());

        }

        if (ctx.negative != null) {
            integer = integer * -1;
        }
        return integer;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBoolVal(@NotNull SiddhiQLGrammarParser.BoolValContext ctx) {
        return "true".equals(ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStringVal(@NotNull SiddhiQLGrammarParser.StringValContext ctx) {
        return ctx.STRING_VAL().getText();
    }
}
