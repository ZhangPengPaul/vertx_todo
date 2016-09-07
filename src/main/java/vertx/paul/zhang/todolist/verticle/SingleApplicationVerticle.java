package vertx.paul.zhang.todolist.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import vertx.paul.zhang.todolist.Constants;
import vertx.paul.zhang.todolist.entity.Todo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by PaulZhang on 2016/9/7.
 */
public class SingleApplicationVerticle extends AbstractVerticle {

    private static final String HTTP_HOST = "0.0.0.0";
    private static final String REDIS_HOST = "192.168.6.220";
    private static final int HTTP_PORT = 9292;
    private static final int REDIS_PORT = 6379;

    private RedisClient redis;

    private void init() {
        RedisOptions config = new RedisOptions()
                .setHost(config().getString("redis.host", REDIS_HOST)) // redis host
                .setPort(config().getInteger("redis.port", REDIS_PORT)); // redis port

        this.redis = RedisClient.create(vertx, config); // redis client

        redis.hset(Constants.REDIS_TODO_KEY, "123", Json.encodePrettily(
                new Todo(123, "something todo", false, 1, "todo/abc")
        ), res -> {
            if (res.failed()) {
                System.err.println("[Error] Redis is not running!");
                res.cause().printStackTrace();
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        init();

        Router router = Router.router(vertx);

        // CORS
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");

        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        router.route().handler(BodyHandler.create());

        // router handler
        router.get(Constants.API_GET).handler(this::handleGetTodo);
        router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
        router.get(Constants.API_CREATE).handler(this::handleCreateTodo);
        router.get(Constants.API_DELETE).handler(this::handleDeleteTodo);
        router.get(Constants.API_DELETE_ALL).handler(this::handleDeleteAll);
        router.get(Constants.API_UPDATE).handler(this::handleUpdateTodo);

        vertx.createHttpServer().requestHandler(router::accept)
                .listen(HTTP_PORT, HTTP_HOST, result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    /**
     * 获取某一代办事项
     *
     * @param routingContext
     */
    private void handleGetTodo(RoutingContext routingContext) {
        String todoId = routingContext.request().getParam("todoId");
        if (Objects.isNull(todoId)) {
            sendError(400, routingContext.response());
        } else {
            redis.hget(Constants.REDIS_TODO_KEY, todoId, res -> {
                if (res.succeeded()) {
                    String result = res.result();
                    if (Objects.isNull(result)) {
                        sendError(404, routingContext.response());
                    } else {
                        routingContext.response().putHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
                                .end(result);
                    }
                } else {
                    sendError(503, routingContext.response());
                }
            });
        }
    }

    /**
     * 获取所有代办事项
     *
     * @param routingContext
     */
    private void handleGetAll(RoutingContext routingContext) {
        redis.hvals(Constants.REDIS_TODO_KEY, res -> {
            if (res.succeeded()) {
                String encoded = Json.encodePrettily(res.result().stream().map(mapper ->
                        new Todo((String) mapper)).collect(Collectors.toList()));
                routingContext.response().putHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
                        .end(encoded);
            } else {
                sendError(503, routingContext.response());
            }
        });
    }

    /**
     * 添加代办事项
     *
     * @param routingContext
     */
    private void handleCreateTodo(RoutingContext routingContext) {
        try {
            final Todo todo = wrapObject(new Todo(routingContext.getBodyAsString()), routingContext);
            final String encoded = Json.encodePrettily(todo);
            redis.hset(Constants.REDIS_TODO_KEY, String.valueOf(todo.getId()), encoded, res -> {
                if (res.succeeded()) {
                    routingContext.response().putHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
                            .setStatusCode(201)
                            .end(encoded);
                } else {
                    sendError(503, routingContext.response());
                }
            });
        } catch (DecodeException ex) {
            sendError(400, routingContext.response());
        }
    }

    /**
     * 更新某一代办事项
     *
     * @param routingContext
     */
    private void handleUpdateTodo(RoutingContext routingContext) {
        try {
            String todoId = routingContext.request().getParam("todoId");
            final Todo newTodo = new Todo(routingContext.getBodyAsString());

            if (Objects.isNull(todoId) || Objects.isNull(newTodo)) {
                sendError(400, routingContext.response());
                return;
            }

            redis.hget(Constants.REDIS_TODO_KEY, todoId, res -> {
                if (res.succeeded()) {
                    String result = res.result();
                    if (Objects.isNull(result)) {
                        sendError(404, routingContext.response());
                    } else {
                        Todo oldTodo = new Todo(result);
                        String response = Json.encodePrettily(oldTodo.merge(newTodo));
                        redis.hset(Constants.REDIS_TODO_KEY, todoId, response, res2 -> {
                            if (res2.succeeded()) {
                                routingContext.response()
                                        .putHeader(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
                                        .end(response);
                            }
                        });
                    }
                } else {
                    sendError(503, routingContext.response());
                }
            });
        } catch (DecodeException ex) {
            sendError(400, routingContext.response());
        }
    }

    /**
     * 删除某一代办事项
     *
     * @param routingContext
     */
    private void handleDeleteTodo(RoutingContext routingContext) {
        String todoId = routingContext.request().getParam("todoId");
        redis.hdel(Constants.REDIS_TODO_KEY, todoId, res -> {
            if (res.succeeded()) {
                routingContext.response().setStatusCode(204).end();
            } else {
                sendError(503, routingContext.response());
            }
        });
    }

    /**
     * 删除所有代办事项
     *
     * @param routingContext
     */
    private void handleDeleteAll(RoutingContext routingContext) {
        redis.del(Constants.REDIS_TODO_KEY, res -> {
            if (res.succeeded()) {
                routingContext.response().setStatusCode(204).end();
            } else {
                sendError(503, routingContext.response());
            }
        });
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    /**
     * Wrap the Todo entity with appropriate id and url
     *
     * @param todo    a todo entity
     * @param context RoutingContext
     * @return the wrapped todo entity
     */
    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id);
        } else if (id == 0)
            todo.setIncId();
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo;
    }
}
