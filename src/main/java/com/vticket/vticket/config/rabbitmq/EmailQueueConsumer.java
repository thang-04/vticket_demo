package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.vticket.vticket.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueConsumer {
    private static final Logger logger = LogManager.getLogger(EmailQueueConsumer.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserCollection userCollection;
    @Autowired
    private MessageService messageService;

    //Auto listener message from queue
    @RabbitListener(queues = Config.RABBITMQ.QUEUE_MAIL)
    public void receiveEmailMessage(LoginEventMessage payload) {
        try {
            // Query user info từ DB
            User user = userCollection.getUserById(payload.getUserId());

            if (user == null) {
                logger.info("User not found for ID: " + payload.getUserId());
                return;
            }

            String subject = messageService.get("email.welcome.subject", user.getFull_name());
            String body = messageService.get("email.welcome.body", user.getFull_name(), payload.getLoginTime());

            // send email
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            System.out.println("Email sent to: " + user.getEmail());
        } catch (Exception ex) {
            logger.error("Error processing email message for user ID: " + payload.getUserId(), ex);
        }
    }

    @RabbitListener(queues = Config.RABBITMQ.QUEUE_MAIL_TICKET)
    public void receiveEmailTicketMessage(PaymentResponse payload) {
        try {
            if (payload.getUserId() == null) {
                logger.info("PaymentResponse userId is null");
                return;
            }
            // Query user info từ DB
            User user = userCollection.getUserById(payload.getUserId());
            if (user == null) {
                logger.info("User not found for ID: " + payload.getUserId());
                return;
            }
                String subject = messageService.get("email.ticket.subject", user.getFull_name());
            String body = messageService.get("email.ticket.body",
                    user.getFull_name(),
                    payload.getEventId(),
                    payload.getBookingCode(),
                    payload.getTotalAmount()
            );
//            String subject = "Vé sự kiện của bạn từ VTicket";
//            String body = "Chào " + user.getFull_name() + ",\n\n" +
//                    "Cảm ơn bạn đã sử dụng dịch vụ của VTicket. Dưới đây là thông tin vé sự kiện của bạn:\n\n" +
//                    "Mã đặt chỗ: " + payload.getBookingCode() + "\n" +
//                    "Tổng thanh toán: " + String.format("%,.0f", payload.getTotalAmount()) + " VND\n\n" +
//                    "Vui lòng mang theo mã đặt chỗ này khi đến sự kiện.\n\n" +
//                    "Trân trọng,\n" +
//                    "Đội ngũ VTicket";

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            System.out.println("Ticket email sent to: " + user.getEmail());

        } catch (Exception ex) {
            logger.error("Error processing ticket email message: " + payload, ex);
        }
    }
}
