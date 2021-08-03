package de.blinkt.openvpn;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import de.blinkt.openvpn.base.StatusInfo;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by sunwanquan on 2019/8/13.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BindUtils {

    private static Set<Object> sBindStatus = new HashSet<>();

    public static void bind(Object className) {
        sBindStatus.add(className);
    }

    public static void unBind(Object className) {
        sBindStatus.remove(className);
    }

    public static void bindStatus(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level) {
        StatusInfo info = new StatusInfo();
        info.setState(state);
        info.setLogmessage(logmessage);
        info.setLocalizedResId(localizedResId);
        info.setLevel(level);

        for (Object bindStatus : sBindStatus) {
            try {
                Class c = bindStatus.getClass();
                Method[] ms = c.getDeclaredMethods();
                for (Method method : ms) {
                    if (method.isAnnotationPresent(BindStatus.class)) {
                        method.setAccessible(true);
                        method.invoke(bindStatus, info);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
