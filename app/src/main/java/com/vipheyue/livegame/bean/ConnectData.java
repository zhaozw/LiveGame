package com.vipheyue.livegame.bean;

/**
 * Created by heyue on 16/5/13.
 */
public class ConnectData {

    /**
     * updatedAt : 2016-05-13 17:20:17
     * TotalIn_dong : 1
     * finish : true
     * TotalIn_nan : 11
     * objectId : d7US000P
     * createdAt : 2016-05-13 15:08:20
     * TotalIn_bei : 12
     * TotalIn_xi : 3
     */

    private GameBean data;
    /**
     * data : {"updatedAt":"2016-05-13 17:20:17","TotalIn_dong":1,"finish":true,"TotalIn_nan":11,"objectId":"d7US000P","createdAt":"2016-05-13 15:08:20","TotalIn_bei":12,"TotalIn_xi":3}
     * action : updateTable
     * objectId :
     * tableName : GameBean
     * appKey : e64952ec5a041da32b1c23568730fc4d
     */

    private String action;
    private String objectId;
    private String tableName;
    private String appKey;

    public GameBean getData() {
        return data;
    }

    public void setData(GameBean data) {
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

}
