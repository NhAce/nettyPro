package util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/24 0024.
 */
public class ChannelGroups {

    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);

    private static final Map<String, ChannelId> arg1 = new HashMap<String, ChannelId>();

    public static void add(String code, Channel channel){
        if (channel != null) {
            arg1.put(code, channel.id());
            CHANNEL_GROUP.add(channel);
        }
    }

    public static ChannelGroup flush(){
        return CHANNEL_GROUP.flush();
    }

    public static boolean remove(Channel channel){
        return CHANNEL_GROUP.remove(channel);
    }

    public static boolean contains(Channel channel){
        return CHANNEL_GROUP.contains(channel);
    }

    public static int size(){
        return CHANNEL_GROUP.size();
    }

    public static Channel find(String code) {
        return CHANNEL_GROUP.find(arg1.get(code));
    }
}
