/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.assetconsumer.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Validate that the ReferenceableClassification bean can be cloned, compared, serialized, deserialized and printed as a String.
 */
public class ReferenceableClassificationTest
{
    private Map<String, Object>  classificationProperties = new HashMap<>();

    /**
     * Default constructor
     */
    public ReferenceableClassificationTest()
    {

    }


    /**
     * Set up an example object to test.
     *
     * @return filled in object
     */
    private ReferenceableClassification getTestObject()
    {
        ReferenceableClassification testObject = new ReferenceableClassification();

        testObject.setClassificationName("TestName");
        testObject.setClassificationProperties(classificationProperties);

        return testObject;
    }


    /**
     * Validate that the object that comes out of the test has the same content as the original test object.
     *
     * @param resultObject object returned by the test
     */
    private void validateResultObject(ReferenceableClassification resultObject)
    {
        assertTrue(resultObject.getClassificationProperties().equals(classificationProperties));
        assertTrue(resultObject.getClassificationName().equals("TestName"));
    }


    /**
     * Validate that the object is initialized properly
     */
    @Test public void testNullObject()
    {
        ReferenceableClassification nullObject = new ReferenceableClassification();

        assertTrue(nullObject.getClassificationName() == null);
        assertTrue(nullObject.getClassificationProperties() == null);

        nullObject = new ReferenceableClassification(null);

        assertTrue(nullObject.getClassificationName() == null);
        assertTrue(nullObject.getClassificationProperties() == null);
    }


    /**
     * Validate that 2 different objects with the same content are evaluated as equal.
     * Also that different objects are considered not equal.
     */
    @Test public void testEquals()
    {
        assertFalse(getTestObject().equals(null));
        assertFalse(getTestObject().equals("DummyString"));
        assertTrue(getTestObject().equals(getTestObject()));

        ReferenceableClassification sameObject = getTestObject();
        assertTrue(sameObject.equals(sameObject));

        ReferenceableClassification differentObject = getTestObject();
        differentObject.setClassificationName("Different");
        assertFalse(getTestObject().equals(differentObject));
    }


    /**
     *  Validate that 2 different objects with the same content have the same hash code.
     */
    @Test public void testHashCode()
    {
        assertTrue(getTestObject().hashCode() == getTestObject().hashCode());
    }


    /**
     *  Validate that an object cloned from another object has the same content as the original
     */
    @Test public void testClone()
    {
        validateResultObject(new ReferenceableClassification(getTestObject()));
    }


    /**
     * Validate that an object generated from a JSON String has the same content as the object used to
     * create the JSON String.
     */
    @Test public void testJSON()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        String       jsonString   = null;

        /*
         * This class
         */
        try
        {
            jsonString = objectMapper.writeValueAsString(getTestObject());
        }
        catch (Throwable  exc)
        {
            assertTrue(false, "Exception: " + exc.getMessage());
        }

        try
        {
            validateResultObject(objectMapper.readValue(jsonString, ReferenceableClassification.class));
        }
        catch (Throwable  exc)
        {
            assertTrue(false, "Exception: " + exc.getMessage());
        }

        /*
         * Through superclass
         */
        AssetConsumerElementHeader  propertyBase = getTestObject();

        try
        {
            jsonString = objectMapper.writeValueAsString(propertyBase);
        }
        catch (Throwable  exc)
        {
            assertTrue(false, "Exception: " + exc.getMessage());
        }

        try
        {
            validateResultObject((ReferenceableClassification) objectMapper.readValue(jsonString, AssetConsumerElementHeader.class));
        }
        catch (Throwable  exc)
        {
            assertTrue(false, "Exception: " + exc.getMessage());
        }
    }


    /**
     * Test that toString is overridden.
     */
    @Test public void testToString()
    {
        assertTrue(getTestObject().toString().contains("ReferenceableClassification"));
    }
}
