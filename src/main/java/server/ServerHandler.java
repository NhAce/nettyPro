package server;

import enums.FpsType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import rabbitMQ.Producer;
import util.ChannelGroups;
import util.Const;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
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
    private boolean timingSuccess = false;
    private String GPRSCode = null;
//    private Date loginTime = null;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf result = (ByteBuf)msg;
        String resultStr = ByteBufUtil.hexDump(result);
        no.incrementAndGet();
        logger.info("Client said: " + resultStr + " length: " + resultStr.length() + " no: " + no);
        if (!StringUtils.isEmpty(resultStr)) {
            if (resultStr.length() == 40 && resultStr.substring(22, 24)
                    .equals(FpsType.LOGIN_HEARTBEAT_NOPOWER.getNo())) { //登录帧，心跳帧，失电报警
                dealWithBasicInfo(ctx, resultStr);
            } else if (resultStr.length() == 36 && resultStr.substring(18, 20)
                    .equals(FpsType.TIMING.getNo())){
                dealWithTimingInfo(ctx, resultStr);
            } else if (resultStr.length() == 60 && resultStr.substring(18, 20)
                    .equals(FpsType.BEIWEI_DATA.getNo())){
                dealWithBeiweiData(ctx, resultStr);
            }
        }
        ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        channels.size();
//        ctx.writeAndFlush("1234567");
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
     * @param ctx
     * @param mark 用于打印/log的输出
     * <br>//channel.writeAndFlush(msg);//不行
     * <br>//channel.writeAndFlush(receiveStr.getBytes());//不行
     * <br>在netty里，进出的都是ByteBuf，楼主应确定服务端是否有对应的编码器，将字符串转化为ByteBuf
     */
    private void writeToClient(final String receiveStr, ChannelHandlerContext ctx, final String mark) {
        try {
            ByteBuf bufff = Unpooled.buffer();//netty需要用ByteBuf传输
            bufff.writeBytes(hexStringToBytes(receiveStr));//对接需要16进制
            ctx.writeAndFlush(bufff).addListener(new ChannelFutureListener() {
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

    private byte[] hexStringToBytes(String hexString) {
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
    public static String makeCheckSum(String[] data) {
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

    /**
     * 获取年月日时分秒数组
     * @return
     */
    private String[] timing(){
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
        String[] timing = sdf.format(new Date()).split("-");
        return timing;
    }

    /**
     * 处理 登录帧，心跳帧，失电报警
     * @param ctx
     * @param info
     */
    private void dealWithBasicInfo(ChannelHandlerContext ctx, String info){
        String[] strArr = new String[info.length() / 2];
        for (int i = 0; i < info.length() / 2; i++) {
            strArr[i] = info.substring(i * 2, (i + 1) * 2);
        }
        String fpsType2 = info.substring(32, 36);//帧识别码
        //处理信息
        strArr[6] = "00";
        strArr[12] = "00";
        String[] newStrArray = new String[12];
        System.arraycopy(strArr, 6, newStrArray, 0, 12);
        strArr[18] = makeCheckSum(newStrArray);//重新计算校验码
        String response = StringUtils.join(strArr);
        GPRSCode = strArr[8] + strArr[7] + strArr[10] + strArr[9];
        if (Const.LOGIN.equals(fpsType2)) {//登录帧
            writeToClient(response, ctx, "登录帧下行： " + response);
            ChannelGroups.add(GPRSCode, ctx.channel());
            outputTimingInfo(ctx);
        } else if (Const.HEART_BEAT.equals(fpsType2)) {//心跳帧
            writeToClient(response, ctx, "心跳帧下行： " + response);
        } else if (Const.NO_POWER_ALARM.equals(fpsType2)){//失电报警
            writeToClient(response, ctx, "失电报警下行： " + response);
        }

    }

    /**
     * 处理 校时 信息
     * @param ctx
     * @param info
     */
    private void dealWithTimingInfo(ChannelHandlerContext ctx, String info) {
        if (!timingSuccess) {
            String[] strArr = new String[info.length() / 2];
            for (int i = 0; i < info.length() / 2; i++) {
                strArr[i] = info.substring(i * 2, (i + 1) * 2);
            }
            String controlWord = strArr[4];
            if (!"DC".equalsIgnoreCase(controlWord)) {
                String[] timing = timing();
                for (int i = 0; i < timing.length; i++) {
                    strArr[i + 10] = timing[i];
                }
                String[] newStrArray = new String[13];
                System.arraycopy(strArr, 3, newStrArray, 0, 13);
                strArr[16] = makeCheckSum(newStrArray);
                String response = StringUtils.join(strArr);
                writeToClient(response, ctx, "校时下行：" + response);
            }else {
                timingSuccess = true;
            }
        }
    }

    /**
     * 主动下发校时信息
     * @param ctx
     */
    private void outputTimingInfo(final ChannelHandlerContext ctx){
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timingSuccess){
                    timer.cancel();
                    logger.info("GPRS编号" + GPRSCode + "的主动校时任务停止");
                }else {
                    String[] timing = timing();
                    String info = "680C0C685C" + GPRSCode + "09" + StringUtils.join(timing) + "0016";
                    String[] strArr = new String[info.length() / 2];
                    for (int i = 0; i < info.length() / 2; i++) {
                        strArr[i] = info.substring(i * 2, (i + 1) * 2);
                    }
                    String[] newStrArray = new String[13];
                    System.arraycopy(strArr, 3, newStrArray, 0, 13);
                    strArr[16] = makeCheckSum(newStrArray);//重新计算校验码
                    String response = StringUtils.join(strArr);
                    writeToClient(response, ctx, "主动下发校时信息：" + response);
                }
            }
        }, 30000, 30000);
    }

    /**
     * 处理北微倾角传感器上传的数据
     */
    private void dealWithBeiweiData(ChannelHandlerContext ctx, String data) {
        logger.info(data);
    }

    private void changeConcentratorConfig(String originAddress, String newAddress,
                                          String mainIp, String mainPort, String reserveIP, String reservePort) {
        StringBuilder sb = new StringBuilder("681717685C");
        sb = sb.append(originAddress).append("28");
        String[] ips = mainIp.split("\\.");
        for (String ip : ips) {
            sb = sb.append(Integer.toHexString(Integer.valueOf(ip)));
        }
        String portHex = Integer.toHexString(Integer.valueOf(mainPort));
        if (!StringUtils.isEmpty(portHex)) {
            sb = sb.append(portHex.substring(2, portHex.length())).append(portHex.substring(0, 2));
        }
        sb = sb.append(newAddress);
        ips = reserveIP.split("\\.");
        for (String ip : ips) {
            sb = sb.append(Integer.toHexString(Integer.valueOf(ip)));
        }
        portHex = Integer.toHexString(Integer.valueOf(reservePort));
        if (!StringUtils.isEmpty(portHex)) {
            sb = sb.append(portHex.substring(2, portHex.length())).append(portHex.substring(0, 2));
        }
        sb = sb.append("03");
        String[] strArr = new String[24];
        generateAndOutputData(sb, strArr, originAddress);
    }

    private void changeFrequency (String frequency, String originAddress) {
        if (StringUtils.isNotEmpty(frequency) && StringUtils.isNotBlank(frequency)
                && StringUtils.isNotEmpty(originAddress) && StringUtils.isNotBlank(originAddress)) {
            StringBuilder sb = new StringBuilder("680C0C685C");
            sb = sb.append(originAddress).append("01");
            String[] arg1 = new String[2];
            String[] arg2 = new String[2];
            if (frequency.length() == 3) {
                arg1[0] = frequency.substring(0, 1);
                arg1[1] = "33";
                arg2[0] = frequency.substring(2);
                arg2[1] = "33";
            }else if (frequency.length() == 4) {
                arg1[0] = frequency.substring(0, 2);
                arg1[1] = "33";
                arg2[0] = frequency.substring(2);
                arg2[1] = "33";
            }
            sb = sb.append(makeCheckSum(arg2)).append(makeCheckSum(arg1)).append("00333300");
            String[] strArr = new String[13];
            generateAndOutputData(sb, strArr, originAddress);
        }
    }

    private void initAndRestart(String originAddress, String operateType) {
        StringBuilder sb = new StringBuilder("680606685D");
        if (Const.INIT.equals(operateType)) {
            sb = sb.append(originAddress).append("03");
        } else if (Const.RESTART.equals(operateType)) {
            sb = sb.append(originAddress).append("04");
        }
        String[] strArr = new String[7];
        generateAndOutputData(sb, strArr, originAddress);
    }

    private void generateAndOutputData(StringBuilder sb, String[] strArr, String originAddress) {
        String str = sb.toString().substring(6);
        for (int i = 0; i < str.length() / 2; i++) {
            strArr[i] = str.substring(i * 2, (i + 1) * 2);
        }
        sb = sb.append(makeCheckSum(strArr)).append(16);
        Channel channel = ChannelGroups.find(originAddress);
        ByteBuf buff = Unpooled.buffer();//netty需要用ByteBuf传输
        buff.writeBytes(hexStringToBytes(sb.toString()));//对接需要16进制
        channel.writeAndFlush(buff);
    }

}
