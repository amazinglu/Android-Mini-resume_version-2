package com.parabit.parabeacon.app.demo.model;

import com.parabit.mmrbt.api.BeaconInfo;
import com.parabit.parabeacon.app.demo.state.Door;

import java.util.Date;

public class DiagnosticMsg {
    private String msgId;
    private String msgTitle;
    private String msgType;
    private boolean msgSend, msgReceive;
    private String successMsg, errorMsg;
    private Door door;
    private double roundTrip;
    private Date msgDate;

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setMsgSend(boolean msgSend) {
        this.msgSend = msgSend;
    }

    public void setMsgReceive(boolean msgReceive) {
        this.msgReceive = msgReceive;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setDoor(Door door) {
        this.door = door;
    }

    public void setRoundTrip(double roundTrip) {
        this.roundTrip = roundTrip;
    }

    public void setMsgDate(Date msgDate) {
        this.msgDate = msgDate;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public String getMsgType() {
        return msgType;
    }

    public boolean isMsgSend() {
        return msgSend;
    }

    public boolean isMsgReceive() {
        return msgReceive;
    }

    public String getSuccessMsg() {
        return successMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Door getDoor() {
        return door;
    }

    public double getRoundTrip() {
        return roundTrip;
    }

    public Date getMsgDate() {
        return msgDate;
    }
}
