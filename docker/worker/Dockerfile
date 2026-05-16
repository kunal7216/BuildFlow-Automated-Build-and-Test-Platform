FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/codeflowx-0.1.0.jar /app/app.jar
ENV BACKEND_URL=http://backend:8080
ENV WORKER_ID=worker-1
ENV SIMULATE_LOAD=true
CMD ["sh","-c","java -cp /app/app.jar com.codeflowx.worker.WorkerClient"]