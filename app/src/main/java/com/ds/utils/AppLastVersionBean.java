package com.ds.utils;

import java.util.List;

public class AppLastVersionBean {

    /**
     * code : 200
     * msg : 查询成功
     * data : [{"id":4,"description":"测试版本2","oldversion":"1.0.1","newversion":"1.0.2","size":9201445,"enforce":0,"platform":2,"downloadUrl":"/upload/apks/20200320/光明政企shadowsocks_4.1.8-188.apk","createTime":"2020-03-19 21:45:48","updateTime":"2020-03-19 21:45:48","status":1}]
     */

    private int code;
    private String msg;
    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 4
         * description : 测试版本2
         * oldversion : 1.0.1
         * newversion : 1.0.2
         * size : 9201445
         * enforce : 0
         * platform : 2
         * downloadUrl : /upload/apks/20200320/光明政企shadowsocks_4.1.8-188.apk
         * createTime : 2020-03-19 21:45:48
         * updateTime : 2020-03-19 21:45:48
         * status : 1
         */

        private int id;
        private String description;
        private String oldversion;
        private String newversion;
        private int versionnum;
        private int size;
        private int enforce;
        private int platform;
        private String downloadUrl;
        private String createTime;
        private String updateTime;
        private int status;

        public int getVersionnum() {
            return versionnum;
        }

        public void setVersionnum(int versionnum) {
            this.versionnum = versionnum;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOldversion() {
            return oldversion;
        }

        public void setOldversion(String oldversion) {
            this.oldversion = oldversion;
        }

        public String getNewversion() {
            return newversion;
        }

        public void setNewversion(String newversion) {
            this.newversion = newversion;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getEnforce() {
            return enforce;
        }

        public void setEnforce(int enforce) {
            this.enforce = enforce;
        }

        public int getPlatform() {
            return platform;
        }

        public void setPlatform(int platform) {
            this.platform = platform;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
