package org.example.utils.bean;

import lombok.*;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HttpResult<T> implements Serializable {

    private boolean success;
    private String msg;
    private T object;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
