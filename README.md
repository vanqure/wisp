## wisp

A minimal, high-performance event bus.

### Why Wisp?

- **High Performance:** Fast pub/sub routing.
- **Minimal & Lightweight:** Tiny memory footprint, no unnecessary boilerplate.
- **All you need:** Subscribe, publish, and event handling result processing however you like.

### Get started

You can build dependency and append it to your local .m2 directory, by using: `./gradlew publishToMavenLocal`

### Wisp in action:

```java
final Wisp wisp = Wisp.create().result(
        String.class, new ResultProcessor<Event, String>() {
            @Override
            public void process(final Event event, final String result) {
                System.out.print("processing event " + event + " with result " + result);
            }
        });

wisp.subscribe(new ExampleEventSubscriber()); 
wisp.publish(new ExampleEvent("Hello World!"), "example-topic");

public record ExampleEvent(String message) implements Event {}

public class ExampleEventSubscriber implements Subscriber {

    @Override
    public String topic() {
        return "example-topic";
    }

    @Subscribe
    public String handle(final ExampleEvent event) {
        System.out.println("Received event: " + event);
        return event.message();
    }
}
```
