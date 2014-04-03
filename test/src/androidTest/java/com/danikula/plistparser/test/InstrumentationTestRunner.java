/**
 * Licensed under Creative Commons Attribution 3.0 Unported license.
 * http://creativecommons.org/licenses/by/3.0/
 * You are free to copy, distribute and transmit the work, and 
 * to adapt the work.  You must attribute android-plist-parser 
 * to Free Beachler (http://www.freebeachler.com).
 * 
 * The Android PList parser (android-plist-parser) is distributed in 
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.
 */
package com.danikula.plistparser.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.TestSuite;

public class InstrumentationTestRunner extends android.test.InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        return new TestSuiteBuilder(InstrumentationTestRunner.class)
                .includePackages("com.danikula.plistparser.test")
                .build();
    }

    @Override
    public ClassLoader getLoader() {
        return InstrumentationTestRunner.class.getClassLoader();
    }

}
