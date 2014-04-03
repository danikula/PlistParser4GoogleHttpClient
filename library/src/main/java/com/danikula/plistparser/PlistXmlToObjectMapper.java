package com.danikula.plistparser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Types;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Xml;

/* package-private */class PlistXmlToObjectMapper {

    private SimpleDateFormat ISO_8601_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private XmlHelper xmlHelper = new XmlHelper();

    public Object convertPlistStreamToObject(InputStream inputStream, Type type) throws IOException, PlistParseException {
        try {
            if (!(type instanceof Class)) {
                throw new PlistParseException(String.format("%s is not instance of Class", type));
            }
            Class<?> resultClass = (Class<?>) type;

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();

            Object result = convertXmlToObject(parser, resultClass);
            return result;
        }
        catch (XmlPullParserException e) {
            throw new PlistParseException("Error parsing plist resource", e);
        }
    }

    private Object convertXmlToObject(XmlPullParser parser, Class<?> resultClass) throws XmlPullParserException, IOException,
        PlistParseException {
        parser.require(XmlPullParser.START_TAG, null, Tags.PLIST);
        int eventType = xmlHelper.nextTag(parser);
        if (Tags.isEndTag(eventType)) {
            return null;
        }
        String tag = parser.getName();
        if (Tags.isDictionary(tag)) {
            return parseDictionary(parser, resultClass);
        }
        else if (Tags.isArray(tag)) {
            return rootArrayToList(parser, resultClass);
        }
        else {
            throw new PlistParseException("Tag plist must contain only tag 'array' or 'dict', but not " + tag);
        }
    }

    // специальная обработка для тех случаев, когда чайлдом плиста является массив.
    // В этом случае нету возможности получить тип элементов листа.
    // Потому лист необходимо завернуть в ListContainer, а тип элементов листа получаем из дженерик параметра супрекласса.
    private Object rootArrayToList(XmlPullParser parser, Class<?> listContainerClass) throws PlistParseException,
        XmlPullParserException, IOException {
        Class<?> arrayEntryClass = getArrayEntryClass(listContainerClass);
        Object listContainer = Types.newInstance(listContainerClass);
        FieldInfo listFieldInfo = getListFieldFromListContainer(listContainerClass, arrayEntryClass);
        List list = parseArray(parser, arrayEntryClass);
        listFieldInfo.setValue(listContainer, list);
        return listContainer;
    }

    private Class getArrayEntryClass(Class<?> arrayContainerClass) throws PlistParseException {
        if (!ListContainer.class.isAssignableFrom(arrayContainerClass)) {
            throw new IllegalStateException(
                    "Class surrounding root array should implement ListContainer<T> where T is type of list entry!");
        }
        Type[] classInterfaces = arrayContainerClass.getGenericInterfaces();
        for (Type classInterface : classInterfaces) {
            if (classInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) classInterface;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    Class<?> rawTypeClass = (Class<?>) rawType;
                    if (ListContainer.class.isAssignableFrom(rawTypeClass)) {
                        Type fieldListEntryType = parameterizedType.getActualTypeArguments()[0];
                        return (Class<?>) fieldListEntryType;
                    }
                }
            }
        }
        throw new IllegalStateException(
                "Class surrounding root array should implement ListContainer<T> where T is type of list entry");
    }

    private FieldInfo getListFieldFromListContainer(Class<?> listContainerClass, Class<?> arrayEntryClass)
        throws PlistParseException {
        ClassInfo listContainerClassInfo = ClassInfo.of(listContainerClass);
        Collection<String> fieldNames = listContainerClassInfo.getNames();
        for (String fieldName : fieldNames) {
            FieldInfo fieldInfo = listContainerClassInfo.getFieldInfo(fieldName);
            Class<?> fieldClass = fieldInfo.getType();
            if (List.class.isAssignableFrom(fieldClass)) {
                Class<?> firstGenericType = getFirstGenericType(fieldInfo);
                if (arrayEntryClass.isAssignableFrom(firstGenericType)) {
                    return fieldInfo;
                }
            }
        }
        String error = String.format("There is no field with class List<'%s'> in class %s", arrayEntryClass, listContainerClass);
        throw new PlistParseException(error);
    }

    private Object parseDictionary(XmlPullParser parser, Class<?> resultClass) throws XmlPullParserException, IOException,
        PlistParseException {
        Object resultObject = Types.newInstance(resultClass);
        ClassInfo resultObjectClassInfo = ClassInfo.of(resultClass);
        int dictionaryDepth = parser.getDepth();
        xmlHelper.nextTag(parser);
        while (dictionaryDepth != parser.getDepth()) {
            xmlHelper.checkStartTag(parser);
            int propertyKeyDepth = parser.getDepth();
            String propertyName = xmlHelper.getTextValueOfElement(parser);
            FieldInfo fieldInfo = resultObjectClassInfo.getFieldInfo(propertyName);
            boolean isFieldPresented = fieldInfo != null;
            if (!isFieldPresented) {
                xmlHelper.nextTag(parser);
                xmlHelper.skipWholeTag(parser);
                boolean isPropertyEndTag = parser.getDepth() == propertyKeyDepth;
                if (isPropertyEndTag) {
                    xmlHelper.nextTag(parser);
                    continue;
                }
                else {// end dictionary tag
                    break;
                }
            }
            int keyTagDepth = parser.getDepth();
            xmlHelper.nextTag(parser);
            if (keyTagDepth != parser.getDepth()) {
                throw new PlistParseException("Value tag and key tag should be on the same depth");
            }
            Class<?> fieldClass = fieldInfo.getType();
            if (List.class.isAssignableFrom(fieldClass)) {
                fieldClass = getFirstGenericType(fieldInfo);
            }
            Object propertyValue = parseValueTag(parser, fieldClass);
            setPropertyValue(resultObject, fieldInfo, propertyValue);

            xmlHelper.nextTag(parser);
        }
        return resultObject;
    }

    private Class<?> getFirstGenericType(FieldInfo fieldInfo) {
        ParameterizedType parameterizedFieldType = (ParameterizedType) fieldInfo.getField().getGenericType();
        return (Class<?>) parameterizedFieldType.getActualTypeArguments()[0];
    }

    private void setPropertyValue(Object resultObject, FieldInfo fieldInfo, Object value) throws PlistParseException {
        Class<?> fieldClass = fieldInfo.getType();
        // handle primitive types
        if (fieldClass == int.class) {
            checkFieldType(fieldInfo, value, int.class);
            int intValue = ((Integer) value).intValue();
            fieldInfo.setValue(resultObject, intValue);
        }
        else if (fieldClass == double.class) {
            checkFieldType(fieldInfo, value, double.class);
            double doubleValue = ((Double) value).doubleValue();
            fieldInfo.setValue(resultObject, doubleValue);
        }
        else if (fieldClass == boolean.class) {
            checkFieldType(fieldInfo, value, boolean.class);
            boolean booleanValue = ((Boolean) value).booleanValue();
            fieldInfo.setValue(resultObject, booleanValue);
        }
        else { // objects
            checkFieldType(fieldInfo, value, value.getClass());
            fieldInfo.setValue(resultObject, value);
        }
    }

    // если разбираемое значение - массив, то valueClass должен содержать тип элементов массива
    private Object parseValueTag(XmlPullParser parser, Class<?> valueClass) throws XmlPullParserException, IOException,
        PlistParseException {
        String type = parser.getName();
        String valueAsString = null;
        try {
            if (Tags.isString(type)) {
                return xmlHelper.getTextValueOfElement(parser);
            }
            else if (Tags.isInteger(type)) {
                valueAsString = xmlHelper.getTextValueOfElement(parser);
                return Integer.parseInt(valueAsString);
            }
            else if (Tags.isReal(type)) {
                valueAsString = xmlHelper.getTextValueOfElement(parser);
                return TextUtils.isEmpty(valueAsString.trim()) ? Double.NaN : Double.parseDouble(valueAsString);
            }
            else if (Tags.isBoolean(type)) {
                boolean booleanValue = Tags.isTrueTag(parser.getName());
                xmlHelper.nextTag(parser);
                return booleanValue;
            }
            else if (Tags.isDate(type)) {
                valueAsString = xmlHelper.getTextValueOfElement(parser);
                return ISO_8601_DATE_FORMATTER.parse(valueAsString);
            }
            else if (Tags.isData(type)) {
                valueAsString = xmlHelper.getTextValueOfElement(parser);
                byte[] bytes = valueAsString.getBytes();
                return Base64.encode(bytes, Base64.DEFAULT);
            }
            else if (Tags.isArray(type)) {
                // если разбираемое значение - массив, то valueClass должен содержать тип элементов массива
                return parseArray(parser, valueClass);
            }
            else if (Tags.isDictionary(type)) {
                return parseDictionary(parser, valueClass);
            }
            else {
                String error = String.format("Unexpected type '%s'. Only plist types are permitted!", type);
                throw new PlistParseException(error);
            }
        }
        catch (NumberFormatException e) {
            throw new PlistParseException(String.format("Error formatting value '%s' to number!", valueAsString));
        }
        catch (ParseException e) {
            throw new PlistParseException(String.format("Error formatting value '%s' to date!", valueAsString));
        }
    }

    private List<?> parseArray(XmlPullParser parser, Class<?> expectedEntryClass) throws XmlPullParserException, IOException,
        PlistParseException {
        int arrayDepth = parser.getDepth();
        xmlHelper.nextTag(parser);
        List resultList = Lists.newArrayList();
        while (arrayDepth != parser.getDepth()) {
            xmlHelper.checkStartTag(parser);
            Object arrayEntry = parseValueTag(parser, expectedEntryClass);
            Class<?> entryClass = arrayEntry.getClass();
            if (!expectedEntryClass.isAssignableFrom(entryClass)) {
                String errorFormat = "Array's entry has incorrect type: expected '%s' but actual: '%s'";
                String error = String.format(errorFormat, expectedEntryClass, entryClass);
                throw new PlistParseException(error);
            }
            resultList.add(arrayEntry);
            xmlHelper.nextTag(parser);
        }
        return resultList;
    }

    private void checkFieldType(FieldInfo fieldInfo, Object value, Class<?> expectedClass) throws PlistParseException {
        if (!fieldInfo.getType().isAssignableFrom(expectedClass)) {
            String fieldClassName = fieldInfo.getType().getName();
            String valueClassName = expectedClass.getName();
            String targetObjectClass = fieldInfo.getField().getDeclaringClass().getName();
            String fieldName = fieldInfo.getName();
            String errorFormat = "Can't set value '%s' (%s) to field '%s.%s' (%s)";
            String errorMessage = String.format(errorFormat, value, valueClassName, targetObjectClass, fieldName, fieldClassName);
            throw new PlistParseException(errorMessage);
        }
    }

}
