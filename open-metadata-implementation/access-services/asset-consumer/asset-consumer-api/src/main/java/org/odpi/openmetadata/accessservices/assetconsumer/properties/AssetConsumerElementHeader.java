/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.assetconsumer.properties;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

/**
 * AssetConsumerElementHeader provides a common base for all instance information from the access service.
 * It implements Serializable.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Tag.class, name = "Tag"),
        @JsonSubTypes.Type(value = ReferenceableHeader.class, name = "ReferenceableHeader"),
        @JsonSubTypes.Type(value = ReferenceableClassification.class, name = "ReferenceableClassification")
})
public abstract class AssetConsumerElementHeader implements Serializable
{
    private static final long serialVersionUID = 1L;


    /**
     * Default Constructor sets the properties to nulls
     */
    public AssetConsumerElementHeader()
    {
        /*
         * Nothing to do.
         */
    }


    /**
     * Copy/clone constructor set values from the template
     *
     * @param template object to copy
     */
    public AssetConsumerElementHeader(AssetConsumerElementHeader template)
    {
        /*
         * Nothing to do.
         */
    }
}
