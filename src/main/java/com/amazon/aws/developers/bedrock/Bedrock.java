package com.amazon.aws.developers.bedrock;

import com.amazon.aws.developers.bedrock.agent.Agent;
import com.amazon.aws.developers.bedrock.agent.utils.Response;
import software.amazon.awssdk.regions.Region;

import java.util.Scanner;
import java.util.UUID;

public class Bedrock {
    public static void main(String[] args) {

        Agent agent = Agent.builder()
                .agentId("your_id_here")
                .agentAliasId("your_alias_here")
                .sessionId(UUID.randomUUID().toString())
                .enableTrace(true) // default is false
                .region(Region.US_WEST_2)
                .build();

        Scanner scanner = new Scanner(System.in);
        String userInput;

        System.out.println("Welcome to the Bedrock CLI! Type 'exit' to end the conversation.");
        System.out.print("\nEnter your prompt: ");

        while (!(userInput = scanner.nextLine()).equalsIgnoreCase("exit")) {
            try {
                Response response = agent.invoke(userInput);
                System.out.println("\nBedrock Response:");
                System.out.println(response.conversation());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            System.out.print("\nEnter your prompt (or 'exit' to quit): ");
        }
        scanner.close();
    }
}

