# Typesafe and Self-contained Components 
(work in progress)
## locationservice 
Build a small REST web service (Location Service) that takes JSON containing an address and returns JSON with the corresponding latitude longitude coordinates.

## Making spray typesafe
The basic architecture of the spray server can be understood as a partial function (receive), which asynchronously evaluates, by an actor request response mechanism:

HttpRequest => HttpResponse

more precise:

def receive = { case HttpRequest( ??? ) => sender ! HttpResponse(???) }

Based on the request, we like to find a type safe function

HttpRequest => function: Function1[-T1, +R] (omitting case)

But we also like to call that function, so we have also to extract the parameter from the request

HttpRequest => parameter: T1

So in summary we have something like:

HttpRequest => Call(parameter, function)

In your example the parameter would be the address:String and the function would map it to a location. At the end we map the type return value to a HttpResponse.

The drawback is that the function is a synchronous call. So we would have repair that:

HttpRequest => function: Function1[-T1, Future[R]]

Example:

HttpRequest => locate: Function[String, Future[Location]]

This enables us to make a non-blocking call inside that function. In our example a non-blocking request to Google Maps API. But that API I would like to wrap in a similar manner:

HttpRequest => googleLocate: Function[String, Future[GoogleLocation]]

So we can call simply googleLocate inside locate by simply mapping 

Future[GoogleLocation] => Future[Location]

with the help of a for expression. Inside googleLocate we use a promise in order to produce the future.
If we like to have real functional composability we should also lift the input order to produce the future.

HttpRequest => function: Function1[Function[T1], Future[R]]

Then we can take the output of one function as input for the other function. For convenience we should have an implicit conversion from T1 to Future[T1] in scope.
Further improvements can be made regarding separation of concerns – e.g. separating out the state. In our case the server is stateless.

## Self contained

We like to make those functions self-contained. That means to put all the things corresponding to those functions in a common component structure. The addition of functionality may be context depending. Perhaps only within a testing environment the test functions are added – for convenience by implicit conversion. This context dependency provides are great flexibility, because it is extensible and overcomes the restrictions of a rigid structure.

1. The locate function and a routing part of the web service matching some part of the URL-path belong together. So we put the routing directive calling the typed function into a component together with that function.Because we deal with asynchronous and typed functions the directive would comprise onComplete, entity, and produce directives.
Another idea is to derive the matching URL-path from the function’s class path. But the configuration file may override.
2. The test functions for all those functions could have a common abstraction.
3. Same with performance testing.
4. A common error handling interface. E.g. timeout of googleLocate.
5. A common logging interface.
6. A common validation interface.
7. A common caching behavior interface.
8. A common monitoring interface.
9. Dependency injection infrastructure. locate could depend on various 
10. Each function is configurable by the configuration file. E.g. the timeout of implementations and not only on googleLocate.
11. The missing parts ...

## Configurable
A configurable frame work putting the self-contained components together.
