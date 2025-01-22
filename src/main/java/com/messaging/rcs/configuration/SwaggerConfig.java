
package com.messaging.rcs.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Server;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
	public static final String AUTHORIZATION_HEADER = "Authorization";

	private ApiInfo apiInfo() {
		return new ApiInfo("Fonada Did Masking Rest APIs", "APIs for Did Masking.", "1.0", "Terms of service",
				new Contact("1800 137 3839", "https://www.fonada.com/", "Info@Fonada.Com"), "License of API",
				"API license URL", Collections.emptyList());
	}

	@Bean
	public Docket api() {
		Server local_Server = new Server("local", "http://localhost:8172", "for local usages", Collections.emptyList(),
				Collections.emptyList());
		Server uAT_Server = new Server("UAT", "http://112.196.76.14:8080/MIS", "for UAT", Collections.emptyList(),
				Collections.emptyList());

		return new Docket(DocumentationType.OAS_30).servers(local_Server, uAT_Server).apiInfo(apiInfo())
				.securityContexts(Arrays.asList(securityContext())).securitySchemes(Arrays.asList(apiKey())).select()
				.apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
	}

	private ApiKey apiKey() {
		return new ApiKey(AUTHORIZATION_HEADER, "JWT", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference(AUTHORIZATION_HEADER, authorizationScopes));
	}
}
