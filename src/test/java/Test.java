import org.apache.commons.lang.StringUtils;
import server.ServerHandler;

/**
 * Created by Administrator on 2018/2/24 0024.
 */
public class Test {

    public static void main(String[] args) {
        String[] d = {"3", "33"};
//        ServerHandler.makeCheckSum(data);
        System.out.println(ServerHandler.makeCheckSum(d));
    }

}
