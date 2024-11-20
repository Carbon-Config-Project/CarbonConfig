package carbonconfiglib.gui.impl.forge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ForgeDataType
{
    private static final Map<Class<?>, ForgeDataType> DATA_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    
    // Ajuste nas funções de parse com casting
    public static final ForgeDataType BOOLEAN = new ForgeDataType(
        Boolean.class, 
        DataType.BOOLEAN, 
        input -> (ParseResult<Object>) (ParseResult<?>) ForgeHelpers.parseBoolean(input), 
        Object::toString, 
        null
    );
    
    public static final ForgeDataType INTEGER = new ForgeDataType(
        Integer.class, 
        DataType.INTEGER, 
        input -> (ParseResult<Object>) (ParseResult<?>) Helpers.parseInt(input), 
        Object::toString, 
        ForgeHelpers::getIntLimit
    );
    
    public static final ForgeDataType LONG = new ForgeDataType(
        Long.class, 
        DataType.INTEGER, 
        input -> (ParseResult<Object>) (ParseResult<?>) ForgeHelpers.parseLong(input), 
        Object::toString, 
        ForgeHelpers::getLongLimit
    );
    
    public static final ForgeDataType FLOAT = new ForgeDataType(
        Float.class, 
        DataType.DOUBLE, 
        input -> (ParseResult<Object>) (ParseResult<?>) ForgeHelpers.parseFloat(input), 
        Object::toString, 
        ForgeHelpers::getFloatLimit
    );
    
    public static final ForgeDataType DOUBLE = new ForgeDataType(
        Double.class, 
        DataType.DOUBLE, 
        input -> (ParseResult<Object>) (ParseResult<?>) Helpers.parseDouble(input), 
        Object::toString, 
        ForgeHelpers::getDoubleLimit
    );
    
    public static final ForgeDataType STRING = new ForgeDataType(
        String.class, 
        DataType.STRING, 
        input -> (ParseResult<Object>) (ParseResult<?>) ForgeHelpers.parseString(input), 
        obj -> (String)obj,  // Substituição de Function.identity()
        null
    );
    
    DataType type;
    Function<String, ParseResult<Object>> parse;
    Function<Object, String> serialize;
    Function<Object[], String> rangeInfo;
    
    @SuppressWarnings("unchecked")
    ForgeDataType(Class<?> clz, DataType type, Function<String, ParseResult<Object>> parse, Function<Object, String> serialize, Function<Object[], String> rangeInfo) {
        this(type, parse, serialize, rangeInfo);
        DATA_TYPES.putIfAbsent(clz, this);
    }
    
    public ForgeDataType(DataType type, Function<String, ParseResult<Object>> parse, Function<Object, String> serialize, Function<Object[], String> rangeInfo) {
        this.type = type;
        this.parse = parse;
        this.serialize = serialize;
        this.rangeInfo = rangeInfo;
    }
    
    public DataType getDataType() {
        return type;
    }
    
    public boolean isEnum() {
        return false;
    }
    
    public String getLimitations(ValueSpec spec) {
        if(rangeInfo == null) return "";
        Object[] data = ForgeHelpers.getRangeInfo(spec);
        return data == null ? "" : rangeInfo.apply(data);
    }
    
    public ParseResult<Object> parse(String input) {
        return parse.apply(input);
    }
    
    public String serialize(Object value) {
        return serialize.apply(value);
    }
    
    public static void registerDataType(Class<?> clz, ForgeDataType type) {
        DATA_TYPES.putIfAbsent(clz, type);
    }
    
    public static ForgeDataType getDataByType(Class<?> clz) {
        if(clz == null) return null;
        ForgeDataType type = DATA_TYPES.get(clz);
        return type == null && clz.isEnum() ? EnumDataType.create(clz) : type;
    }
    
    public static class EnumDataType extends ForgeDataType {
        Class<? extends Enum> clz;
        
        EnumDataType(Class<? extends Enum> clz) {
            super(clz, DataType.ENUM, null, null, null);
            this.clz = clz;
        }
        
        @Override
        public boolean isEnum() { return true; }
        
        @Override
        public ParseResult<Object> parse(String input) {
            try { 
                return ParseResult.success(Enum.valueOf(clz, input)); 
            }
            catch (Exception e) { 
                return ParseResult.error(input, e, "Value must be one of the following: "+Arrays.toString(toArray(null))); 
            }
        }
        
        @Override
        public String getLimitations(ValueSpec spec) {
            return "Value must be one of the following: "+Arrays.toString(toArray(spec));
        }
        
        public List<Suggestion> getSuggestions(ValueSpec spec) {
            List<Suggestion> result = new ObjectArrayList<>();
            ISuggestionProvider.enums(clz).provideSuggestions(result::add, enumConstant -> spec.test(enumConstant.getValue()));
            return result;
        }
        
        @Override
        public String serialize(Object value) {
            return ((Enum<?>) value).name();
        }
        
        @SuppressWarnings({"rawtypes", "unchecked" })
        public static ForgeDataType create(Class<?> clz) {
            return new EnumDataType(clz.asSubclass(Enum.class));
        }
        
        private String[] toArray(ValueSpec spec) {
            Enum[] array = clz.getEnumConstants();
            List<String> values = new ObjectArrayList<>();
            for(int i = 0, m = array.length; i < m; i++) {
                if(spec == null || spec.test(array[i])) {
                    values.add(array[i].name());
                }
            }
            return values.toArray(new String[values.size()]);
        }
    }
}
