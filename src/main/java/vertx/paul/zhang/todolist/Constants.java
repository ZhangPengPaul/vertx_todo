package vertx.paul.zhang.todolist;

/**
 * Created by PaulZhang on 2016/9/7.
 */
public final class Constants {

    private Constants() {

    }

    /**
     * API Route
     */
    public static final String API_GET = "/todos/:todoId";
    public static final String API_LIST_ALL = "/todos";
    public static final String API_CREATE = "/todos";
    public static final String API_UPDATE = "/todos/:todoId";
    public static final String API_DELETE = "/todos/:todoId";
    public static final String API_DELETE_ALL = "/todos";

    /**
     * Persistence key
     */
    public static final String REDIS_TODO_KEY = "VERT_TODO";

    public static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

}
