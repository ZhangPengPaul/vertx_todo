package vertx.paul.zhang.todolist.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;

/**
 * Created by PaulZhang on 2016/9/7.
 */
public class SingleApplicationVerticle extends AbstractVerticle {

    private static final String HTTP_HOST = "0.0.0.0";
    private static final String REDIS_HOST = "192.168.6.220";
    private static final int HTTP_PORT = 9292;
    private static final int REDIS_PORT = 6379;

    private RedisClient redisClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        // TODO: 2016/9/7 start 
    }
}
