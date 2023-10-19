package byvoid.spring.web.servlet.config.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ResponseBodyWrapperConfiguration.class})
public @interface EnableResponseBodyWrapper {
}
