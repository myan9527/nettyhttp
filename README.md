# Http server and mvc framework with netty.

Some 3-rd libraries:
- Netty
- Fastjson
- Logback
- Apache httpclient
- Owner
- Apache commons

## Basic workflow of mvc processing:
Request -> init RoutingContext -> build action map -> parsing request -> search for request handler -> 
handle each request and build response data -> write response to client.

## Core config:
The `mvc.properties` need to be provided, `router.basePackage` should be included in this file. Then add the `NettyRequestDispatcher` to your channel pipeline.
 