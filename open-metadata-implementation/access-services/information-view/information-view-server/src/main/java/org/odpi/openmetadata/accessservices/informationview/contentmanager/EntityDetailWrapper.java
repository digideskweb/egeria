/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.informationview.contentmanager;


import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;

public class EntityDetailWrapper {

    private EntityDetail entityDetail;
    private EntityStatus entityStatus;


    public EntityDetailWrapper(EntityDetail entityDetail, EntityStatus entityStatus) {
        this.entityDetail = entityDetail;
        this.entityStatus = entityStatus;
    }

    public EntityDetail getEntityDetail() {
        return entityDetail;
    }

    public void setEntityDetail(EntityDetail entityDetail) {
        this.entityDetail = entityDetail;
    }

    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    public enum EntityStatus {
        NEW,
        UPDATED,
        DELETED

    }

}
