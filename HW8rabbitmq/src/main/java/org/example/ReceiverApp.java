package org.example;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReceiverApp {
    private static final String EXCHANGER_NAME = "myExchanger";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGER_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();
        new Thread(() -> {
            try {
                readMessage (channel, queueName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }
    private static void readMessage(Channel channel, String queueName) throws IOException {
        System.out.println("Привет подписчик!");
        System.out.println("Прежде чем получать рассылки, укажи тему, на которую хочешь подписаться:");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            String topic = null;
            while (true) {
                command = reader.readLine();

                if (command.equals("exit")) break;
                if (command.equals("help")) {
                    System.out.println("Доступные команды");
                    System.out.println(" set_topic - подписка на рассылку");
                    System.out.println(" drop_topic - отменить подписку на рассылку");
                    continue;
                }
                if (command.startsWith("set_topic") && command.split(" ").length ==2) {
                    topic = command.split(" ")[1];
                    channel.queueBind(queueName, EXCHANGER_NAME, topic);
                    System.out.println("Вы подписались на рассылку, на тему : " + topic);
                } else if (command.startsWith("drop_topic") && command.split(" ").length ==2) {
                    topic = command.split(" ")[1];
                    channel.queueUnbind(queueName, EXCHANGER_NAME, topic);
                    System.out.println("Вы отписались от рассылки, на тему : " + topic);
                } else {
                    System.out.println("Введите команду в формате: 'команда тема_подписки'");
                }
            }
        }
    }
}
