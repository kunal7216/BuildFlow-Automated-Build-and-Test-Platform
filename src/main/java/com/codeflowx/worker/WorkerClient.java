package com.codeflowx.worker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simple worker client that emits periodic heartbeats to the backend.
 * Configure via environment variables:
 *  - BACKEND_URL (e.g. http://localhost:8080)
 *  - WORKER_ID (required)
 *  - HOSTNAME (optional)
 *  - SUPPORTED_LANGUAGES (optional, comma-separated)
 *  - INTERVAL_SECONDS (optional, default 10)
 */
public class WorkerClient {
  public static void main(String[] args) throws Exception {
    String backend = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8080");
    String workerId = System.getenv().getOrDefault("WORKER_ID", "worker-" + new Random().nextInt(1000));
    String hostname = System.getenv().getOrDefault("HOSTNAME", java.net.InetAddress.getLocalHost().getHostName());
    String langs = System.getenv().getOrDefault("SUPPORTED_LANGUAGES", "JAVA, PYTHON");
    int interval = Integer.parseInt(System.getenv().getOrDefault("INTERVAL_SECONDS", "10"));

    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper om = new ObjectMapper();
    System.out.println("Worker client starting. workerId=" + workerId + " backend=" + backend);

    final var running = new java.util.concurrent.atomic.AtomicBoolean(true);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Shutting down worker client...");
      running.set(false);
    }));

    while(running.get()){
      try {
        Map<String,Object> body = new HashMap<>();
        body.put("hostname", hostname);
        body.put("status", "IDLE");
        body.put("supportedLanguages", langs);
        body.put("activeJobs", 0);

        String json = om.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(String.format("%s/api/v1/workers/%s/heartbeat", backend, workerId)))
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("heartbeat sent, status=" + resp.statusCode());
      } catch(Exception ex){
        System.err.println("heartbeat failed: " + ex.getMessage());
      }

      try { Thread.sleep(interval * 1000L); } catch(InterruptedException ignored){ Thread.currentThread().interrupt(); break; }
    }

    System.out.println("Worker client stopped.");
  }
}
