package com.liland.lcc.cl;

import com.liland.lcc.dto.SystemStatus;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "data")
public class UserDataDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    @Lob
    private Blob publickey;
    private String instancetype;
    private String version;
    private SystemStatus status;
    private Date timestamp;

    /*public UserDataDB(String uuid, String publickey, String instancetype, String version, Integer status, String timestamp){
        this.uuid = uuid;
        this.publickey = publickey;
        this.instancetype = instancetype;
        this.version = version;
        this.status = status;
        this.timestamp = timestamp;
    }*/

    public String getUuid(){
        return uuid;
    }

    public Blob getPublickey(){
        return publickey;
    }

    public String getInstancetype(){
        return instancetype;
    }

    public String getVersion(){
        return version;
    }

    public SystemStatus getStatus(){
        return status;
    }

    public Date getTimestamp(){
        return timestamp;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public void setPublickey(Blob publickey){
        this.publickey = publickey;
    }

    public void setInstancetype(String instancetype){
        this.instancetype = instancetype;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public void setStatus(SystemStatus status){
        this.status = status;
    }

    public void setTimestamp(Date timestamp){
        this.timestamp = timestamp;
    }
}