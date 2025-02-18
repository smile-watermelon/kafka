package com.smilewatermelon.kafka.two.chapter;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Company {

    private String name;

    private String address;
}
