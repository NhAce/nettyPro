package enums;

/**
 * Created by Administrator on 2018/2/8 0008.
 */
public enum FpsType implements BaseEnum<FpsType, String> {
    LOGIN_HEARTBEAT_NOPOWER("C9", "00", "登录，心跳，失电报警"),TIMING("5C", "09", "校时"), INIT("5D", "03", "初始化"),
    RESTART("5D", "04", "重启"), INTERVAL("5C", "01", "设置采集频率"), BEIWEI_DATA("5A", "39", "北微倾角数据"),
    CHANGE_CONFIG("5C", "28", "修改汇集器配置"), INCLINOMETER_ISSUED("5C", "52", "倾角仪表计下发"),
    INCLINOMETER_CANCEL("5C", "53", "倾角仪表计取消");

    /* 控制字 */
    private String controlWord;
    /* 帧类型 */
    private String type;
    /* 中文名 */
    private String name;

    FpsType(String controlWord, String type, String name) {
        this.controlWord = controlWord;
        this.type = type;
        this.name = name;
    }

    public String getControlWord() {
        return controlWord;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public FpsType getProperty(String value) {
        switch (Integer.valueOf(value)){
            case 0 : return LOGIN_HEARTBEAT_NOPOWER;
            case 1 : return INTERVAL;
            case 3 : return INIT;
            case 4 : return RESTART;
            case 9 : return TIMING;
            case 39 : return BEIWEI_DATA;
            default : throw new RuntimeException("未知枚举类型");
        }
    }
}
