package org.example;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class BlogReceiver {
    private static final String EXCHANGE_NAME = "it_blog";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("My queue name: " + queueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Topic '" + delivery.getEnvelope().getRoutingKey() + "'");
            System.out.println(" [x] message" + message + "'");
            System.out.println(" [x] _______________________ '");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            String[] messageArray = message.split(" ", 2);
            if (messageArray.length>1 & messageArray[0].equals("set_topic")) {
                channel.queueBind(queueName,EXCHANGE_NAME, messageArray[1]);
                System.out.println(" [x] You add topic '" + messageArray[1] + "'");
            }
            if (messageArray.length>1 & messageArray[0].equals("del_topic")) {
                channel.queueUnbind(queueName, EXCHANGE_NAME, messageArray[1]);
                System.out.println(" [x] You delete topic '" + messageArray[1] + "'");
            }
            if (message.equals("bye")) {
                scanner.close();
                channel.close();
                return;
            }
        }
    }
}
