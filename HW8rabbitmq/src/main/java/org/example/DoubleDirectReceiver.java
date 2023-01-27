package org.example;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class DoubleDirectReceiver {
    private static final String EXCHANGE_NAME = "DoubleDirect";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();

        while (true) {
            System.out.println("Введите действие: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            StringBuilder stringBuilder = new StringBuilder();
            if (input.startsWith("set_topic")) {
                String type = stringBuilder.substring(10);
                if (type.startsWith("php")) {
                    channel.queueBind(queueName, EXCHANGE_NAME, "php");
                    System.out.println(" [*] Waiting for php messages");
                }
                if (type.startsWith("java")) {
                    channel.queueBind(queueName,EXCHANGE_NAME, "java");
                    System.out.println(" [*] Waiting for java messages");
                }
            }
            if (input.startsWith("delete_topic")) {
                String delete = stringBuilder.substring(13);
                if (delete.startsWith("php")) {
                    channel.queueUnbind(queueName, EXCHANGE_NAME, "php");
                    System.out.println(" [*] Stop waiting for php messages");
                }
                if (delete.startsWith("java")) {
                    channel.queueUnbind(queueName, EXCHANGE_NAME, "java");
                    System.out.println(" [*] Stop waiting for java messages");
                }
            }
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("[X] Received '" + message + "'");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        }
    }
}
