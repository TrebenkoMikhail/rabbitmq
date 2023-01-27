package org.example;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class itReceiverApp {
    private static final String EXCHANGE_NAME = "it_blog_exchange";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("Waiting message...");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received message: " + message);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        Scanner scanner = new Scanner(System.in);
        boolean flag = true;

        while (flag) {
            System.out.println("Меню");
            System.out.println("1 : Новая подписка");
            System.out.println("2 : Удаление подписки");
            System.out.println("3 : Выход из меню");

            System.out.println("Выберите пункт меню:");
            int menu = scanner.nextInt();
            switch (menu) {
                case 1:
                    System.out.println("Укажите название подписки (set_topic название): ");
                    String setTopic = scanner.nextLine();
                    String [] arr = setTopic.split(" ", 2);
                    channel.queueBind(queueName, EXCHANGE_NAME, arr[1]);
                    System.out.println("Выподписаны подписанны на " + arr[1]);
                    break;

                case 2:
                    System.out.println("Укажите название, которую нужно удалить (delete_topic название): ");
                    String deleteTopic = scanner.nextLine();
                    String [] arr1 = deleteTopic.split(" ", 2);
                    channel.queueUnbind(queueName, EXCHANGE_NAME, arr1[1]);
                    System.out.println("Подписка на " + arr1[1] + " удалена");
                    break;

                case 3:
                    scanner.close();
                    flag = false;
                    break;
            }
        }
    }
}
