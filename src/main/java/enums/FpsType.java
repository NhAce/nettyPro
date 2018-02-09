package enums;

/**
 * Created by Administrator on 2018/2/8 0008.
 */
public enum FpsType implements BaseEnum<FpsType, String> {
    LOGIN_HEARTBEAT_NOPOWER("00", "登录，心跳，失电报警"),TIMING("09", "校时"), INIT("03", "初始化"), RESTART("04", "重启"),
    INTERVAL("01", "设置采集频率");

    /* 标识码 */
    private String sourceCode;
    /* 中文名 */
    private String sourceName;

    FpsType(String sourceCode, String sourceName) {
        this.sourceCode = sourceCode;
        this.sourceName = sourceName;
    }

    public String getNo() {
        return sourceCode;
    }

    public String getName() {
        return sourceName;
    }

    public FpsType getProperty(String value) {
        switch (Integer.valueOf(value)){
            case 0 : return LOGIN_HEARTBEAT_NOPOWER;
            case 1 : return INTERVAL;
            case 3 : return INIT;
            case 4 : return RESTART;
            case 9 : return TIMING;
            default : throw new RuntimeException("未知枚举类型");
        }
    }
}
