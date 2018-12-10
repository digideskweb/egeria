/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.repositoryservices.igc.repositoryconnector.mapping;

import org.apache.commons.collections.map.ReferenceMap;
import org.odpi.openmetadata.adapters.repositoryservices.igc.clientlibrary.IGCRestClient;
import org.odpi.openmetadata.adapters.repositoryservices.igc.clientlibrary.model.common.MainObject;
import org.odpi.openmetadata.adapters.repositoryservices.igc.clientlibrary.model.common.Reference;
import org.odpi.openmetadata.adapters.repositoryservices.igc.repositoryconnector.IGCOMRSMetadataCollection;
import org.odpi.openmetadata.adapters.repositoryservices.igc.repositoryconnector.IGCOMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.SequencingOrder;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.PrimitiveDef;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.PrimitiveDefCategory;
import org.odpi.openmetadata.repositoryservices.ffdc.OMRSErrorCode;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeErrorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Handles mapping the most basic of IGC objects' attributes to OMRS entity attributes.
 */
public abstract class ReferenceMapper {

    public static final String SOURCE_NAME = "IGC";
    public static final String IGC_REST_COMMON_MODEL_PKG = "org.odpi.openmetadata.adapters.repositoryservices.igc.clientlibrary.model.common";
    public static final String IGC_REST_GENERATED_MODEL_PKG = "org.odpi.openmetadata.adapters.repositoryservices.igc.clientlibrary.model.generated";

    protected IGCOMRSRepositoryConnector igcomrsRepositoryConnector;
    protected Reference me;

    protected String igcType;
    protected String igcPOJO;
    protected String omrsType;
    protected String userId;

    protected ArrayList<String> otherPOJOs;

    protected EntitySummary summary;
    protected EntityDetail detail;
    protected ArrayList<Relationship> relationships;
    protected ArrayList<Classification> classifications;
    protected ArrayList<String> alreadyMappedProperties;

    public ReferenceMapper(Reference me,
                           String igcAssetTypeName,
                           String omrsEntityTypeName,
                           IGCOMRSRepositoryConnector igcomrsRepositoryConnector,
                           String userId) {

        this.me = me;
        this.igcType = igcAssetTypeName;
        this.omrsType = omrsEntityTypeName;
        this.igcomrsRepositoryConnector = igcomrsRepositoryConnector;
        this.userId = userId;
        this.relationships = new ArrayList<>();
        this.alreadyMappedProperties = new ArrayList<>();

        this.otherPOJOs = new ArrayList<>();

        // fully-qualified POJO class path
        if (igcAssetTypeName.equals("main_object")) {
            this.igcPOJO = IGC_REST_COMMON_MODEL_PKG + ".MainObject";
        } else {
            this.igcPOJO = IGC_REST_GENERATED_MODEL_PKG + "."
                    + igcomrsRepositoryConnector.getIGCVersion() + "."
                    + ReferenceMapper.getCamelCase(igcAssetTypeName);
        }

    }

    protected EntitySummary getSummary() { return this.summary; }
    protected EntityDetail getDetail() { return this.detail; }
    protected List<Relationship> getRelationships() { return this.relationships; }
    protected List<Classification> getClassifications() { return this.classifications; }
    protected List<String> getAlreadyMappedProperties() { return this.alreadyMappedProperties; }
    protected void addAlreadyMappedProperty(String propertyName) { this.alreadyMappedProperties.add(propertyName); }
    protected void addAlreadyMappedProperties(List<String> propertyNames) { this.alreadyMappedProperties.addAll(propertyNames); }

    public void addOtherIGCAssetType(String igcAssetTypeName) {
        this.otherPOJOs.add(IGC_REST_GENERATED_MODEL_PKG + "."
                + igcomrsRepositoryConnector.getIGCVersion() + "."
                + ReferenceMapper.getCamelCase(igcAssetTypeName));
    }

    public List<String> getOtherIGCAssetTypes() { return this.otherPOJOs; }

    public abstract EntitySummary getOMRSEntitySummary();
    public abstract EntityDetail getOMRSEntityDetail();
    public abstract List<Relationship> getOMRSRelationships(String          relationshipTypeGUID,
                                                            int             fromRelationshipElement,
                                                            SequencingOrder sequencingOrder,
                                                            int             pageSize);

    /**
     * Sets up the private 'classifications' member with all classifications for this object;
     * this method should be overridden if there are any implementation-specific classifications that need
     * to be catered for.
     */
    protected abstract void getMappedClassifications();

    protected void mapIGCToOMRSEntitySummary(List<String> classificationProperies) {
        if (summary == null) {
            summary = new EntitySummary();
            summary.setGUID(me.getId());
            summary.setInstanceURL(me.getUrl());
        }
    }

    protected void mapIGCToOMRSEntityDetail(PropertyMappingSet mappings, List<String> classificationProperties) {
        if (detail == null) {
            try {
                detail = igcomrsRepositoryConnector.getRepositoryHelper().getSkeletonEntity(
                        SOURCE_NAME,
                        igcomrsRepositoryConnector.getMetadataCollectionId(),
                        InstanceProvenanceType.LOCAL_COHORT,
                        userId,
                        omrsType
                );
            } catch (TypeErrorException e) {
                e.printStackTrace();
            }
            detail.setStatus(InstanceStatus.ACTIVE);
            detail.setGUID(me.getId());
            detail.setInstanceURL(me.getUrl());
        }
    }

    protected void complexPropertyMappings(InstanceProperties instanceProperties, Method getPropertyByName) { }
    protected void complexRelationshipMappings() { }

    /**
     * Returns the OMRS PrimitivePropertyValue represented by the provided value.
     *
     * @param value the value to represent as an OMRS PrimitivePropertyValue
     * @return PrimitivePropertyValue
     */
    public static PrimitivePropertyValue getPrimitivePropertyValue(Object value) {
        PrimitivePropertyValue propertyValue = new PrimitivePropertyValue();
        PrimitiveDef primitiveDef = new PrimitiveDef();
        if (value instanceof Boolean) {
            primitiveDef.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN);
        } else if (value instanceof Date) {
            primitiveDef.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE);
        } else if (value instanceof Integer) {
            primitiveDef.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT);
        } else if (value instanceof Number) {
            primitiveDef.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_FLOAT);
        } else {
            primitiveDef.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING);
        }
        propertyValue.setPrimitiveValue(value);
        propertyValue.setPrimitiveDefCategory(primitiveDef.getPrimitiveDefCategory());
        propertyValue.setTypeGUID(primitiveDef.getGUID());
        propertyValue.setTypeName(primitiveDef.getName());
        return propertyValue;
    }

    /**
     * Merge together the values of all the provided arrays.
     * (From: https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java)
     *
     * @param first first array to merge
     * @param rest subsequent arrays to merge
     * @return T[]
     */
    @SafeVarargs
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Retrieves an EntityProxy object for the provided IGC object.
     *
     * @param igcomrsRepositoryConnector OMRS connector to the IBM IGC repository
     * @param igcObj the IGC object for which to retrieve an EntityProxy
     * @param omrsTypeName the OMRS entity type
     * @param userId the user through which to retrieve the EntityProxy (unused)
     * @return EntityProxy
     * @throws RepositoryErrorException
     */
    public static EntityProxy getEntityProxyForObject(IGCOMRSRepositoryConnector igcomrsRepositoryConnector,
                                                      Reference igcObj,
                                                      String omrsTypeName,
                                                      String userId) throws RepositoryErrorException {

        final String methodName = "getEntityProxyForObject";
        final String className = "ReferenceMapper";

        IGCRestClient igcRestClient = igcomrsRepositoryConnector.getIGCRestClient();
        String igcType = igcObj.getType();
        PrimitivePropertyValue qualifiedName = null;

        EntityProxy entityProxy = null;

        if (igcType != null) {
            // Construct 'qualifiedName' from the Identity of the object (label is a special case as a non-MainObject)
            MainObject igcMObj = null;
            if (igcType.equals("label")) {
                qualifiedName = ReferenceMapper.getPrimitivePropertyValue("(label)=" + igcObj.getName());
            } else {
                try {
                    igcMObj = (MainObject) igcObj;
                    qualifiedName = ReferenceMapper.getPrimitivePropertyValue(igcMObj.getIdentity(igcRestClient).toString());
                } catch (ClassCastException e) {
                    OMRSErrorCode errorCode = OMRSErrorCode.TYPEDEF_NOT_KNOWN_FOR_INSTANCE;
                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(igcType,
                            null,
                            methodName,
                            igcomrsRepositoryConnector.getRepositoryName());
                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                            className,
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction());
                }
            }

            // 'qualifiedName' is the only unique InstanceProperty we need on an EntityProxy
            InstanceProperties uniqueProperties = new InstanceProperties();
            uniqueProperties.setProperty("qualifiedName", qualifiedName);

            try {
                entityProxy = igcomrsRepositoryConnector.getRepositoryHelper().getNewEntityProxy(
                        SOURCE_NAME,
                        igcomrsRepositoryConnector.getMetadataCollectionId(),
                        InstanceProvenanceType.LOCAL_COHORT,
                        userId,
                        omrsTypeName,
                        uniqueProperties,
                        null
                );
                entityProxy.setGUID(igcObj.getId());

                if (igcMObj != null) {
                    entityProxy.setCreatedBy(igcMObj.getCreatedBy());
                    entityProxy.setCreateTime(igcMObj.getCreatedOn());
                    entityProxy.setUpdatedBy(igcMObj.getModifiedBy());
                    entityProxy.setUpdateTime(igcMObj.getModifiedOn());
                    if (entityProxy.getUpdateTime() != null) {
                        entityProxy.setVersion(entityProxy.getUpdateTime().getTime());
                    }
                }

            } catch (TypeErrorException e) {
                e.printStackTrace();
            }
        }

        return entityProxy;

    }

    /**
     * Converts an IGC type or property (something_like_this) into a camelcase class name (SomethingLikeThis).
     *
     * @param input
     * @return String
     */
    public static final String getCamelCase(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (String token : input.split("_")) {
            sb.append(token.substring(0, 1).toUpperCase());
            sb.append(token.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Recursively traverses the class hierarchy upwards to find the field.
     *
     * @param name the name of the field to find
     * @param clazz the class in which to search (and recurse upwards on its class hierarchy)
     * @return Field first found (lowest level of class hierarchy), or null if never found
     */
    public static Field recursePropertyByName(String name, Class clazz) {
        Field f = null;
        Class superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            try {
                f = superClazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                f = ReferenceMapper.recursePropertyByName(name, superClazz);
            }
        }
        return f;
    }

    /**
     * Retrieves the first Field, from anywhere within the class hierarchy (bottom-up), by its name.
     *
     * @param name the name of the field to retrieve
     * @return Field
     */
    public Field getFieldByName(String name) {
        Field field;
        try {
            field = this.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            field = ReferenceMapper.recursePropertyByName(name, this.getClass());
        }
        return field;
    }

    /**
     * Introspect a mapping class to retrieve a Mapper of that type.
     *
     * @param mappingClass the mapping class to retrieve an instance of
     * @param igcomrsRepositoryConnector connectivity to an IGC environment via an OMRS connector
     * @param userId the user through which to retrieve the mapping (currently unused)
     * @return ReferenceableMapper
     */
    public static final ReferenceableMapper getMapper(Class mappingClass, IGCOMRSRepositoryConnector igcomrsRepositoryConnector, String userId) {

        ReferenceableMapper referenceableMapper = null;

        try {
            Constructor constructor = mappingClass.getConstructor(MainObject.class, IGCOMRSRepositoryConnector.class, String.class);
            referenceableMapper = (ReferenceableMapper) constructor.newInstance(
                    null,
                    ((IGCOMRSRepositoryConnector) igcomrsRepositoryConnector),
                    userId
            );
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return referenceableMapper;

    }

    /**
     * Retrieve the IGC asset type covered by the provided mapping.
     *
     * @param mappingClass the mapping class to retrieve the IGC asset type for
     * @param igcomrsRepositoryConnector connectivity to an IGC environment via an OMRS connector
     * @param userId the user through which to retrieve the mapping (currently unused)
     * @return String
     */
    public static final String getIgcTypeFromMapping(Class mappingClass, IGCOMRSRepositoryConnector igcomrsRepositoryConnector, String userId) {

        String igcAssetTypeName = null;

        Field igcType;
        try {
            igcType = mappingClass.getDeclaredField("igcType");
        } catch (NoSuchFieldException e) {
            igcType = ReferenceMapper.recursePropertyByName("igcType", mappingClass);
        }
        if (igcType != null) {

            igcType.setAccessible(true);
            ReferenceableMapper referenceableMapper = ReferenceMapper.getMapper(mappingClass, igcomrsRepositoryConnector, userId);
            try {
                igcAssetTypeName = (String) igcType.get(referenceableMapper);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return igcAssetTypeName;

    }

    /**
     * Retrieve any additional POJO objects that are required by the provided mapping.
     *
     * @param mappingClass the mapping class for which to retrieve any additional POJOs
     * @param igcomrsRepositoryConnector connectivity to an IGC environment via an OMRS connector
     * @param userId the user through which to retrieve the mapping (currently unused)
     * @return
     */
    public static final List<String> getAdditionalIgcPOJOs(Class mappingClass, IGCOMRSRepositoryConnector igcomrsRepositoryConnector, String userId) {

        List<String> extraPOJOs = null;

        try {
            Method getOtherPOJOs = mappingClass.getMethod("getOtherIGCAssetTypes", null);
            ReferenceableMapper referenceableMapper = ReferenceMapper.getMapper(mappingClass, igcomrsRepositoryConnector, userId);
            extraPOJOs = (List<String>) getOtherPOJOs.invoke(referenceableMapper);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return extraPOJOs;

    }

    /**
     * Retrieve the properties mapped by the provided class.
     *
     * @param mappingClass the mapping class that defines the mapped properties
     * @param igcomrsRepositoryConnector connectivity to an IGC environment via an OMRS connector
     * @param userId the user through which to retrieve the mapping (currently unused)
     * @return PropertyMappingSet
     */
    public static final PropertyMappingSet getPropertiesFromMapping(Class mappingClass, IGCOMRSRepositoryConnector igcomrsRepositoryConnector, String userId) {

        PropertyMappingSet propertyMappingSet = null;

        Field properties;
        try {
            properties = mappingClass.getDeclaredField("PROPERTIES");
        } catch (NoSuchFieldException e) {
            properties = ReferenceMapper.recursePropertyByName("PROPERTIES", mappingClass);
        }
        if (properties != null) {

            properties.setAccessible(true);
            ReferenceableMapper referenceableMapper = ReferenceMapper.getMapper(mappingClass, igcomrsRepositoryConnector, userId);
            try {
                propertyMappingSet = (PropertyMappingSet) properties.get(referenceableMapper);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return propertyMappingSet;

    }

}
