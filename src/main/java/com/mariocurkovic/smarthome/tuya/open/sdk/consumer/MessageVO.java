package com.mariocurkovic.smarthome.tuya.open.sdk.consumer;

import java.io.Serializable;

/**
 * @author: bilahepan
 * @date: 2019/3/26 下午5:36
 */
public class MessageVO implements Serializable {


    private String data;
    private Integer protocol;
    private String pv;
    private String sign;
    private Long t;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Long getT() {
        return t;
    }

    public void setT(Long t) {
        this.t = t;
    }

}