package com.example.realtime.web;

import com.example.realtime.model.SensorReading;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.Message;
import org.springframework.data.mongodb.core.messaging.ReactiveMongoMessageListenerContainer;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;

@Controller
public class StreamController {

  private final ReactiveMongoTemplate template;
  private final ReactiveMongoMessageListenerContainer container;

  @Autowired
  public StreamController(ReactiveMongoTemplate template) {
    this.template = template;
    this.container = new ReactiveMongoMessageListenerContainer(template);
  }

  // SSE stream of new SensorReading changes
  @GetMapping(path = "/stream/readings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @ResponseBody
  public Flux<Map<String, Object>> streamReadings() {
    ChangeStreamRequest<ChangeStreamDocument<Document>> request =
        ChangeStreamRequest.builder()
            .collection("sensor_readings")
            .filter(Criteria.where("operationType").in("insert", "update", "replace"))
            .build();

    Flux<Message<ChangeStreamDocument<Document>, ChangeStreamEvent<Document>>> flux =
        container.register(request, Document.class);

    return flux
        .map(msg -> msg.getBody().getFullDocument())
        .filter(doc -> doc != null)
        .map(doc -> Map.of(
            "timestamp", doc.get("timestamp"),
            "value", doc.get("value")
        ));
  }

  // Quick insert API to simulate data (POST /api/readings?value=42.5)
  @PostMapping("/api/readings")
  @ResponseBody
  public Map<String, Object> addReading(@RequestParam double value) {
    SensorReading r = new SensorReading(Instant.now(), value);
    return template.save(r).map(saved -> Map.of(
        "id", saved.getId(),
        "timestamp", saved.getTimestamp(),
        "value", saved.getValue()
    )).block(); // for demo simplicity
  }

  // Serve the index page
  @GetMapping("/")
  public String index() { return "forward:/index.html"; }
}
If you prefer a simpler approach without the message container, you can also use template.changeStream(SensorReading.class) to get a Flux<ChangeStreamEvent<SensorReading>> and map it to SSE. The above uses the message listener container for clarity.
