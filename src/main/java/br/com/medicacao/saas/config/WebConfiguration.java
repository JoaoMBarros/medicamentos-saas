package br.com.medicacao.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfiguration implements PageableHandlerMethodArgumentResolverCustomizer {

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public void customize(org.springframework.data.web.PageableHandlerMethodArgumentResolver resolver) {
        resolver.setMaxPageSize(MAX_PAGE_SIZE);
    }
}
