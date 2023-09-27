package com.awspushtest.controller;

import com.awspushtest.config.AwsConst;
import com.awspushtest.dto.SubscribeDto;
import com.awspushtest.service.CredentialService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sns")
public class SnsController {
    @Autowired
    private AwsConst awsConst;
    private final CredentialService credentialService;

    @PostMapping("/subscribe")
    public String subscribeTopic(@RequestBody SubscribeDto subscribeDto) throws Exception {
        SnsClient snsClient = credentialService.getSnsClient();
        String endpointArn = null;

        List<Endpoint> endpoints = snsClient.listEndpointsByPlatformApplication(builder -> {
            builder.platformApplicationArn(awsConst.getAwsPlatformAppArnAndroid()).build();
        }).endpoints();

        for (int i = 0; i < endpoints.size(); i++) {
            if (endpoints.get(i).attributes().get("Token").equals(subscribeDto.getDeviceToken())) {
                endpointArn = endpoints.get(i).endpointArn();
                break;
            }
        }

        if (endpointArn == null) {
            try {
                endpointArn = snsClient.createPlatformEndpoint(
                        builder -> {
                            builder
                                    .token(subscribeDto.getDeviceToken())
                                    .customUserData(subscribeDto.getOs())
                                    .platformApplicationArn(subscribeDto.getOs().equals("android") ? awsConst.getAwsPlatformAppArnAndroid() : awsConst.getAwsPlatformAppArnIos())
                                    .build();
                        }
                ).endpointArn();
            } catch (Exception e) {
                return "AWS SNS endpoint 생성 실패";
            }
        }

        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .endpoint(endpointArn)
                .protocol("Application")
                .topicArn(getTopicArns().get(0))
                .build();
        SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);
        if (!subscribeResponse.sdkHttpResponse().isSuccessful()) {
            throw new Exception();
        }
        return "AWS SNS endpoint 생성 및 주제 구독 성공";
    }

    @GetMapping("/getTopicArns")
    public List<String> getTopicArns() {
        SnsClient snsClient = credentialService.getSnsClient();
        List<Topic> topics = snsClient.listTopics().topics();
        List<String> topicArns = new ArrayList<>();
        topics.forEach(topic -> {
            topicArns.add(topic.topicArn());
        });
        return topicArns;
    }

    @GetMapping("/getSubscriptionByTopic/{topicArn}")
    public List<String> getSubsByTopic(@PathVariable String topicArn) {
        SnsClient snsClient = credentialService.getSnsClient();
        List<Subscription> subscriptions = null;
        List<String> res = new ArrayList<>();
        try {
            subscriptions = snsClient.listSubscriptionsByTopic(builder -> {
                builder.topicArn(topicArn).build();
            }).subscriptions();
        } catch (Exception e) {
            res.add(e.getMessage());
            return res;
        }

        subscriptions.forEach(subscription -> {
            res.add(subscription.subscriptionArn());
        });

        return res;
    }

    @GetMapping("/sendMsg/{message}")
    public String sendMsg(@PathVariable String message) {
        Map<String, String> map = new HashMap<>();
        map.put("GCM", "{ \"data\": { \"message\": \"" + message + "\" } }");
        map.put("default", "default test");
        String msg = JSONObject.toJSONString(map);

        try {
            SnsClient snsClient = credentialService.getSnsClient();
            PublishRequest publishRequest =
                    PublishRequest.builder()
                            .messageStructure("json")
                            .message(msg)
                            .topicArn(getTopicArns().get(0))
                            .build();
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            return msg;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
