package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProgrammingLangReceiver {
    public static void main(String[] args) throws Exception{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Print exchanger name to connect");
        String exchangerName = scanner.nextLine();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setConnectionTimeout(10000);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangerName, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();
        List<String> deliveryThemes = new ArrayList<>();
        System.out.println("Print themes to deliver and 'end' to exit binding");
        String inputTheme;
        while (scanner.hasNextLine() && !((inputTheme = scanner.nextLine()).equalsIgnoreCase("end"))) {
            deliveryThemes.add(inputTheme);
        }
        for (String themes : deliveryThemes) {
            channel.queueBind(queueName, exchangerName, themes);
        }
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        Thread unbinding = new Thread(() -> {
            String unbindingTheme;
            while (true) {
                if (scanner.hasNextLine() && !(unbindingTheme = scanner.nextLine()).substring(0, 5).equalsIgnoreCase("unbind")) {
                    try {
                        channel.queueUnbind(queueName,exchangerName, unbindingTheme.substring(7));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
            unbinding.start();
    }
}
