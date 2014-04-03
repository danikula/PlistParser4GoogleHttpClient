package com.danikula.plistparser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* package-private */class Tags {

    static final String PLIST = "plist";

    static final String KEY = "key";

    static final String ARRAY = "array";
    static final String DICT = "dict";
    static final String STRING = "string";
    static final String INTEGER = "integer";
    static final String REAL = "real";
    static final String DATA = "data";
    static final String DATE = "date";
    static final String TRUE = "true";
    static final String FALSE = "false";

    static boolean isDictionary(String tag) {
        return DICT.equalsIgnoreCase(tag);
    }

    static boolean isArray(String tag) {
        return ARRAY.equalsIgnoreCase(tag);
    }

    static boolean isString(String tag) {
        return STRING.equalsIgnoreCase(tag);
    }

    static boolean isInteger(String tag) {
        return INTEGER.equalsIgnoreCase(tag);
    }

    static boolean isReal(String tag) {
        return REAL.equalsIgnoreCase(tag);
    }

    static boolean isTrueTag(String tag) {
        return TRUE.equalsIgnoreCase(tag);
    }

    static boolean isFalseTag(String tag) {
        return FALSE.equalsIgnoreCase(tag);
    }

    static boolean isBoolean(String tag) {
        return isTrueTag(tag) || isFalseTag(tag);
    }

    static boolean isDate(String tag) {
        return DATE.equalsIgnoreCase(tag);
    }

    static boolean isData(String tag) {
        return DATA.equalsIgnoreCase(tag);
    }

    static boolean isKey(String tag) {
        return KEY.equalsIgnoreCase(tag);
    }

    static boolean isValidPlistType(String value) {
        return isEqualsAny(value, ARRAY, DATA, DATE, DICT, REAL, INTEGER, STRING, TRUE, FALSE);
    }

    static boolean isKey(XmlPullParser parser) {
        return isKey(parser.getName());
    }

    static boolean isStartTag(XmlPullParser parser) throws XmlPullParserException {
        return isStartTag(parser.getEventType());
    }

    static boolean isTextNode(int eventType) {
        return XmlPullParser.TEXT == eventType;
    }

    static boolean isStartTag(int eventType) {
        return XmlPullParser.START_TAG == eventType;
    }

    static boolean isEndTag(int eventType) {
        return XmlPullParser.END_TAG == eventType;
    }

    private static boolean isEqualsAny(String value, String... expectedTags) {
        for (String expectedTag : expectedTags) {
            if (expectedTag.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

}
