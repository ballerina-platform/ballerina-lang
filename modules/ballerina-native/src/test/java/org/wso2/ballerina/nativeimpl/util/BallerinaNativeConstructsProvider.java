package org.wso2.ballerina.nativeimpl.util;

import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.nativeimpl.NativeConstructLoader;
import org.wso2.ballerina.core.nativeimpl.NativeUnitProxy;

public class BallerinaNativeConstructsProvider implements NativeConstructLoader {

public BallerinaNativeConstructsProvider() {}

public void load(SymbolScope globalScope) {
globalScope.define(new SymbolName("copyOf.double[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.DoubleArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.double[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.double[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.double[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.DoubleArrayLength").newInstance());
          nativeCallableUnit.setName("length.double[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.double[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.double[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.DoubleArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.double[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.double[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.float[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.FloatArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.float[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.float[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.float[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.FloatArrayLength").newInstance());
          nativeCallableUnit.setName("length.float[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.float[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.float[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.FloatArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.float[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.float[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.int[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.IntArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.int[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.int[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.int[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.IntArrayLength").newInstance());
          nativeCallableUnit.setName("length.int[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.int[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.int[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.IntArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.int[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.int[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.json[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.JsonArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.json[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.json[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.json[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.JsonArrayLength").newInstance());
          nativeCallableUnit.setName("length.json[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.json[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.json[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.JsonArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.json[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.json[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.long[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.LongArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.long[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.long[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.long[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.LongArrayLength").newInstance());
          nativeCallableUnit.setName("length.long[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.long[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.long[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.LongArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.long[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.long[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.message[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.MessageArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.message[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.message[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.message[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.MessageArrayLength").newInstance());
          nativeCallableUnit.setName("length.message[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.message[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.message[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.MessageArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.message[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.message[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.string[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.StringArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.string[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.string[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.string[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.StringArrayLength").newInstance());
          nativeCallableUnit.setName("length.string[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.string[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.string[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.StringArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.string[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.string[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOf.xml[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.XmlArrayCopyOf").newInstance());
          nativeCallableUnit.setName("copyOf.xml[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOf.xml[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.xml[]","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.XmlArrayLength").newInstance());
          nativeCallableUnit.setName("length.xml[]");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", true)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.xml[]","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("copyOfRange.xml[].int.int","ballerina.lang.array"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.array.XmlArrayRangeCopy").newInstance());
          nativeCallableUnit.setName("copyOfRange.xml[].int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.array");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", true), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", true)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("copyOfRange.xml[].int.int","ballerina.lang.array"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.boolean","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddBooleanToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.boolean","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.boolean","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddBooleanToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.boolean","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.double","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddDoubleToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.double","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.double","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddDoubleToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.double","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.float","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddFloatToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.float","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.float","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddFloatToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.float","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.int","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddIntToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.int","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.int","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddIntToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.int","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.json","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddJSONToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.json","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.json","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddJSONToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.json","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddStringToArray").newInstance());
          nativeCallableUnit.setName("add.json.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("add.json.string.string.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.AddStringToObject").newInstance());
          nativeCallableUnit.setName("add.json.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("add.json.string.string.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getBoolean.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetBoolean").newInstance());
          nativeCallableUnit.setName("getBoolean.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getBoolean.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getDouble.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetDouble").newInstance());
          nativeCallableUnit.setName("getDouble.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getDouble.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getFloat.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetFloat").newInstance());
          nativeCallableUnit.setName("getFloat.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getFloat.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getInt.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetInt").newInstance());
          nativeCallableUnit.setName("getInt.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getInt.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getJson.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetJSON").newInstance());
          nativeCallableUnit.setName("getJson.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getJson.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getString.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.GetString").newInstance());
          nativeCallableUnit.setName("getString.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getString.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("remove.json.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.Remove").newInstance());
          nativeCallableUnit.setName("remove.json.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("remove.json.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("rename.json.string.string.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.Rename").newInstance());
          nativeCallableUnit.setName("rename.json.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("rename.json.string.string.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.boolean","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetBoolean").newInstance());
          nativeCallableUnit.setName("set.json.string.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.boolean","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.double","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetDouble").newInstance());
          nativeCallableUnit.setName("set.json.string.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.double","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.float","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetFloat").newInstance());
          nativeCallableUnit.setName("set.json.string.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.float","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.int","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetInt").newInstance());
          nativeCallableUnit.setName("set.json.string.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.int","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.json","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetJSON").newInstance());
          nativeCallableUnit.setName("set.json.string.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.json","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.json.string.string","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.SetString").newInstance());
          nativeCallableUnit.setName("set.json.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.json.string.string","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("toString.json","ballerina.lang.json"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.json.ToString").newInstance());
          nativeCallableUnit.setName("toString.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.json");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("toString.json","ballerina.lang.json"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("keys.map","ballerina.lang.map"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.map.GetKeys").newInstance());
          nativeCallableUnit.setName("keys.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.map");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("keys.map","ballerina.lang.map"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.map","ballerina.lang.map"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.map.Length").newInstance());
          nativeCallableUnit.setName("length.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.map");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.map","ballerina.lang.map"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("remove.map.string","ballerina.lang.map"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.map.Remove").newInstance());
          nativeCallableUnit.setName("remove.map.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.map");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("map", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("remove.map.string","ballerina.lang.map"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("addHeader.message.string.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.AddHeader").newInstance());
          nativeCallableUnit.setName("addHeader.message.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("addHeader.message.string.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("clone.message","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.Clone").newInstance());
          nativeCallableUnit.setName("clone.message");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("clone.message","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getHeader.message.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.GetHeader").newInstance());
          nativeCallableUnit.setName("getHeader.message.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getHeader.message.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getHeaders.message.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.GetHeaders").newInstance());
          nativeCallableUnit.setName("getHeaders.message.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", true)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getHeaders.message.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getJsonPayload.message","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.GetJsonPayload").newInstance());
          nativeCallableUnit.setName("getJsonPayload.message");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getJsonPayload.message","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getStringPayload.message","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.GetStringPayload").newInstance());
          nativeCallableUnit.setName("getStringPayload.message");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getStringPayload.message","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getXmlPayload.message","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.GetXMLPayload").newInstance());
          nativeCallableUnit.setName("getXmlPayload.message");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getXmlPayload.message","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("removeHeader.message.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.RemoveHeader").newInstance());
          nativeCallableUnit.setName("removeHeader.message.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("removeHeader.message.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setHeader.message.string.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.SetHeader").newInstance());
          nativeCallableUnit.setName("setHeader.message.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("setHeader.message.string.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setJsonPayload.message.json","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.SetJsonPayload").newInstance());
          nativeCallableUnit.setName("setJsonPayload.message.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setJsonPayload.message.json","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setStringPayload.message.string","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.SetStringPayload").newInstance());
          nativeCallableUnit.setName("setStringPayload.message.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setStringPayload.message.string","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setXmlPayload.message.xml","ballerina.lang.message"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.message.SetXMLPayload").newInstance());
          nativeCallableUnit.setName("setXmlPayload.message.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.message");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setXmlPayload.message.xml","ballerina.lang.message"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.boolean","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.BooleanValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.boolean","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("contains.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.Contains").newInstance());
          nativeCallableUnit.setName("contains.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("contains.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.double","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.DoubleValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.double","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("equalsIgnoreCase.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.EqualsIgnoreCase").newInstance());
          nativeCallableUnit.setName("equalsIgnoreCase.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("equalsIgnoreCase.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.float","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.FloatValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.float","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("hasPrefix.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.HasPrefix").newInstance());
          nativeCallableUnit.setName("hasPrefix.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("hasPrefix.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("hasSuffix.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.HasSuffix").newInstance());
          nativeCallableUnit.setName("hasSuffix.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("hasSuffix.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("indexOf.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.IndexOf").newInstance());
          nativeCallableUnit.setName("indexOf.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("indexOf.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.int","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.IntValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.int","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.json","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.JsonValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.json","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("lastIndexOf.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.LastIndexOf").newInstance());
          nativeCallableUnit.setName("lastIndexOf.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("lastIndexOf.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("length.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.Length").newInstance());
          nativeCallableUnit.setName("length.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("length.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.long","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.LongValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.long");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.long","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("replace.string.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.Replace").newInstance());
          nativeCallableUnit.setName("replace.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("replace.string.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("replaceAll.string.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.ReplaceAll").newInstance());
          nativeCallableUnit.setName("replaceAll.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("replaceAll.string.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("replaceFirst.string.string.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.ReplaceFirst").newInstance());
          nativeCallableUnit.setName("replaceFirst.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("replaceFirst.string.string.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.StringValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("subString.string.int.int","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.SubString").newInstance());
          nativeCallableUnit.setName("subString.string.int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("subString.string.int.int","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("toLowerCase.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.ToLowerCase").newInstance());
          nativeCallableUnit.setName("toLowerCase.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("toLowerCase.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("toUpperCase.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.ToUpperCase").newInstance());
          nativeCallableUnit.setName("toUpperCase.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("toUpperCase.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("trim.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.Trim").newInstance());
          nativeCallableUnit.setName("trim.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("trim.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("unescape.string","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.Unescape").newInstance());
          nativeCallableUnit.setName("unescape.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("unescape.string","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("valueOf.xml","ballerina.lang.string"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.string.XmlValueOf").newInstance());
          nativeCallableUnit.setName("valueOf.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.string");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("valueOf.xml","ballerina.lang.string"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("currentTimeMillis","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.CurrentTimeMillis").newInstance());
          nativeCallableUnit.setName("currentTimeMillis");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setStackFrameSize(0);
          nativeCallableUnit.setSymbolName(new SymbolName("currentTimeMillis","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("epochTime","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.EpochTime").newInstance());
          nativeCallableUnit.setName("epochTime");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setStackFrameSize(0);
          nativeCallableUnit.setSymbolName(new SymbolName("epochTime","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.boolean","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogBoolean").newInstance());
          nativeCallableUnit.setName("log.int.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.boolean","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.double","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogDouble").newInstance());
          nativeCallableUnit.setName("log.int.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.double","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.float","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogFloat").newInstance());
          nativeCallableUnit.setName("log.int.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.float","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.int","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogInt").newInstance());
          nativeCallableUnit.setName("log.int.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.int","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.long","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogLong").newInstance());
          nativeCallableUnit.setName("log.int.long");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("long", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.long","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("log.int.string","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.LogString").newInstance());
          nativeCallableUnit.setName("log.int.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("log.int.string","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("nanoTime","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.NanoTime").newInstance());
          nativeCallableUnit.setName("nanoTime");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setStackFrameSize(0);
          nativeCallableUnit.setSymbolName(new SymbolName("nanoTime","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.boolean","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintBoolean").newInstance());
          nativeCallableUnit.setName("print.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.boolean","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.double","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintDouble").newInstance());
          nativeCallableUnit.setName("print.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.double","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.float","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintFloat").newInstance());
          nativeCallableUnit.setName("print.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.float","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.int","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintInt").newInstance());
          nativeCallableUnit.setName("print.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.int","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.json","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintJSON").newInstance());
          nativeCallableUnit.setName("print.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.json","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.boolean","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnBoolean").newInstance());
          nativeCallableUnit.setName("println.boolean");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("boolean", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.boolean","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.double","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnDouble").newInstance());
          nativeCallableUnit.setName("println.double");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("double", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.double","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.float","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnFloat").newInstance());
          nativeCallableUnit.setName("println.float");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("float", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.float","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.int","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnInt").newInstance());
          nativeCallableUnit.setName("println.int");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.int","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.json","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnJSON").newInstance());
          nativeCallableUnit.setName("println.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.json","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.long","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnLong").newInstance());
          nativeCallableUnit.setName("println.long");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.long","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.string","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnString").newInstance());
          nativeCallableUnit.setName("println.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.string","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("println.xml","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintlnXML").newInstance());
          nativeCallableUnit.setName("println.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("println.xml","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.long","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintLong").newInstance());
          nativeCallableUnit.setName("print.long");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("long", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.long","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.string","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintString").newInstance());
          nativeCallableUnit.setName("print.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.string","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("print.xml","ballerina.lang.system"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.system.PrintXML").newInstance());
          nativeCallableUnit.setName("print.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.system");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("print.xml","ballerina.lang.system"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("addAttribute.xml.string.string.string","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.AddAttribute").newInstance());
          nativeCallableUnit.setName("addAttribute.xml.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("addAttribute.xml.string.string.string","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("addAttribute.xml.string.string.string.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.AddAttributeWithNamespaces").newInstance());
          nativeCallableUnit.setName("addAttribute.xml.string.string.string.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(5);
          nativeCallableUnit.setSymbolName(new SymbolName("addAttribute.xml.string.string.string.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("addElement.xml.string.xml","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.AddElement").newInstance());
          nativeCallableUnit.setName("addElement.xml.string.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("addElement.xml.string.xml","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("addElement.xml.string.xml.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.AddElementWithNamespaces").newInstance());
          nativeCallableUnit.setName("addElement.xml.string.xml.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("xml", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("addElement.xml.string.xml.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getString.xml.string","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.GetString").newInstance());
          nativeCallableUnit.setName("getString.xml.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getString.xml.string","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getString.xml.string.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.GetStringWithNamespaces").newInstance());
          nativeCallableUnit.setName("getString.xml.string.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("getString.xml.string.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getXml.xml.string","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.GetXML").newInstance());
          nativeCallableUnit.setName("getXml.xml.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getXml.xml.string","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getXml.xml.string.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.GetXMLWithNamespaces").newInstance());
          nativeCallableUnit.setName("getXml.xml.string.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("getXml.xml.string.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("remove.xml.string","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.Remove").newInstance());
          nativeCallableUnit.setName("remove.xml.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("remove.xml.string","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("remove.xml.string.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.RemoveWithNamespaces").newInstance());
          nativeCallableUnit.setName("remove.xml.string.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("remove.xml.string.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.xml.string.string","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.SetString").newInstance());
          nativeCallableUnit.setName("set.xml.string.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.xml.string.string","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.xml.string.string.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.SetStringWithNamespaces").newInstance());
          nativeCallableUnit.setName("set.xml.string.string.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("set.xml.string.string.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.xml.string.xml","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.SetXML").newInstance());
          nativeCallableUnit.setName("set.xml.string.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("set.xml.string.xml","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("set.xml.string.xml.map","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.SetXMLWithNamespaces").newInstance());
          nativeCallableUnit.setName("set.xml.string.xml.map");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false), new SimpleTypeName("string", false), new SimpleTypeName("xml", false), new SimpleTypeName("map", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("set.xml.string.xml.map","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("toString.xml","ballerina.lang.xml"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.xml.ToString").newInstance());
          nativeCallableUnit.setName("toString.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.xml");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("toString.xml","ballerina.lang.xml"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("acceptAndReturn.int","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.AcceptAndReturn").newInstance());
          nativeCallableUnit.setName("acceptAndReturn.int");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("acceptAndReturn.int","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("convertToResponse.message","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.ConvertToResponse").newInstance());
          nativeCallableUnit.setName("convertToResponse.message");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("convertToResponse.message","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getContentLength.message","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.GetContentLength").newInstance());
          nativeCallableUnit.setName("getContentLength.message");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getContentLength.message","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getMethod.message","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.GetMethod").newInstance());
          nativeCallableUnit.setName("getMethod.message");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getMethod.message","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getStatusCode.message","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.GetStatusCode").newInstance());
          nativeCallableUnit.setName("getStatusCode.message");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("int", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("getStatusCode.message","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setContentLength.message.int","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.SetContentLength").newInstance());
          nativeCallableUnit.setName("setContentLength.message.int");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setContentLength.message.int","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setReasonPhrase.message.string","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.SetReasonPhrase").newInstance());
          nativeCallableUnit.setName("setReasonPhrase.message.string");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setReasonPhrase.message.string","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("setStatusCode.message.int","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.http.SetStatusCode").newInstance());
          nativeCallableUnit.setName("setStatusCode.message.int");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("int", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("setStatusCode.message.int","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("encode.string","ballerina.net.uri"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.uri.Encode").newInstance());
          nativeCallableUnit.setName("encode.string");
          nativeCallableUnit.setPackagePath("ballerina.net.uri");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("encode.string","ballerina.net.uri"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getQueryParam.message.string","ballerina.net.uri"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.net.uri.GetQueryParam").newInstance());
          nativeCallableUnit.setName("getQueryParam.message.string");
          nativeCallableUnit.setPackagePath("ballerina.net.uri");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("message", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(2);
          nativeCallableUnit.setSymbolName(new SymbolName("getQueryParam.message.string","ballerina.net.uri"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("base64decode.string","ballerina.util"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.util.Base64Decode").newInstance());
          nativeCallableUnit.setName("base64decode.string");
          nativeCallableUnit.setPackagePath("ballerina.util");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("base64decode.string","ballerina.util"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("base64encode.string","ballerina.util"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.util.Base64Encode").newInstance());
          nativeCallableUnit.setName("base64encode.string");
          nativeCallableUnit.setPackagePath("ballerina.util");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("base64encode.string","ballerina.util"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getHmac.string.string.string","ballerina.util"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.util.GetHmac").newInstance());
          nativeCallableUnit.setName("getHmac.string.string.string");
          nativeCallableUnit.setPackagePath("ballerina.util");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("getHmac.string.string.string","ballerina.util"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("getRandomString","ballerina.util"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.util.GetRandomString").newInstance());
          nativeCallableUnit.setName("getRandomString");
          nativeCallableUnit.setPackagePath("ballerina.util");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(0);
          nativeCallableUnit.setSymbolName(new SymbolName("getRandomString","ballerina.util"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.HTTPConnector").newInstance());
          nativeCallableUnit.setName("HTTPConnector");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:delete","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Delete").newInstance());
          nativeCallableUnit.setName("HTTPConnector:delete");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:delete","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:execute","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Execute").newInstance());
          nativeCallableUnit.setName("HTTPConnector:execute");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(4);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:execute","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:get","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Get").newInstance());
          nativeCallableUnit.setName("HTTPConnector:get");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:get","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:head","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Head").newInstance());
          nativeCallableUnit.setName("HTTPConnector:head");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:head","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:patch","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Patch").newInstance());
          nativeCallableUnit.setName("HTTPConnector:patch");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:patch","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:post","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Post").newInstance());
          nativeCallableUnit.setName("HTTPConnector:post");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:post","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName("HTTPConnector:put","ballerina.net.http"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.connectors.http.Put").newInstance());
          nativeCallableUnit.setName("HTTPConnector:put");
          nativeCallableUnit.setPackagePath("ballerina.net.http");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("connector", false), new SimpleTypeName("string", false), new SimpleTypeName("message", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{});
          nativeCallableUnit.setStackFrameSize(3);
          nativeCallableUnit.setSymbolName(new SymbolName("HTTPConnector:put","ballerina.net.http"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".json->.string","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.JSONToString").newInstance());
          nativeCallableUnit.setName(".json->.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".json->.string","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".json->.xml","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.JSONToXML").newInstance());
          nativeCallableUnit.setName(".json->.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".json->.xml","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".string->.json","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.StringToJSON").newInstance());
          nativeCallableUnit.setName(".string->.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".string->.json","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".string->.xml","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.StringToXML").newInstance());
          nativeCallableUnit.setName(".string->.xml");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".string->.xml","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".xml->.json","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.XMLToJSON").newInstance());
          nativeCallableUnit.setName(".xml->.json");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("json", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".xml->.json","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

globalScope.define(new SymbolName(".xml->.string","ballerina.lang.convertors"),
  new NativeUnitProxy(() -> {
      NativeUnit nativeCallableUnit = null;
      try {
          nativeCallableUnit = ((NativeUnit) Class.forName("org.wso2.ballerina.nativeimpl.lang.convertors.XMLToString").newInstance());
          nativeCallableUnit.setName(".xml->.string");
          nativeCallableUnit.setPackagePath("ballerina.lang.convertors");
          nativeCallableUnit.setArgTypeNames(new SimpleTypeName[]{new SimpleTypeName("xml", false)});
          nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[]{new SimpleTypeName("string", false)});
          nativeCallableUnit.setStackFrameSize(1);
          nativeCallableUnit.setSymbolName(new SymbolName(".xml->.string","ballerina.lang.convertors"));
          return nativeCallableUnit;
      } catch (Exception ignore) {
      } finally {
          return nativeCallableUnit;
      }
  })
);

}
}
