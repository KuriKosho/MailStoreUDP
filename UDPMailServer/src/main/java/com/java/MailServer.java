package com.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MailServer {
    private static final int SERVER_PORT = 12345;
    private static final String BASE_DIR = "mail_accounts";
    // Map để lưu trạng thái online của người dùng và địa chỉ UDP của họ
    private static Map<String, ClientInfo> onlineUsers = new HashMap<>();

    private static class ClientInfo {
        InetAddress address;
        int port;
        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
    }


    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String response = handleRequest(message, clientAddress, clientPort);
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String handleRequest(String message, InetAddress clientAddress, int clientPort) {
        System.out.println("Received message: " + message);
        String[] parts = message.split(":");
        String command = parts[0];
        String accountName = parts[1];
        switch (command) {
            case "LOGIN":
                onlineUsers.put(accountName, new ClientInfo(clientAddress, clientPort));
                return "LOGIN_SUCCESS";
            case "CREATE_ACCOUNT":
                onlineUsers.put(accountName, new ClientInfo(clientAddress, clientPort));
                return createAccount(accountName);
            case "SEND_EMAIL":
                String from = parts[1];
                String to = parts[2];
                String subject = parts[3];
                String content = parts[4];
                String time = parts[5] + parts[6];
                System.out.println("SEND_EMAIL: " + from + " " + to + " " + subject + " " + content + " " + time);
                sendEmail(from, to, subject, content, time);
                return getEmails(to);
//                return sendEmail(from, to, subject, content, time);
            case "GET_EMAILS":
                return getEmails(accountName);
            default:
                return "UNKNOWN_COMMAND";
        }
    }

    private static String createAccount(String accountName) {
        File accountDir = new File(BASE_DIR, accountName);
        if (!accountDir.exists()) {
            accountDir.mkdirs();
            try (FileWriter writer = new FileWriter(new File(accountDir, "new_email.txt"))) {
                writer.write("Thank you for using this service. we hope that you will feel comfortable...");
            } catch (IOException e) {
                e.printStackTrace();
                return "ERROR_CREATING_ACCOUNT";
            }
            return "ACCOUNT_CREATED";
        } else {
            return "ACCOUNT_ALREADY_EXISTS";
        }
    }

    private static String sendEmail(String accountName, String recipient, String subject, String emailContent, String time) {
        System.out.println("Sending email from " + accountName + " to " + recipient + " with content: " + emailContent);
        File recipientDir = new File(BASE_DIR, recipient);
        if (recipientDir.exists()) {
            try (FileWriter writer = new FileWriter(new File(recipientDir, "email_" + System.currentTimeMillis() + ".txt"))) {
                writer.write("FROM:" + accountName + "\n" +
                        "TO:" + recipient + "\n" +
                        "SUBJECT:" + subject + "\n" +
                        "CONTENT:" + emailContent + "\n" +
                        "TIME:" + time);
                System.out.println("Email saved successfully");
                // Kiểm tra nếu người nhận đang online
                for (String key : onlineUsers.keySet()) {
                    System.out.println(key);
                }
                if (onlineUsers.containsKey(recipient)) {
                    ClientInfo recipientInfo = onlineUsers.get(recipient);
                    System.out.println("Recipient is online");
                    String immediateEmail = "SEND_MAIL:" + accountName + ":" +
                            recipient + ":" +
                            subject + ":" +
                            emailContent + ":" +
                            time;

                    // Gửi ngay email cho người nhận qua UDP
//                    sendDirectEmail(recipientInfo.address, recipientInfo.port, immediateEmail);
                    return "EMAIL_SENT_IMMEDIATELY";
                } else {
                    // Lưu email cho người nhận, chờ khi đăng nhập
                    System.out.println("Recipient is offline");
                    return "EMAIL_STORED";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "ERROR_SENDING_EMAIL";
            }
        } else {
            return "RECIPIENT_NOT_FOUND";
        }
    }

    // Hàm gửi trực tiếp email qua UDP cho người nhận đang online
    private static void sendDirectEmail(InetAddress recipientAddress, int recipientPort, String emailContent) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = emailContent.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, recipientAddress, recipientPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static String getEmails(String accountName) {
        File accountDir = new File(BASE_DIR, accountName);
        if (accountDir.exists()) {
            StringBuilder response = new StringBuilder();
            try {
                Files.list(Paths.get(accountDir.getPath())).forEach(path -> {
                    File emailFile = path.toFile();
                    if (emailFile.isFile() && emailFile.getName().endsWith(".txt")) {
                        try {
                            // Đọc nội dung của từng file email
                            String content = new String(Files.readAllBytes(path));

                            // Tách nội dung thành các phần khác nhau (FROM, TO, SUBJECT, CONTENT, TIME)
                            String from = content.contains("FROM:") ? content.split("FROM:")[1].split("\n")[0].trim() : "";
                            String to = content.contains("TO:") ? content.split("TO:")[1].split("\n")[0].trim() : "";
                            String subject = content.contains("SUBJECT:") ? content.split("SUBJECT:")[1].split("\n")[0].trim() : "";
                            String emailContent = content.contains("CONTENT:") ? content.split("CONTENT:")[1].split("\n")[0].trim() : "";
                            String time = content.contains("TIME:") ? content.split("TIME:")[1].split("\n")[0].trim() : "";

                            // Định dạng kết quả theo yêu cầu: "from@gmail.com":"to@gmail.com":"subject":"content":"time"
                            response.append(from).append(":").append(to).append(":").append(subject).append(":").append(emailContent).append(":").append(time).append("|");
                            // Gửi email này tới người dùng nếu họ đang online
                            if (onlineUsers.containsKey(accountName)) {
                                ClientInfo clientInfo = onlineUsers.get(accountName);
                                sendDirectEmail(clientInfo.address, clientInfo.port, "SEND_EMAIL:" + from + ":" + to + ":" + subject + ":" + emailContent + ":" + time);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                return "ERROR_RETRIEVING_EMAILS";
            }
            String re = response.toString();
            return "GET_EMAILS:" + re;
        } else {
            return "ACCOUNT_NOT_FOUND";
        }
    }

}