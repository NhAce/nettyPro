import org.apache.commons.lang.StringUtils;
import server.ServerHandler;

/**
 * Created by Administrator on 2018/2/24 0024.
 */
public class Test {

    public static void main(String[] args) {
        String[] d = {"3", "33"};
//        ServerHandler.makeCheckSum(data);
        System.out.println(Float.intBitsToFloat(0x429A385F));
//        Float.intBitsToFloat(Integer.parseInt("0x429A385F"));
        System.out.println(Integer.parseInt("11", 16));
//        System.out.println(Float.intBitsToFloat(Integer.parseInt("429A385F",16)));
    }

}
