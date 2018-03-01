package util;


public interface Const {

    /**
     * 登录帧识别帧
     */
    String LOGIN = "0100";
    /**
     * 心跳帧识别帧
     */
    String HEART_BEAT = "0400";
    /**
     * 失电报警识别帧
     */
    String NO_POWER_ALARM = "0010";
    /**
     * 初始化操作
     */
    String INIT = "init";
    /**
     * 重启
     */
    String RESTART = "restart";
    /**
     * 倾角仪表计下发
     */
    String INCLINOMETER_ISSUED = "issued";
    /**
     * 倾角仪表计取消
     */
    String INCLINOMETER_CANCEL = "cancel";
}
