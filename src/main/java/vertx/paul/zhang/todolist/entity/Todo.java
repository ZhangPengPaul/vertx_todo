package vertx.paul.zhang.todolist.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by PaulZhang on 2016/9/7.
 */
@DataObject(generateConverter = true)
public class Todo {

    private static final AtomicInteger acc = new AtomicInteger(0); // counter

    private int id;
    private String title;
    private Boolean completed;
    private Integer order;
    private String url;

    public Todo() {
    }

    public Todo(Todo other) {
        this.id = other.getId();
        this.title = other.getTitle();
        this.completed = other.getCompleted();
        this.order = other.getOrder();
        this.url = other.getUrl();
    }

    public Todo(JsonObject jsonObj) {

    }

    public Todo(String jsonStr) {

    }

    public Todo(int id, String title, Boolean completed, Integer order, String url) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.order = order;
        this.url = url;
    }

    public JsonObject toJson() {
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static int getIncId() {
        return acc.get();
    }

    public void setIncId() {
        this.id = acc.incrementAndGet();
    }

    public static void setIncIdWith(int n) {
        acc.set(n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Todo)) return false;

        Todo todo = (Todo) o;

        if (getId() != todo.getId()) return false;
        if (!getTitle().equals(todo.getTitle())) return false;
        if (!getCompleted().equals(todo.getCompleted())) return false;
        if (!getOrder().equals(todo.getOrder())) return false;
        return getUrl().equals(todo.getUrl());

    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getTitle().hashCode();
        result = 31 * result + getCompleted().hashCode();
        result = 31 * result + getOrder().hashCode();
        result = 31 * result + getUrl().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}';
    }

    private <T> T getOrElse(T value, T defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }

    public Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                url);
    }

}
