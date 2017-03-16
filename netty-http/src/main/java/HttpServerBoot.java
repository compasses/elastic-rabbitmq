
import http.Config;
import http.DefaultServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by i311352 on 2/16/2017.
 */
public class HttpServerBoot {
    ConfigurableApplicationContext context;
    private Config conf = new Config();


    public HttpServerBoot(ConfigurableApplicationContext configurableApplicationContext) {
        this.context = configurableApplicationContext;
    }

    protected static boolean isEpollAvailable = false;

    static {
        isEpollAvailable = Epoll.isAvailable();
    }

    public void run() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        try {
            if (isEpollAvailable) {
                b.group(new EpollEventLoopGroup(this.conf.getEventLoopThreadCount()))
                 .channel(EpollServerSocketChannel.class);
            } else {
                b.group(new NioEventLoopGroup(this.conf.getEventLoopThreadCount()))
                 .channel(NioServerSocketChannel.class);
            }
            b.childHandler(new DefaultServerInitializer(conf, context))
             .option(ChannelOption.SO_BACKLOG, conf.getBacklog())
             .option(ChannelOption.SO_REUSEADDR, true);

            Channel ch = b.bind(conf.getPort()).sync().channel();
            ch.closeFuture().sync();
        } finally {

        }
    }
}
