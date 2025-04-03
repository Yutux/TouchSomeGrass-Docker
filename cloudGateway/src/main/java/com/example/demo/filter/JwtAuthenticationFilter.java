package com.example.demo.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.utils.JwtService;
import com.example.demo.utils.RouteValidator;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
	@Autowired
	private JwtService jwtService;
	@Autowired
	private RouteValidator validator;
	public JwtAuthenticationFilter() {
		super(Config.class);
	}
	public static class Config {
	}
	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			if (validator.isSecured.test(exchange.getRequest())) {
				// récupération du header Authorization
				String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					// extraction du token JWT
					String jwt = authHeader.substring(7);

					if (!jwtService.CheckToken(jwt) && !jwtService.isTokenValid(jwt)) {
						return this.onError(exchange, "Accès non autorisé à l'application", HttpStatus.UNAUTHORIZED);
					}
				} else {
					// Si le header Authorization est manquant
					throw new RuntimeException("Header d'autorisation manquant");
				}
			}
			return chain.filter(exchange);
		};
	}
	private Mono<Void> onError(ServerWebExchange exchange, String string, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);  // Définit le code de statut de la réponse HTTP
		return response.setComplete();  // Indique que la réponse est complète et terminée
	}

}
