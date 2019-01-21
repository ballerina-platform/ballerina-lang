/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.LockableStructureType;
import org.ballerinalang.util.BLangConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The {@code GlobalMemoryBlock} represents the global memory block in Ballerina VM.
 *
 * @since 1.0.0
 */

public final class GlobalMemoryBlock implements BRefType, LockableStructureType {
    private long[] longFields;
    private VarLock[] longLocks;
    private double[] doubleFields;
    private VarLock[] doubleLocks;
    private String[] stringFields;
    private VarLock[] stringLocks;
    private int[] intFields;
    private VarLock[] intLocks;
    private BRefType[] refFields;
    private VarLock[] refLocks;

    private BStructureType structType;

    /**
     * Creates a GlobalMemoryBlock.
     *
     * @param structType type of the struct
     */
    public GlobalMemoryBlock(BStructureType structType) {
        this.structType = structType;

        int[] fieldCount = this.structType.getFieldTypeCount();
        longFields = new long[fieldCount[0]];
        doubleFields = new double[fieldCount[1]];
        stringFields = new String[fieldCount[2]];
        intFields = new int[fieldCount[3]];
        refFields = new BRefType[fieldCount[4]];

        Arrays.fill(stringFields, BLangConstants.STRING_EMPTY_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalMemoryBlock value() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BStructureType getType() {
        return structType;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {
        
    }

    @Override
    public long getIntField(int index) {
        return longFields[index];
    }

    @Override
    public void setIntField(int index, long value) {
        longFields[index] = value;
    }

    @Override
    public double getFloatField(int index) {
        return doubleFields[index];
    }

    @Override
    public void setFloatField(int index, double value) {
        doubleFields[index] = value;
    }

    @Override
    public String getStringField(int index) {
        return stringFields[index];
    }

    @Override
    public void setStringField(int index, String value) {
        stringFields[index] = value;
    }

    @Override
    public int getBooleanField(int index) {
        return intFields[index];
    }

    @Override
    public void setBooleanField(int index, int value) {
        intFields[index] = value;
    }

    @Override
    public BRefType getRefField(int index) {
        return refFields[index];
    }

    @Override
    public void setRefField(int index, BRefType value) {
        refFields[index] = value;
    }

    @Override
    public boolean lockIntField(Strand ctx, int index) {
        /*
        TODO below synchronization is done on non final variable(which is getting changed in copy method)
        This is ok for the time being as below synchronizations are only valid for global memory block which is
        not getting copied, even in that case there shouldn't be a problem as synchronization always happens after
        copying, but look into that when implementing locking support for struct fields and connector variables.
         */
        if (longLocks == null) {
            synchronized (longFields) {
                if (longLocks == null) {
                    longLocks = new VarLock[longFields.length];
                }
            }
        }
        if (longLocks[index] == null) {
            //locking the whole field array
            synchronized (longFields) {
                if (longLocks[index] == null) {
                    longLocks[index] = new VarLock();
                }
            }
        }
        return longLocks[index].lock(ctx);
    }

    @Override
    public void unlockIntField(int index) {
        longLocks[index].unlock();
    }

    @Override
    public boolean lockFloatField(Strand ctx, int index) {
        /*
        TODO below synchronization is done on non final variable(which is getting changed in copy method)
        This is ok for the time being as below synchronizations are only valid for global memory block which is
        not getting copied, even in that case there shouldn't be a problem as synchronization always happens after
        copying, but look into that when implementing locking support for struct fields and connector variables.
         */
        if (doubleLocks == null) {
            synchronized (doubleFields) {
                if (doubleLocks == null) {
                    doubleLocks = new VarLock[doubleFields.length];
                }
            }
        }
        if (doubleLocks[index] == null) {
            //locking the whole field array
            synchronized (doubleFields) {
                if (doubleLocks[index] == null) {
                    doubleLocks[index] = new VarLock();
                }
            }
        }
        return doubleLocks[index].lock(ctx);
    }

    @Override
    public void unlockFloatField(int index) {
        doubleLocks[index].unlock();
    }

    @Override
    public boolean lockStringField(Strand ctx, int index) {
        /*
        TODO below synchronization is done on non final variable(which is getting changed in copy method)
        This is ok for the time being as below synchronizations are only valid for global memory block which is
        not getting copied, even in that case there shouldn't be a problem as synchronization always happens after
        copying, but look into that when implementing locking support for struct fields and connector variables.
         */
        if (stringLocks == null) {
            synchronized (stringFields) {
                if (stringLocks == null) {
                    stringLocks = new VarLock[stringFields.length];
                }
            }
        }
        if (stringLocks[index] == null) {
            synchronized (stringFields) {
                if (stringLocks[index] == null) {
                    stringLocks[index] = new VarLock();
                }
            }
        }
        return stringLocks[index].lock(ctx);
    }

    @Override
    public void unlockStringField(int index) {
        stringLocks[index].unlock();
    }

    @Override
    public boolean lockBooleanField(Strand ctx, int index) {
        /*
        TODO below synchronization is done on non final variable(which is getting changed in copy method)
        This is ok for the time being as below synchronizations are only valid for global memory block which is
        not getting copied, even in that case there shouldn't be a problem as synchronization always happens after
        copying, but look into that when implementing locking support for struct fields and connector variables.
         */
        if (intLocks == null) {
            synchronized (intFields) {
                if (intLocks == null) {
                    intLocks = new VarLock[intFields.length];
                }
            }
        }
        if (intLocks[index] == null) {
            synchronized (intFields) {
                if (intLocks[index] == null) {
                    intLocks[index] = new VarLock();
                }
            }
        }
        return intLocks[index].lock(ctx);
    }

    @Override
    public void unlockBooleanField(int index) {
        intLocks[index].unlock();
    }

    @Override
    public boolean lockRefField(Strand ctx, int index) {
        /*
        TODO below synchronization is done on non final variable(which is getting changed in copy method)
        This is ok for the time being as below synchronizations are only valid for global memory block which is
        not getting copied, even in that case there shouldn't be a problem as synchronization always happens after
        copying, but look into that when implementing locking support for struct fields and connector variables.
         */
        if (refLocks == null) {
            synchronized (refFields) {
                if (refLocks == null) {
                    refLocks = new VarLock[refFields.length];
                }
            }
        }
        if (refLocks[index] == null) {
            synchronized (refFields) {
                if (refLocks[index] == null) {
                    refLocks[index] = new VarLock();
                }
            }
        }
        return refLocks[index].lock(ctx);
    }

    @Override
    public void unlockRefField(int index) {
        refLocks[index].unlock();
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        GlobalMemoryBlock bStruct = new GlobalMemoryBlock(structType);
        bStruct.longFields = Arrays.copyOf(longFields, longFields.length);
        bStruct.doubleFields = Arrays.copyOf(doubleFields, doubleFields.length);
        bStruct.stringFields = Arrays.copyOf(stringFields, stringFields.length);
        bStruct.intFields = Arrays.copyOf(intFields, intFields.length);
        bStruct.refFields = Arrays.copyOf(refFields, refFields.length);
        return bStruct;
    }
    
    @Override
    public String toString() {
        return this.stringValue();
    }
}
