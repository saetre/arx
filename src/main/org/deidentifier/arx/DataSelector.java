package org.deidentifier.arx;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataType.ARXDate;

import de.linearbits.objectselector.IAccessor;
import de.linearbits.objectselector.Selector;
import de.linearbits.objectselector.SelectorBuilder;
import de.linearbits.objectselector.datatypes.DataType;

/**
 * A selector for tuples
 * @author Prasser, Kohlmayer
 *
 */
public class DataSelector {
    
    /**
     * An accessor for data elements
     * @author Fabian Prasser
     */
    private class DataAccessor implements IAccessor<Integer>{

        /** The data handle*/
        private final DataHandle handle;
        /** The data definition*/
        private final DataDefinition definition;
        /** The data types*/
        private final Map<String, DataType<?>> types;
        /** The indices*/
        private final Map<String, Integer> indices;
        
        /**
         * Creates a new instance
         * @param data
         */
        protected DataAccessor(Data data){
            this.handle = data.getHandle();
            this.definition = data.getDefinition();
            this.types = getTypes();
            this.indices = getIndices();
        }
        
        /**
         * Returns the data types
         * @return
         */
        private Map<String, DataType<?>> getTypes() {
            Map<String, DataType<?>> result = new HashMap<String, DataType<?>>();
            for (int i=0; i<handle.getNumColumns(); i++){
                String attribute = handle.getAttributeName(i);
                org.deidentifier.arx.DataType<?> type = definition.getDataType(attribute);
                if (type == org.deidentifier.arx.DataType.DECIMAL) {
                    result.put(attribute, DataType.NUMERIC);
                } else if (type == org.deidentifier.arx.DataType.STRING) {
                    result.put(attribute, DataType.STRING);                    
                } else if (type instanceof org.deidentifier.arx.DataType.ARXDate){
                    String format = ((ARXDate)type).getFormat();
                    result.put(attribute, DataType.DATE(format));
                } else {
                    result.put(attribute, DataType.STRING);
                }
            }
            return result;
        }

        /**
         * Returns the indices
         * @return
         */
        private Map<String, Integer> getIndices() {
            Map<String, Integer> result = new HashMap<String, Integer>();
            for (int i=0; i<handle.getNumColumns(); i++){
                result.put(handle.getAttributeName(i), i);
            }
            return result;
        }

        @Override
        public boolean exists(String arg0) {
            return indices.containsKey(arg0);
        }

        @Override
        public DataType<?> getType(String arg0) {
            return types.get(arg0);
        }

        @Override
        public Object getValue(Integer arg0, String arg1) {
            
            int column = indices.get(arg1);
            DataType<?> type = types.get(arg1);
            String value = handle.getValue(arg0, column);
            return type.fromString(value);
        }

        @Override
        public boolean isDataTypesSupported() {
            return true;
        }

        @Override
        public boolean isExistanceSupported() {
            return true;
        }        
    }
    
    /** The builder*/
    private final SelectorBuilder<Integer> builder;
    /** The selector*/
    private Selector<Integer> selector = null;
   
    private DataSelector(Data data){
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data)); 
    }

    private DataSelector(Data data, String query) throws ParseException {
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data), query);
    }

    public static DataSelector create(Data data){
        return new DataSelector(data);
    }

    public static DataSelector create(Data data, String query) throws ParseException{
        return new DataSelector(data, query);
    }
    
    public DataSelector field(String name){
        this.builder.field(name);
        return this;
    }
    
    public DataSelector and(){
        this.builder.and();
        return this;
    }
    
    public DataSelector or(){
        this.builder.or();
        return this;
    }
    
    public DataSelector begin(){
        this.builder.begin();
        return this;
    }
    
    public DataSelector end(){
        this.builder.end();
        return this;
    }
    
    /*
     * NUMERIC
     */
    public DataSelector leq(final double val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final double val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final double val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final double val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final double val){
        this.builder.equals(val);
        return this;
    }
    
    /* **************************************
     * STRING
     * **************************************/
    public DataSelector leq(final String val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final String val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final String val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final String val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final String val){
        this.builder.equals(val);
        return this;
    }

    /* **************************************
     * Datetime
     * **************************************/
    public DataSelector leq(final Date val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final Date val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final Date val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final Date val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final Date val){
        this.builder.equals(val);
        return this;
    }
    
    public void build() throws ParseException{
        this.selector = this.builder.build();
    }

    /**
     * Determines whether the given row is selected by the expression
     * @param row
     * @return
     */
    public boolean isSelected(int row){
        if (selector == null) {
            try {
                build();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return selector.isSelected(row);
    }
}
