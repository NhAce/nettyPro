package enums;


public interface BaseEnum<T, S> {

    /**
     * 获得属性值
     *
     * @return： 属性值
     */
    public S getNo();

    /**
     * 获得属性的解释，用于输出
     *
     * @return： 属性的中文解释
     */
    public String getName();

    /**
     * 根据数值获得枚举值
     *
     * @param value
     *            : 枚举类型的数值，与数据库对应
     * @return： 枚举值
     */
    public T getProperty(S value);
}
