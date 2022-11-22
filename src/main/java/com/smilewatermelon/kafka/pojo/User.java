package com.smilewatermelon.kafka.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guagua
 * @date 2022/11/22 16:09
 * @describe
 */
@Data
@Builder
public class User {
    private String name;

    private Integer age;

    private String email;
}
