package util;


public interface Const {
    /**
     * 登录，心跳，失电报警的帧类型
     */
    String[] X = {"00", "09", "28", "01", "03", "04", "39", "15", "52", "53"};
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
}
