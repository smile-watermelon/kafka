package com.smilewatermelon.kafka.entity;

import lombok.*;

/**
 * @author guagua
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserEntity {

    private Integer id;
    private String name;
}
