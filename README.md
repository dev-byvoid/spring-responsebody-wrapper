# 介绍
基于注解和自定义 **`HandlerMethodReturnValueHandler`**，在不破坏spring默认配置情况下，实现对springmvc restful接口返回结果的包装增加增加业务返回码和说明信息，类似：
```json

{
    "code": "0",
    "message": "success",
    "data": {
        "id": 1,
        "name": "Spring技术内幕"
    }
}

```

# 使用方式
在启动类上加上注解`@EnableResponseBodyWrapper`，在使用`@ResponseBody`的地方使用`@ResponseBodyWrapper`。

示例：

```java
@SpringBootApplication
@Slf4j
@EnableResponseBodyWrapper
public class ApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }

    @ResponseBodyWrapper
    @RestController
    public static class DemoController {

        @GetMapping("/demo")
        public Object demo() {
            return ImmutableMap.of("id", 1, "name", "Spring技术内幕");
        }

        @GetMapping("/exception")
        public void exception(boolean throwBizException) {
            if (!throwBizException) {
                throw new ArithmeticException("/ By Zero");
            } else {
                throw new BizException("E000", "除数不能为0");
            }
        }
    }

    // 重写springboot中的默认异常属性
    @Bean
    public DefaultErrorAttributes defaultErrorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
                Map<String, Object> attributes = super.getErrorAttributes(webRequest, includeStackTrace);
                return ImmutableMap.of("code", attributes.getOrDefault("status", "500"), "message", attributes.getOrDefault("message", ""));
            }
        };
    }

    @ResponseBodyWrapper
    @RestControllerAdvice
    public static class ExceptionHandlerAdviceCustomizer {

        @ExceptionHandler(BizException.class)
        public Object bizExceptionHandler(BizException e) {
            log.error("bizException handle", e);
            return new ObjectWrapper<>(e.getCode(), e.getMessage());
        }

        @ExceptionHandler(Throwable.class)
        public Object throwableHandler(Throwable e) {
            log.error("throwable handle", e);
            return new ObjectWrapper<>("E500", e.getMessage());
        }
    }

    //自定义带异常码的业务异常类
    public static class BizException extends RuntimeException {

        private String code;
        private String message;

        public BizException(String code, String message) {
            super(message);
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return this.code;
        }
    }
}

```

覆盖默认返回码：
```yaml
response-body:
  wrapper:
    default-code: 200
```
