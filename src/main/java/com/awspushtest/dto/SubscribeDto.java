package com.awspushtest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribeDto {
    private String deviceToken;
    private String os;
}
