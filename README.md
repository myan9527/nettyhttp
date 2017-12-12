# Http server and mvc framework with netty.

Used 3-rd party libraries:
- Netty
- Fastjson
- Freemarker
- Logback
- Owner
- Apache commons

## Basic workflow of mvc processing:
Request -> init RoutingContext -> build action map -> parsing request -> search for request handler -> 
handle each request and build response data -> write response to client.

## Core config:
The `config.properties` need to be provided, `router.basePackage` should be included in this file. Then add the `NettyRequestDispatcher` to your channel pipeline.
 
## Example:
```java
@Router
public class BasicRouter {
    
    @Action(value = "/act", method = {RequestMethod.GET})
    public NettyResponse act(RequestParam param) {
        NettyResponse response = new JsonResponse();
        response.put("date", new Date());
        response.put("name", "Michael Yan");
        System.out.println("Query params:" + param.getInt("id"));
        return response;
    }
    
    @Action(value="/post", method = {RequestMethod.POST})
    public NettyResponse testPost(RequestParam param) {
        NettyResponse response = new JsonResponse();
        response.put("param", param);
        response.put("date", new Date().toString());
        return response;
    }
}
``` 
Now, we only support GET/POST method, response data type will include plain txt/json/html, html template engine: freemarker.