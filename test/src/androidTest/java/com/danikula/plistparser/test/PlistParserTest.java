package com.danikula.plistparser.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.danikula.plistparser.test.bean.Simple;
import com.danikula.plistparser.test.bean.StringListContainer;
import com.danikula.plistparser.test.bean.User;
import com.danikula.plistparser.PlistObjectParser;
import com.google.api.client.util.ObjectParser;

import android.test.InstrumentationTestCase;

public class PlistParserTest extends InstrumentationTestCase {

    private ObjectParser plistParser = new PlistObjectParser();

    public void testSimple() {
        Simple simpleObject = parse(R.raw.simple, Simple.class);
        assertEquals("Anna", simpleObject.getValue());
    }

    public void testParsingUser() {
        User user = parse(R.raw.user, User.class);

        assertEquals("danik", user.getName());
        assertEquals(null, user.getNotExisted());
        assertEquals(42, user.getAge());
        assertEquals(Integer.valueOf(9999), user.getSalary());
        assertEquals(true, user.isHandsome());
        assertEquals((double)74.5f, user.getWeight());
        assertEquals(getUserBirthday(), user.getBirthday());
        assertEquals(Arrays.asList("htc desire", "nexus 7", ""), user.getDevices());
        assertEquals(Arrays.asList(118, 551, 42), user.getFavoriteNumbers());

        assertEquals("anna", user.getSister().getName());
        assertEquals(26, user.getSister().getAge());
        assertEquals(Integer.valueOf(45632), user.getSister().getSalary());
        assertEquals(true, user.getSister().isHandsome());

        assertEquals("vova", user.getParents().get(0).getName());
        assertEquals("tania", user.getParents().get(1).getName());
    }

    public void testClosing() {
        try {
            Reader plistReader = openTestPlistReader(R.raw.user);
            plistParser.parseAndClose(plistReader, User.class);

            plistReader.read(); // try to read closed reader
            fail("Unable to read closed stream");
        }
        catch (IOException awaited) {
        }
    }

    public void testArray() {
        StringListContainer listContainer = parse(R.raw.array, StringListContainer.class);
        List<String> list = listContainer.getList();
        assertTrue(list != null);
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), "a");
        assertEquals(list.get(1), "b");
        assertEquals(list.get(2), "c");
    }

    private Date getUserBirthday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return dateFormat.parse("1960-10-10T10:10:10");
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T parse(int rawId, Class<T> clazz) {
        try {
            Reader plistReader = openTestPlistReader(rawId);
            return plistParser.parseAndClose(plistReader, clazz);
        }
        catch (IOException e) {
            fail("Unexpected fail!");
            throw new RuntimeException(e); // just for compiler not complain
        }
    }

    private InputStream openTestPlist(int rawId) {
        return getInstrumentation().getContext().getResources().openRawResource(rawId);
    }

    private Reader openTestPlistReader(int rawId) {
        return new InputStreamReader(openTestPlist(rawId));
    }
}
