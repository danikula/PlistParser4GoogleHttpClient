package com.danikula.plistparser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

/* package-private */class XmlHelper {

    void checkStartTag(XmlPullParser parser) throws XmlPullParserException, PlistParseException {
        if (!Tags.isStartTag(parser)) {
            throw new PlistParseException("Parser's state is not START_TAG: " + getParserDescription(parser));
        }
    }

    String getParserDescription(XmlPullParser parser) throws XmlPullParserException {
        return String.format("event type: %d, name: '%s', text: '%s', line: %d", parser.getEventType(), parser.getName(),
                parser.getText(), parser.getLineNumber());
    }

    String getTextValueOfElement(XmlPullParser parser) throws XmlPullParserException, IOException, PlistParseException {
        parser.require(START_TAG, null, null);
        String tagName = parser.getName();
        int eventType = parser.next();
        if (Tags.isEndTag(eventType)) {
            return "";
        }
        else if (!Tags.isTextNode(eventType)) {
            throw new PlistParseException(String.format("Tag '%s' doesn't contain direct text node", tagName));
        }
        String textValue = parser.getText();
        parser.next();
        parser.require(END_TAG, null, null);
        return textValue;
    }

    int nextTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType;
        do {
            eventType = parser.next();
        } while (eventType != END_TAG && eventType != START_TAG);
        return eventType;
    }

    void skipWholeTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (!Tags.isStartTag(parser)) {
            throw new IllegalStateException("It is not start of tag!");
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
