package de.blinkt.openvpn.base;


import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by sunwanquan on 2019/8/13.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class StatusInfo {
    private String state;
    private String logmessage;
    private int localizedResId;
    private VpnStatus.ConnectionStatus level;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLogmessage() {
        return logmessage;
    }

    public void setLogmessage(String logmessage) {
        this.logmessage = logmessage;
    }

    public int getLocalizedResId() {
        return localizedResId;
    }

    public void setLocalizedResId(int localizedResId) {
        this.localizedResId = localizedResId;
    }

    public VpnStatus.ConnectionStatus getLevel() {
        return level;
    }

    public void setLevel(VpnStatus.ConnectionStatus level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "StatusInfo{" +
                "state='" + state + '\'' +
                ", logmessage='" + logmessage + '\'' +
                ", localizedResId=" + localizedResId +
                ", level=" + level +
                '}';
    }
}
