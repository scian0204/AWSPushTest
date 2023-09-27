package com.awspushtest.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwsConst {
    @Value("${accesskey}")
    private String awsAccessKey;

    @Value("${secretKey}")
    private String awsSecretKey;

    @Value("${region}")
    private String awsRegion;

    @Value("${platformAppAndroid}")
    private String awsPlatformAppArnAndroid;

    @Value("${platformAppIos}")
    private String awsPlatformAppArnIos;
}
