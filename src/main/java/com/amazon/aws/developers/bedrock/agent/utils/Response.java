package com.amazon.aws.developers.bedrock.agent.utils;

public class Response {
     private String conversation;
     private String sessionId;

     public String conversation() {
        return conversation;
    }
    public String sessionId() {
        return sessionId;
    }
   static public Builder builder(){
        return new Builder();
    }

    Response(Builder builder){
        this.conversation = builder.response;
        this.sessionId = builder.sessionId;
    }
    public static class Builder{
        private String response;
        private String sessionId;

        Builder(){}

        public Builder response(String response){
            this.response = response;
            return this;
        }
        public Builder sessionId(String sessionId){
            this.sessionId = sessionId;
            return this;
        }

        public Response build(){
            if(sessionId == null || sessionId.isEmpty()){
                throw new IllegalStateException("SessionId cannot be null or empty");
            }
            if(response == null || response.isEmpty()){
                throw new IllegalStateException("Response cannot be null or empty");
            }
            return new Response(this);
        }
    }
}
