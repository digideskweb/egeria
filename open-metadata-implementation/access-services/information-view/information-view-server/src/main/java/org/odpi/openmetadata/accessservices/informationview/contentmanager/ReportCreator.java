/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.informationview.contentmanager;

import org.odpi.openmetadata.accessservices.informationview.events.ReportRequestBody;
import org.odpi.openmetadata.accessservices.informationview.lookup.LookupHelper;
import org.odpi.openmetadata.accessservices.informationview.utils.Constants;
import org.odpi.openmetadata.accessservices.informationview.utils.EntityPropertiesBuilder;
import org.odpi.openmetadata.accessservices.informationview.utils.EntityPropertiesUtils;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReportCreator extends ReportBasicOperation {

    private static final Logger log = LoggerFactory.getLogger(ReportCreator.class);

    public ReportCreator(EntitiesCreatorHelper entitiesCreatorHelper, LookupHelper lookupHelper, OMRSAuditLog auditLog) {
        super(entitiesCreatorHelper, lookupHelper, auditLog);
    }


    public void createReport(ReportRequestBody payload, EntityDetail reportEntity) throws Exception {
        String qualifiedNameForComplexSchemaType = EntityPropertiesUtils.getStringValueForProperty(reportEntity.getProperties(), Constants.QUALIFIED_NAME) + Constants.TYPE_SUFFIX;
        InstanceProperties complexSchemaTypeProperties = new EntityPropertiesBuilder()
                .withStringProperty(Constants.QUALIFIED_NAME, qualifiedNameForComplexSchemaType)
                .build();
        EntityDetailWrapper complexSchemaTypeEntityWrapper = entitiesCreatorHelper.createOrUpdateEntity(Constants.COMPLEX_SCHEMA_TYPE,
                qualifiedNameForComplexSchemaType, complexSchemaTypeProperties, null, true);

        log.debug("Created report schema type {}", complexSchemaTypeEntityWrapper.getEntityDetail().getGUID());
        entitiesCreatorHelper.addRelationship(Constants.ASSET_SCHEMA_TYPE,
                reportEntity.getGUID(),
                complexSchemaTypeEntityWrapper.getEntityDetail().getGUID(),
                Constants.INFORMATION_VIEW_OMAS_NAME,
                new InstanceProperties());

        addElements(EntityPropertiesUtils.getStringValueForProperty(reportEntity.getProperties(), Constants.QUALIFIED_NAME), complexSchemaTypeEntityWrapper.getEntityDetail().getGUID(), payload.getReportElements());
    }





}
