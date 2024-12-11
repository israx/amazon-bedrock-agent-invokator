package com.amazon.aws.developers.bedrock.agent;

import com.amazon.aws.developers.bedrock.agent.utils.Response;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponseHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Agent implements AutoCloseable {
    private String sessionId;
    private String agentId;
    private String agentAliasId;
    private Region region =  Region.US_WEST_2;
    private Boolean enableTrace = false;
    private BedrockAgentRuntimeAsyncClient client;

    Agent (Builder builder){
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.agentAliasId = builder.agentAliasId;
        this.client = BedrockAgentRuntimeAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(this.region).build();
        if(builder.region != null){
            this.region = builder.region;
        }
        if(builder.enableTrace != null){
            this.enableTrace = builder.enableTrace;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        public Boolean enableTrace;
        private String sessionId;
        private String agentId;
        private String agentAliasId;
        private Region region ;
       Builder(){}

        public Builder enableTrace(Boolean enableTrace){
            this.enableTrace = enableTrace;
            return this;
        }
        public Builder region(Region region){
            this.region = region;
            return this;
        }
        public Builder sessionId(String sessionId){
            this.sessionId = sessionId;
            return this;
        }
        public Builder agentId(String agentId){
            this.agentId = agentId;
            return this;
        }
        public Builder agentAliasId(String agentAliasId){
            this.agentAliasId = agentAliasId;
            return this;
        }
        public Agent build(){
           if(sessionId == null || sessionId.isEmpty()){
               throw new IllegalStateException("SessionId cannot be null or empty");
           }
           if(agentId == null || agentId.isEmpty()){
               throw new IllegalStateException("AgentId cannot be null or empty");
           }
           if(agentAliasId == null || agentAliasId.isEmpty()){
               throw new IllegalStateException("AgentAliasId cannot be null or empty");
           }
            return new Agent(this);
        }
    }

    public Response invoke(String prompt) {
        // Create the request
        InvokeAgentRequest request = InvokeAgentRequest.builder()
                .agentId(this.agentId)
                .agentAliasId(this.agentAliasId)
                .sessionId(sessionId)
                .enableTrace(this.enableTrace)
                .inputText(prompt)
                .build();

        try {
            StringBuilder completeResponseTextBuffer = new StringBuilder();
            // invoke handler
            InvokeAgentResponseHandler handler = InvokeAgentResponseHandler.builder()
                    .onResponse(response -> {
//                        System.out.println("sessionId: " + response.sessionId());
//                        System.out.println("contentType: " + response.contentType());
                    })
                    .subscriber(
                            InvokeAgentResponseHandler.Visitor.builder()
                                    .onChunk(chunk -> completeResponseTextBuffer.append(chunk.bytes().asUtf8String()))
                                    .onTrace(trace -> {
//                                        System.out.println("trace:" + trace.trace());
                                    })
                                    .build()
                    )
                    .build();
            // Invoke the agent
            CompletableFuture<Void> response = this.client.invokeAgent(request, handler);
            // The get method will block the current thread until the ongoing Future is complete or 1 minute has passed
            response.get(1, TimeUnit.MINUTES);
            String conversation = completeResponseTextBuffer.toString();

            return Response.
                    builder().
                    response(conversation).
                    sessionId(this.sessionId).
                    build();

        } catch (Exception e) {
            System.err.println("Error invoking agent: " + e.getMessage());
            e.printStackTrace();
            this.close();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close(){
        if (client != null) {
            client.close();
        }
    }
}
