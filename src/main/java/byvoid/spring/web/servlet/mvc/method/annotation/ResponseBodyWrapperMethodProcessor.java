package byvoid.spring.web.servlet.mvc.method.annotation;

import byvoid.spring.web.ObjectWrapper;
import byvoid.spring.web.bind.annotation.ResponseBodyWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.List;

public class ResponseBodyWrapperMethodProcessor extends RequestResponseBodyMethodProcessor {

    private final String defaultCode;

    public ResponseBodyWrapperMethodProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice, String defaultCode) {
        super(converters, manager, requestResponseBodyAdvice);
        this.defaultCode = defaultCode;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return super.supportsReturnType(returnType) && (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBodyWrapper.class) ||
                returnType.hasMethodAnnotation(ResponseBodyWrapper.class));
    }


    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        if (!(returnValue instanceof ObjectWrapper)) {
            ObjectWrapper<Object> objectWrapper = new ObjectWrapper<>();
            objectWrapper.setCode(defaultCode);
            objectWrapper.setData(returnValue);
            returnValue = objectWrapper;
        }

        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

}
