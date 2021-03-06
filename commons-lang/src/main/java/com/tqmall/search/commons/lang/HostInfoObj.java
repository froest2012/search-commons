package com.tqmall.search.commons.lang;

import java.io.Serializable;

/**
 * Created by xing on 16/1/1.
 * 实现{@link HostInfo}的默认对象
 */
public class HostInfoObj implements HostInfo, Serializable {

    private static final long serialVersionUID = 8506400854259669539L;

    private String ip;

    private int port;

    public HostInfoObj() {
    }

    public HostInfoObj(HostInfo hostInfo) {
        this.ip = hostInfo.getIp();
        this.port = hostInfo.getPort();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return ip + ':' + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostInfoObj)) return false;

        HostInfoObj that = (HostInfoObj) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
