package com.example.demo.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
/*
@Component
public class CorsFilter implements GlobalFilter{
	
	 @Override
	    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
	        ServerHttpResponse response = exchange.getResponse();
	        HttpHeaders headers = response.getHeaders();

	        headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
	        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
	        headers.add("Access-Control-Allow-Headers", "*");
	        headers.add("Access-Control-Allow-Credentials", "true");

	        return chain.filter(exchange);
	    }

}*/
