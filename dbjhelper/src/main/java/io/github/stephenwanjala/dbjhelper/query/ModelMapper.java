package io.github.stephenwanjala.dbjhelper.query;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map; /** Utility class for populating Java models from SQL ResultSet. */
public final class ModelMapper {

    private ModelMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Populates a Java model object from a SQL ResultSet.
     *
     * @param rs    The ResultSet containing the data (already positioned at the desired row).
     * @param clazz The class of the model to populate.
     * @param <T>   The type of the model.
     * @return An instance of the model populated with data from the ResultSet, or null if there is an error.
     * @throws SQLException                 If an error occurs while accessing the ResultSet.
     * @throws ReflectiveOperationException If an error occurs while creating or setting fields on the model object.
     */
    public static <T> T populateModel(ResultSet rs, Class<T> clazz)
            throws SQLException, ReflectiveOperationException {
        if (rs == null) {
            return null;
        }

        T model = createInstance(clazz);
        Map<String, Field> fieldMap = mapFields(clazz);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnLabel = metaData.getColumnLabel(i).toLowerCase(Locale.US);
            String camelCaseLabel = snakeToCamel(columnLabel);

            // Check for field existence
            Field field = fieldMap.getOrDefault(columnLabel, fieldMap.get(camelCaseLabel));
            if (field != null) {
                Object value = rs.getObject(i);
                setFieldValue(model, field, value);
            }
        }

        return model;
    }

    private static <T> T createInstance(Class<T> clazz) throws ReflectiveOperationException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private static <T> Map<String, Field> mapFields(Class<T> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName().toLowerCase(Locale.US);
            fieldMap.put(fieldName, field); // Standard lowercase mapping
            fieldMap.put(camelToSnake(field.getName()), field); // Support snake_case
        }
        return fieldMap;
    }

    private static <T> void setFieldValue(T model, Field field, Object value)
            throws ReflectiveOperationException {
        if (value != null) {
            field.set(model, convertType(value, field.getType()));
        }
    }

    private static Object convertType(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value; // No conversion needed
        }

        if (value == null) {
            return null; // Handle null values explicitly
        }

        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == BigDecimal.class) {
            return value instanceof Number ? BigDecimal.valueOf(((Number) value).doubleValue()) : new BigDecimal(value.toString());
        } else if (targetType == Timestamp.class) {
            return value instanceof java.util.Date ? new Timestamp(((java.util.Date) value).getTime()) : Timestamp.valueOf(value.toString());
        } else if (targetType == java.util.Date.class) {
            return parseDate(value.toString());
        }

        return value; // Default case, return as is
    }

    private static Date parseDate(String dateString) {
        String[] formats = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy"};
        for (String format : formats) {
            try {
                return new SimpleDateFormat(format).parse(dateString);
            } catch (ParseException ignored) {
            }
        }
        return null; // Return null if no format matches
    }

    private static String snakeToCamel(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }

        return result.toString();
    }

    private static String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.US);
    }
}
