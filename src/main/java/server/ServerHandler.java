package server;

import enums.FpsType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import rabbitMQ.Producer;
import util.Const;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/2/1 0001.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private Producer producer;
    private int queueNum;
    private static final Logger logger = Logger.getLogger(ServerHandler.class);
    public ServerHandler(Producer producer, int queueNum){
        this.producer = producer;
        this.queueNum = queueNum;
    }
    private static final AtomicInteger no = new AtomicInteger(0);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf result = (ByteBuf)msg;
        String resultStr = ByteBufUtil.hexDump(result);
        no.incrementAndGet();
        logger.info("Client said: " + resultStr + " length: " + resultStr.length() + " no: " + no);
        String response;
        if (!StringUtils.isEmpty(resultStr)) {
            if (resultStr.length() == 40 && resultStr.substring(22, 24)
                    .equals(FpsType.LOGIN_HEARTBEAT_NOPOWER.getNo())) { //登录帧，心跳帧，失电报警
                String[] strArr = new String[resultStr.length() / 2];
                for (int i = 0; i < resultStr.length() / 2; i++) {
                    strArr[i] = resultStr.substring(i * 2, (i + 1) * 2);
                }
                String fpsType2 = resultStr.substring(32, 36);//帧识别码
                //处理信息
                strArr[6] = "00";
                strArr[12] = "00";
                String[] newStrArray = new String[12];
                System.arraycopy(strArr, 6, newStrArray, 0, 12);
                strArr[18] = makeCheckSum(newStrArray);//重新计算校验码
                response = StringUtils.join(strArr);
                if (Const.LOGIN.equals(fpsType2)) {//登录帧
                    writeToClient(response, ctx, "登录帧下行： " + response);
                } else if (Const.HEART_BEAT.equals(fpsType2)) {//心跳帧
                    writeToClient(response, ctx, "心跳帧下行： " + response);
                } else if (Const.NO_POWER_ALARM.equals(fpsType2)){//失电报警
                    writeToClient(response, ctx, "失电报警下行： " + response);
                }
            } else if (resultStr.length() == 36 && resultStr.substring(21, 23)
                    .equals(FpsType.TIMING)){

            }
        }
//        ctx.writeAndFlush(Unpooled.buffer().writeBytes(hexStringToBytes(response)));
//        producer.sendMessage(resultStr, no.get(), queueNum);
        result.release();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常的时候关闭连接
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 公用回写数据到客户端的方法
     * @param receiveStr 需要回写的字符串
     * @param channel
     * @param mark 用于打印/log的输出
     * <br>//channel.writeAndFlush(msg);//不行
     * <br>//channel.writeAndFlush(receiveStr.getBytes());//不行
     * <br>在netty里，进出的都是ByteBuf，楼主应确定服务端是否有对应的编码器，将字符串转化为ByteBuf
     */
    private void writeToClient(final String receiveStr, ChannelHandlerContext channel, final String mark) {
        try {
            ByteBuf bufff = Unpooled.buffer();//netty需要用ByteBuf传输
            bufff.writeBytes(hexStringToBytes(receiveStr));//对接需要16进制
            channel.writeAndFlush(bufff).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
//                    StringBuilder sb = new StringBuilder("");
//                    if(!StringUtils.isEmpty(mark)){
//                        sb.append("【").append(mark).append("】");
//                    }
                    logger.info(mark);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("调用通用writeToClient()异常" + e.getMessage());
        }
    }

    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /** 计算校验位 ，返回十六进制校验位 */
    private static String makeCheckSum(String[] data) {
        int dSum = 0;
        int length = 0;
        // 遍历十六进制，并计算总和
        for (int i = 0; i < data.length; i++){
            dSum += Integer.parseInt(data[i], 16);
        }
        int mod = dSum % 256; // 用256取余，十六进制最大是FF，FF的十进制是255
        String checkSumHex = Integer.toHexString(mod).toUpperCase(); // 余数转成十六进制
        length = checkSumHex.length();
        if (length < 2) {
            checkSumHex = "0" + checkSumHex;  // 校验位不足两位的，在前面补0
        }
        return checkSumHex;
    }
}
