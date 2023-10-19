package byvoid.spring.web.servlet.config.annotation;

import byvoid.spring.web.servlet.mvc.method.annotation.ResponseBodyWrapperMethodProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Configuration(proxyBeanMethods = false)
public class ResponseBodyWrapperConfiguration implements InitializingBean {

    private final RequestMappingHandlerAdapter adapter;
    private final HandlerExceptionResolver resolverComposite;


    private String defaultCode;

    public ResponseBodyWrapperConfiguration(
            @Qualifier("requestMappingHandlerAdapter") RequestMappingHandlerAdapter adapter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolverComposite,
            @Value("${response-body.wrapper.default-code:200}") String defaultCode) {
        this.adapter = adapter;
        this.resolverComposite = resolverComposite;
        this.defaultCode = defaultCode;
    }

    @Override
    public void afterPropertiesSet() throws Exception {


        Field field = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, "contentNegotiationManager");
        ReflectionUtils.makeAccessible(field);
        ContentNegotiationManager contentNegotiationManager = (ContentNegotiationManager) ReflectionUtils.getField(field, adapter);

        field = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, "requestResponseBodyAdvice");
        ReflectionUtils.makeAccessible(field);
        List<Object> requestResponseBodyAdvice = (List<Object>) ReflectionUtils.getField(field, adapter);

        final List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(this.adapter.getReturnValueHandlers());

        handlers.add(IntStream.range(0, handlers.size())
                .filter(index -> handlers.get(index) instanceof RequestResponseBodyMethodProcessor)
                .findFirst()
                .orElse(handlers.size()), new ResponseBodyWrapperMethodProcessor(adapter.getMessageConverters(),
                contentNegotiationManager, requestResponseBodyAdvice, defaultCode));
        this.adapter.setReturnValueHandlers(handlers);


        if (!(resolverComposite instanceof HandlerExceptionResolverComposite)) {
            return;
        }

        HandlerExceptionResolverComposite composite = (HandlerExceptionResolverComposite) resolverComposite;
        List<HandlerExceptionResolver> resolvers = composite.getExceptionResolvers();
        resolvers.stream()
                .filter(e -> e instanceof ExceptionHandlerExceptionResolver)
                .forEach(e -> {

                    ExceptionHandlerExceptionResolver resolver = (ExceptionHandlerExceptionResolver) e;
                    Field responseBodyAdvice = ReflectionUtils.findField(ExceptionHandlerExceptionResolver.class, "responseBodyAdvice");
                    ReflectionUtils.makeAccessible(responseBodyAdvice);
                    List<Object> responseBodyAdvices = (List<Object>) ReflectionUtils.getField(responseBodyAdvice, resolver);

                    ArrayList<HandlerMethodReturnValueHandler> list = new ArrayList<>(resolver.getReturnValueHandlers().getHandlers());
                    list.add(IntStream.range(0, list.size())
                            .filter(index -> list.get(index) instanceof RequestResponseBodyMethodProcessor)
                            .findFirst()
                            .orElse(list.size()), new ResponseBodyWrapperMethodProcessor(resolver.getMessageConverters(),
                            resolver.getContentNegotiationManager(), responseBodyAdvices, defaultCode));

                    resolver.setReturnValueHandlers(list);
                });

    }
}
