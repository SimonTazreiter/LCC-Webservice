package com.liland.lcc.dto;

public class FilterStates {
    private SystemStatus status;
    private InstanceType instancetype;

    //region getter
    public SystemStatus getStatus() {
        return status;
    }

    public InstanceType getInstancetype() {
        return instancetype;
    }
    //endregion
    
    //region setter
    public void setStatus(SystemStatus status) {
        this.status = status;
    }

    public void setInstancetype(InstanceType instancetype) {
        this.instancetype = instancetype;
    }
    //endregion
}
