/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.subjectarea;

/**
 * The SubjectAreaDefinition Open Metadata Access Service (OMAS).
 */
public interface SubjectArea
{
    /**
     * Get the subject area API class
     * @return subject area glossary API class
     */
   SubjectAreaGlossary getSubjectAreaGlossary();
    /**
     * Get the subject area term API class - use this class to issue term calls.
     * @return subject area term API class
     */
   SubjectAreaTerm getSubjectAreaTerm();
    /**
     * Get the subject area category API class - use this class to issue category calls.
     * @return subject area category API class
     */
   SubjectAreaCategory getSubjectAreaCategory();
    /**
     * Get the subject area relationship API class - use this class to issue relationship calls.
     * @return subject area relationship API class
     */
   SubjectAreaRelationship getSubjectAreaRelationship();
    /**
     * Get the subject area graph API class - use this class to issue graph calls.
     * @return subject area graph API class
     */
   SubjectAreaGraph getSubjectAreaGraph();
}