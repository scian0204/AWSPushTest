package com.awspushtest.service;

import com.awspushtest.config.AwsConst;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;



@Service
@RequiredArgsConstructor
public class CredentialService {
    @Autowired
    private AwsConst awsConst;

    public AwsCredentialsProvider getAwsCredentials(String accessKeyId, String secretAccessKey) {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return () -> awsBasicCredentials;
    }

    public SnsClient getSnsClient() {
        return SnsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(
                        getAwsCredentials(awsConst.getAwsAccessKey(), awsConst.getAwsSecretKey())
                )
                .build();
    }
}
