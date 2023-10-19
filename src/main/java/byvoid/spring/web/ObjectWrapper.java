package byvoid.spring.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class ObjectWrapper<T> implements Serializable {

    private String code;

    private String message;

    private T data;

    public ObjectWrapper() {}

    public ObjectWrapper(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
