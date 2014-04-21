/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.TypeExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ConvertFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;
    private ExpressionExecutor variableExecutor;
    private TypeConverter typeConverter;
    private ExpressionExecutor format1Expression;
    private ExpressionExecutor format2Expression;
    private SimpleDateFormat format1;
    private SimpleDateFormat format2;
    private int runningAttributes;


    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    // convert(foo,string)
    // convert(foo<string>,long,"MM-YY")
    // convert(foo<string>,string,"MM-YY","MM-YYYY")
    // convert(foo<long>,string,"MM-YY")
    @Override
    public void init(Attribute.Type[] attributeTypes, SiddhiContext siddhiContext) {
        if (attributeSize < 2 || attributeSize > 4) {
            throw new QueryCreationException("Convert has to have 2 to 4 expressions; attribute, to converted type and 1 or 2 optional formats, but " + attributeSize + " expressions provided!");
        }
        variableExecutor = attributeExpressionExecutors.get(0);
        if (variableExecutor instanceof TypeExpressionExecutor) {
            throw new QueryCreationException("Convert's 1st expression should not be the to converted type, it has to be the value that need tobe converted");
        }
        ExpressionExecutor typeExpression = attributeExpressionExecutors.get(1);
        if (!(typeExpression instanceof TypeExpressionExecutor)) {
            throw new QueryCreationException("Convert's 2nd expression should be the to converted type");
        }
        returnType = (Attribute.Type) typeExpression.execute(null);

        runningAttributes = 2;

        if (attributeSize == 3) {
            format1Expression = attributeExpressionExecutors.get(2);

            if ((variableExecutor.getReturnType() != Attribute.Type.LONG || returnType != Attribute.Type.STRING || format1Expression.getReturnType() != Attribute.Type.STRING) &&
                (variableExecutor.getReturnType() != Attribute.Type.STRING || returnType != Attribute.Type.LONG || format1Expression.getReturnType() != Attribute.Type.STRING)) {
                throw new QueryCreationException("Convert only supports (<string variable>,<long type>,<string format>) or (<long variable>,<string type>,<string format>) or (<string variable>,<string type>,<string format>,<string format>) " +
                                                 "but here (" + variableExecutor.getReturnType() + "," + returnType + "," + format1Expression.getReturnType() + ") is provided ");
            }
            if (!(format1Expression instanceof ConstantExpressionExecutor)) {
                runningAttributes = 3;
            } else {
                format1 = new SimpleDateFormat((String) format1Expression.execute(null));
                format1.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
        }
        if (attributeSize == 4) {
            format1Expression = attributeExpressionExecutors.get(2);
            format2Expression = attributeExpressionExecutors.get(3);
            if (variableExecutor.getReturnType() != Attribute.Type.STRING || returnType != Attribute.Type.STRING || format1Expression.getReturnType() != Attribute.Type.STRING || format2Expression.getReturnType() != Attribute.Type.STRING) {
                throw new QueryCreationException("Convert only supports (<string variable>,<long type>,<string format>) or (<long variable>,<string type>,<string format>) or (<string variable>,<string type>,<string format>,<string format>) " +
                                                 "but here (" + variableExecutor.getReturnType() + "," + returnType + "," + format1Expression.getReturnType() + "," + format2Expression.getReturnType() + ") is provided ");
            }
            if (!(format1Expression instanceof ConstantExpressionExecutor) || !(format2Expression instanceof ConstantExpressionExecutor)) {
                runningAttributes = 4;
            } else {
                format1 = new SimpleDateFormat((String) format1Expression.execute(null));
                format2 = new SimpleDateFormat((String) format2Expression.execute(null));
            }
        }


        switch (returnType) {

            case STRING:
                switch (variableExecutor.getReturnType()) {

                    case STRING:
                        if (attributeSize == 4) {
                            if (runningAttributes == 4) {
                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        Object[] objects = (Object[]) obj;
                                        SimpleDateFormat format1 = new SimpleDateFormat((String) objects[1]);
                                        SimpleDateFormat format2 = new SimpleDateFormat((String) objects[2]);
                                        try {
                                            return format2.format(format1.parse((String) objects[0]));
                                        } catch (ParseException e) {
                                            return null;
                                        }
                                    }
                                };
                            } else {
                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        try {
                                            return format2.format(format1.parse((String) obj));
                                        } catch (ParseException e) {
                                            return null;
                                        }
                                    }
                                };
                            }

                        } else {
                            typeConverter = new TypeConverter() {
                                @Override
                                public Object convert(Object obj) {
                                    return obj.toString();
                                }
                            };
                        }
                        break;

                    case LONG:
                        if (attributeSize == 3) {
                            if (runningAttributes == 3) {

                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        Object[] objects = (Object[]) obj;
                                        SimpleDateFormat format1 = new SimpleDateFormat((String) objects[1]);
                                        Calendar calender = SimpleDateFormat.getDateInstance().getCalendar();
                                        calender.setTimeInMillis((Long) objects[0]);
                                        return format1.format(calender.getTime());
                                    }
                                };
                            } else {
                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        Calendar calender = SimpleDateFormat.getDateInstance().getCalendar();
                                        calender.setTimeInMillis((Long) obj);
                                        return format1.format(calender.getTime());
                                    }
                                };
                            }
                        } else {
                            typeConverter = new TypeConverter() {
                                @Override
                                public Object convert(Object obj) {
                                    return obj.toString();
                                }
                            };
                        }
                        break;

                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                    default:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                return obj.toString();
                            }
                        };
                        break;
                }


                break;
            case INT:
                switch (variableExecutor.getReturnType()) {
                    case STRING:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    return Integer.parseInt(obj.toString());
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case INT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Integer) {
                                        return obj;
                                    } else {
                                        return Integer.parseInt(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case LONG:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Long) {
                                        return ((Long) obj).intValue();
                                    } else {
                                        return Integer.parseInt(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case FLOAT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Float) {
                                        return ((Float) obj).intValue();
                                    } else {
                                        return Integer.parseInt(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case DOUBLE:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Double) {
                                        return ((Double) obj).intValue();
                                    } else {
                                        return Integer.parseInt(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case BOOL:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Boolean) {
                                        return ((Boolean) obj) ? 1 : 0;
                                    } else {
                                        return Boolean.parseBoolean(obj.toString()) ? 1 : 0;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                }
                break;
            case LONG:
                switch (variableExecutor.getReturnType()) {

                    case STRING:
                        if (attributeSize == 3) {
                            if (runningAttributes == 3) {

                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        Object[] objects = (Object[]) obj;
                                        SimpleDateFormat format1 = new SimpleDateFormat((String) objects[1]);
                                        format1.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        try {
                                            return format1.parse((String) objects[0]).getTime();
                                        } catch (ParseException e) {
                                            return null;
                                        }
                                    }
                                };
                            } else {
                                typeConverter = new TypeConverter() {
                                    @Override
                                    public Object convert(Object obj) {
                                        try {
                                            return format1.parse((String) obj).getTime();
                                        } catch (ParseException e) {
                                            return null;
                                        }
                                    }
                                };
                            }

                        } else {
                            typeConverter = new TypeConverter() {
                                @Override
                                public Object convert(Object obj) {
                                    try {
                                        return Long.parseLong(obj.toString());
                                    } catch (NumberFormatException e) {
                                        return null;
                                    }
                                }
                            };
                        }
                        break;

                    case INT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Integer) {
                                        return ((Integer) obj).longValue();
                                    } else {
                                        return Long.parseLong(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case LONG:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Long) {
                                        return obj;
                                    } else {
                                        return Long.parseLong(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case FLOAT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Float) {
                                        return ((Float) obj).longValue();
                                    } else {
                                        return Long.parseLong(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case DOUBLE:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Double) {
                                        return ((Double) obj).longValue();
                                    } else {
                                        return Long.parseLong(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case BOOL:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Boolean) {
                                        return ((Boolean) obj) ? 1l : 0l;
                                    } else {
                                        return Boolean.parseBoolean(obj.toString()) ? 1l : 0l;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                }
                break;
            case FLOAT:
                switch (variableExecutor.getReturnType()) {

                    case STRING:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    return Float.parseFloat(obj.toString());
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case INT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Integer) {
                                        return ((Integer) obj).floatValue();
                                    } else {
                                        return Float.parseFloat(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case LONG:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Long) {
                                        return ((Long) obj).floatValue();
                                    } else {
                                        return Float.parseFloat(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case FLOAT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Float) {
                                        return obj;
                                    } else {
                                        return Float.parseFloat(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case DOUBLE:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Double) {
                                        return ((Double) obj).floatValue();
                                    } else {
                                        return Float.parseFloat(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case BOOL:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Boolean) {
                                        return ((Boolean) obj) ? 1f : 0f;
                                    } else {
                                        return Boolean.parseBoolean(obj.toString()) ? 1f : 0f;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                }
                break;
            case DOUBLE:
                switch (variableExecutor.getReturnType()) {

                    case STRING:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    return Double.parseDouble(obj.toString());
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case INT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Integer) {
                                        return ((Integer) obj).doubleValue();
                                    } else {
                                        return Double.parseDouble(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case LONG:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Long) {
                                        return ((Long) obj).doubleValue();
                                    } else {
                                        return Double.parseDouble(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case FLOAT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Float) {
                                        return ((Float) obj).doubleValue();
                                    } else {
                                        return Double.parseDouble(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case DOUBLE:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Double) {
                                        return obj;
                                    } else {
                                        return Double.parseDouble(obj.toString());
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case BOOL:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Boolean) {
                                        return ((Boolean) obj) ? 1.0 : 0.0;
                                    } else {
                                        return Boolean.parseBoolean(obj.toString()) ? 1.0 : 0.0;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                }
                break;
            case BOOL:
                switch (variableExecutor.getReturnType()) {

                    case STRING:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    return Boolean.parseBoolean(obj.toString());
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case INT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Integer) {
                                        return ((Integer) obj) == 1;
                                    } else {
                                        return Integer.parseInt(obj.toString()) == 1;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case LONG:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Long) {
                                        return ((Long) obj) == 1l;
                                    } else {
                                        return Long.parseLong(obj.toString()) == 1l;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case FLOAT:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Float) {
                                        return ((Float) obj) == 1f;
                                    } else {
                                        return Float.parseFloat(obj.toString()) == 1f;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case DOUBLE:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Double) {
                                        return ((Double) obj) == 1.0;
                                    } else {
                                        return Double.parseDouble(obj.toString()) == 1.0;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case BOOL:
                        typeConverter = new TypeConverter() {
                            @Override
                            public Object convert(Object obj) {
                                try {
                                    if (obj instanceof Boolean) {
                                        return obj;
                                    } else {
                                        return Boolean.parseBoolean(obj.toString()) ? 1 : 0;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            }
                        };
                        break;
                    case TYPE:
                        throw new UnsupportedOperationException("Type not supported for Conversion ");
                }
                break;
            case TYPE:
                throw new UnsupportedOperationException("Type not supported for Conversion ");
        }

    }

    @Override
    public Object execute(AtomicEvent event) {

        if (runningAttributes == 2) {
            return process(variableExecutor.execute(event));
        } else if (runningAttributes == 3) {
            Object[] objs = new Object[]{variableExecutor.execute(event), format1Expression.execute(event)};
            return process(objs);
        } else if (runningAttributes == 4) {
            Object[] objs = new Object[]{variableExecutor.execute(event),
                                         format1Expression.execute(event),
                                         format2Expression.execute(event)};
            return process(objs);
        }
        return null;
    }

    protected Object process(Object obj) {
        return typeConverter.convert(obj);
    }


    interface TypeConverter {
        Object convert(Object obj);
    }

    @Override
    public void destroy(){

    }
}
