/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.framework.check.transformer;

import java.util.concurrent.Callable;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.StateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * This class implements an abstract base class for all transformers.
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class AbstractTransformer implements Callable<IHashGroupify> {

    /**
     * Implementation of the delegate for Requirements.COUNTER
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounter implements IGroupify {

        @Override
        public final void callAll(final int[] outtuple, final int i) {
            groupify.addAll(outtuple, i, 1, -1, -1);
        }

        @Override
        public final void callGroupify(final int[] outtuple, final HashGroupifyEntry element) {
            groupify.addGroupify(outtuple, element.representant, element.count, null, -1);
        }

        @Override
        public final void callSnapshot(final int[] outtuple, final int[] snapshot, final int i) {
            groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], null, null, -1);
        }
    }

    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterDistribution implements IGroupify {
        @Override
        public final void callAll(final int[] outtuple, final int i) {
            groupify.addAll(outtuple, i, 1, sensitiveValues[i], -1);
        }

        @Override
        public final void callGroupify(final int[] outtuple, final HashGroupifyEntry element) {
            groupify.addGroupify(outtuple, element.representant, element.count, element.distribution, -1);
        }

        @Override
        public final void callSnapshot(final int[] outtuple, final int[] snapshot, final int i) {
            groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 2]), dictionarySensFreq.get(snapshot[i + 3]), -1);
        }
    }

    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.SECONDARY_COUNTER
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterSecondaryCounter implements IGroupify {
        @Override
        public final void callAll(final int[] outtuple, final int i) {
            groupify.addAll(outtuple, i, 1, -1, 1);
        }

        @Override
        public final void callGroupify(final int[] outtuple, final HashGroupifyEntry element) {
            groupify.addGroupify(outtuple, element.representant, element.count, null, element.pcount);
        }

        @Override
        public final void callSnapshot(final int[] outtuple, final int[] snapshot, final int i) {
            groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], null, null, snapshot[i + 2]);
        }
    }


    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.SECONDARY_COUNTER | Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterSecondaryCounterDistribution implements IGroupify {
        @Override
        public final void callAll(final int[] outtuple, final int i) {
            groupify.addAll(outtuple, i, 1, sensitiveValues[i], 1);
        }

        @Override
        public final void callGroupify(final int[] outtuple, final HashGroupifyEntry element) {
            groupify.addGroupify(outtuple, element.representant, element.count, element.distribution, element.pcount);
        }

        @Override
        public final void callSnapshot(final int[] outtuple, final int[] snapshot, final int i) {
            groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 3]), dictionarySensFreq.get(snapshot[i + 4]), snapshot[i + 2]);
        }
    }


    /**
     * Implementation of the delegate for Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyDistribution implements IGroupify {
        @Override
        public final void callAll(final int[] outtuple, final int i) {
            groupify.addAll(outtuple, i, 1, sensitiveValues[i], -1);
        }

        @Override
        public final void callGroupify(final int[] outtuple, final HashGroupifyEntry element) {
            groupify.addGroupify(outtuple, element.representant, element.count, element.distribution, -1);
        }

        @Override
        public final void callSnapshot(final int[] outtuple, final int[] snapshot, final int i) {
            groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 2]), dictionarySensFreq.get(snapshot[i + 3]), -1);
        }
    }

    /**
     * Interface for delegates to the groupify 
     * @author Kohlmayer, Prasser
     *
     */
    protected interface IGroupify {
        
        /** Mode ALL*/
        public abstract void callAll(final int[] outtuple, final int i);

        /** Mode GROUPIFY*/
        public abstract void callGroupify(final int[] outtuple, final HashGroupifyEntry element);

        /** Mode SNAPSHOT*/
        public abstract void callSnapshot(final int[] outtuple, final int[] snapshot, final int i);
    }

    /** The hash groupify*/
    private IHashGroupify                     groupify;

    /** The bucket. */
    protected int                             bucket;

    /** The buffer. */
    protected int[][]                         buffer;

    /** The column index array. */
    protected final int[]                     columnIndexArray;

    /** The column map array. */
    protected final int[][][]                 columnMapArray;

    /** The mode of operation **/
    protected final ARXConfiguration          config;

    /** The data. */
    protected final int[][]                   data;

    /** The delegate*/
    protected final IGroupify                       delegate;

    /** The dictionary for the snapshot compression **/
    protected final IntArrayDictionary        dictionarySensFreq;

    /** The dictionary for the snapshot compression **/
    protected final IntArrayDictionary        dictionarySensValue;

    /** The dimensions. */
    protected final int                       dimensions;

    /** The element. */
    protected HashGroupifyEntry               element;
    /** The groupify array. */
    protected HashGroupifyEntry[]             groupifyArray;
    /** The hierarchies. */
    protected final GeneralizationHierarchy[] hierarchies;
    /** The idindex14. */
    protected int[][]                         idindex0, idindex1, idindex2, idindex3, idindex4, idindex5, idindex6, idindex7, idindex8, idindex9, idindex10, idindex11, idindex12, idindex13,
            idindex14;
    /** The index14. */
    protected int                             index0, index1, index2, index3, index4, index5, index6, index7, index8, index9, index10, index11, index12, index13, index14;
    /** The intuple. */
    protected int[]                           intuple;
    /** The generalization hierarchies */
    protected int[][][]                       map;
    /** The num elements. */
    protected int                             numElements;
    /** The outindices. */
    protected int                             outindex0;
    /** The outindices. */
    protected int                             outindex1;
    /** The outindices. */
    protected int                             outindex10;
    /** The outindices. */
    protected int                             outindex11;
    /** The outindices. */
    protected int                             outindex12;
    /** The outindices. */
    protected int                             outindex13;
    /** The outindices. */
    protected int                             outindex14;

    /** The outindices. */
    protected int                             outindex2;

    /** The outindices. */
    protected int                             outindex3;

    /** The outindices. */
    protected int                             outindex4;

    /** The outindices. */
    protected int                             outindex5;

    /** The outindices. */
    protected int                             outindex6;
    /** The outindices. */
    protected int                             outindex7;
    /** The outindices. */
    protected int                             outindex8;
    /** The outindices. */
    protected int                             outindex9;
    /** The outtuple. */
    protected int[]                           outtuple;
    /** The sesitive values. */
    protected final int[]                     sensitiveValues;
    /** The snapshot. */
    protected int[]                           snapshot;
    /** The size of one snapshopt entry **/
    protected final int                       ssStepWidth;
    /** The start index. */
    protected int                             startIndex;
    /** The stateindices */
    protected int                             stateindex0;
    /** The stateindices */
    protected int                             stateindex1;
    /** The stateindices */
    protected int                             stateindex10;
    /** The stateindices */
    protected int                             stateindex11;
    /** The stateindices */
    protected int                             stateindex12;
    /** The stateindices */
    protected int                             stateindex13;

    /** The stateindices */
    protected int                             stateindex14;
    /** The stateindices */
    protected int                             stateindex2;
    /** The stateindices */
    protected int                             stateindex3;
    /** The stateindices */
    protected int                             stateindex4;

    /** The stateindices */
    protected int                             stateindex5;

    /** The stateindices */
    protected int                             stateindex6;

    /** The stateindices */
    protected int                             stateindex7;

    /** The stateindices */
    protected int                             stateindex8;

    /** The stateindices */
    protected int                             stateindex9;

    /** The state index array. */
    protected final int[]                     stateIndexArray;

    /** The states. */
    protected int[]                           states;

    /** The stop index. */
    protected int                             stopIndex;

    /** The transition. */
    protected TransitionType                  transition;

    /**
     * Instantiates a new abstract transformer.
     * 
     * @param data
     *            the data
     * @param hierarchies
     *            the hierarchies
     */
    public AbstractTransformer(final int[][] data,
                               final GeneralizationHierarchy[] hierarchies,
                               final int[] sensitiveValues,
                               final IntArrayDictionary dictionarySensValue,
                               final IntArrayDictionary dictionarySensFreq,
                               final ARXConfiguration config) {
        this.config = config;
        this.data = data;
        this.hierarchies = hierarchies;
        this.sensitiveValues = sensitiveValues;
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        ssStepWidth = config.getSnapshotLength();

        // Init arrays
        dimensions = data[0].length;
        int arraySizes = 15;
        if (dimensions > arraySizes) {
            arraySizes = dimensions;
        }
        stateIndexArray = new int[arraySizes];
        columnIndexArray = new int[arraySizes];
        columnMapArray = new int[arraySizes][][];
        map = new int[hierarchies.length][][];
        for (int i = 0; i < hierarchies.length; i++) {
            map[i] = hierarchies[i].getArray();
        }

        // Prepare delegate
        switch (config.getRequirements()) {
        case ARXConfiguration.REQUIREMENT_COUNTER:
            delegate = new GroupifyCounter();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
            delegate = new GroupifyCounterSecondaryCounter();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyCounterSecondaryCounterDistribution();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyCounterDistribution();
            break;
        case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyDistribution();
            break;
        default:
            RuntimeException e = new RuntimeException("Invalid requirements: " + config.getRequirements());
            throw(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public IHashGroupify call() {
        // clear local groupify
        groupify.clear();

        switch (transition) {
        case UNOPTIMIZED:
            processAll();
            break;
        case ROLLUP:
            processGroupify();
            break;
        case SNAPSHOT:
            processSnapshot();
            break;

        default:
            break;
        }
        return groupify;
    }

    /**
     * Inits the.
     * 
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param groupify
     *            the groupify
     * @param source
     *            the source
     * @param snapshot
     *            the snapshot
     * @param transition
     *            the transition
     * @param startIndex
     *            the start index
     * @param stopIndex
     *            the stop index
     * @param bucket
     *            the bucket
     * @param element
     *            the element
     * @param buffer
     *            the buffer
     */
    public void init(final long projection,
                     final int[] state,
                     final IHashGroupify groupify,
                     final IHashGroupify source,
                     final int[] snapshot,
                     final TransitionType transition,
                     final int startIndex,
                     final int stopIndex,
                     final int bucket,
                     final HashGroupifyEntry element,
                     final int[][] buffer) {

        this.buffer = buffer;

        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.element = element;
        this.bucket = bucket;
        numElements = stopIndex - startIndex;

        states = state;
        this.transition = transition;

        int index = 0;
        for (int i = 0; i < dimensions; i++) {
            if ((projection & (1L << i)) == 0) {
                stateIndexArray[index] = state[i];
                columnIndexArray[index] = i;
                columnMapArray[index] = hierarchies[i].getArray();
                index++;
            }
        }

        // Store groupify
        this.groupify = groupify;
        // this.groupify.clear();

        // Store snapshot
        this.snapshot = snapshot;

        // Store values
        index0 = columnIndexArray[0];
        index1 = columnIndexArray[1];
        index2 = columnIndexArray[2];
        index3 = columnIndexArray[3];
        index4 = columnIndexArray[4];
        index5 = columnIndexArray[5];
        index6 = columnIndexArray[6];
        index7 = columnIndexArray[7];
        index8 = columnIndexArray[8];
        index9 = columnIndexArray[9];
        index10 = columnIndexArray[10];
        index11 = columnIndexArray[11];
        index12 = columnIndexArray[12];
        index13 = columnIndexArray[13];
        index14 = columnIndexArray[14];

        // Store values
        outindex0 = columnIndexArray[0];
        outindex1 = columnIndexArray[1];
        outindex2 = columnIndexArray[2];
        outindex3 = columnIndexArray[3];
        outindex4 = columnIndexArray[4];
        outindex5 = columnIndexArray[5];
        outindex6 = columnIndexArray[6];
        outindex7 = columnIndexArray[7];
        outindex8 = columnIndexArray[8];
        outindex9 = columnIndexArray[9];
        outindex10 = columnIndexArray[10];
        outindex11 = columnIndexArray[11];
        outindex12 = columnIndexArray[12];
        outindex13 = columnIndexArray[13];
        outindex14 = columnIndexArray[14];

        // Store values
        stateindex0 = stateIndexArray[0];
        stateindex1 = stateIndexArray[1];
        stateindex2 = stateIndexArray[2];
        stateindex3 = stateIndexArray[3];
        stateindex4 = stateIndexArray[4];
        stateindex5 = stateIndexArray[5];
        stateindex6 = stateIndexArray[6];
        stateindex7 = stateIndexArray[7];
        stateindex8 = stateIndexArray[8];
        stateindex9 = stateIndexArray[9];
        stateindex10 = stateIndexArray[10];
        stateindex11 = stateIndexArray[11];
        stateindex12 = stateIndexArray[12];
        stateindex13 = stateIndexArray[13];
        stateindex14 = stateIndexArray[14];

        // Store values
        idindex0 = columnMapArray[0];
        idindex1 = columnMapArray[1];
        idindex2 = columnMapArray[2];
        idindex3 = columnMapArray[3];
        idindex4 = columnMapArray[4];
        idindex5 = columnMapArray[5];
        idindex6 = columnMapArray[6];
        idindex7 = columnMapArray[7];
        idindex8 = columnMapArray[8];
        idindex9 = columnMapArray[9];
        idindex10 = columnMapArray[10];
        idindex11 = columnMapArray[11];
        idindex12 = columnMapArray[12];
        idindex13 = columnMapArray[13];
        idindex14 = columnMapArray[14];

    }

    /**
     * Update out indices.
     * 
     * @param activecolumns
     *            the activecolumns
     * @param projection
     *            the projection
     */
    public void updateOutIndices(final int[] activecolumns, final long projection) {

        // Make sure all 15 indices are set
        final int[] outindices2 = new int[15];
        int outcount = 0;
        for (int i = 0; i < activecolumns.length; i++) {
            if ((projection & (1L << activecolumns[i])) == 0) {
                outindices2[outcount++] = i;
            }
        }

        // Copy
        outindex0 = outindices2[0];
        outindex1 = outindices2[1];
        outindex2 = outindices2[2];
        outindex3 = outindices2[3];
        outindex4 = outindices2[4];
        outindex5 = outindices2[5];
        outindex6 = outindices2[6];
        outindex7 = outindices2[7];
        outindex8 = outindices2[8];
        outindex9 = outindices2[9];
        outindex10 = outindices2[10];
        outindex11 = outindices2[11];
        outindex12 = outindices2[12];
        outindex13 = outindices2[13];
        outindex14 = outindices2[14];
    }

    /**
     * Process complete input dataset.
     */
    protected abstract void processAll();

    /**
     * Process groupify.
     */
    protected abstract void processGroupify();

    /**
     * Process snapshot.
     */
    protected abstract void processSnapshot();

}
