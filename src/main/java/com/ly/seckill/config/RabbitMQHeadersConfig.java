//package com.ly.seckill.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
///*
// * @description: some description
// */
//@Configuration
//public class RabbitMQHeadersConfig {
//
//    private static final String QUEUE01="queue_headers01";
//    private static final String QUEUE02="queue_headers02";
//    private static final String EXCHANGE="headersExchange";
//
//    @Bean
//    public Queue queue01(){
//        return new Queue(QUEUE01);
//    }
//
//    @Bean
//    public Queue queue02(){
//        return new Queue(QUEUE02);
//    }
//
//    @Bean
//    public HeadersExchange headersExchange(){
//        return new HeadersExchange(EXCHANGE);
//    }
//
//    @Bean
//    public Binding binding01(){
//        Map<String, Object> map = new HashMap<>();
//        map.put("color", "red");
//        map.put("speed", "low");
//        return BindingBuilder.bind(queue01()).to(headersExchange()).whereAny(map).match();
//    }
//
//    @Bean
//    public Binding binding02(){
//        Map<String, Object> map = new HashMap<>();
//        map.put("color", "red");
//        map.put("speed", "fast");
//        return BindingBuilder.bind(queue02()).to(headersExchange()).whereAll(map).match();
//    }
//}
