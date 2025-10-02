package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.message.LoginEventMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    //Auto listener message from queue
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME + "1")
    public void receiveEmailMessage(LoginEventMessage payload) {
        // Query user info từ DB
        User user = userCollection.getUserById(payload.getUserId());

        if (user == null) {
            logger.info("User not found for ID: " + payload.getUserId());
            return;
        }

        String subject = "Chào mừng " + user.getFull_name();
        String body = "Xin chào " + user.getFull_name() + ",\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản tại VTicket vào lúc " + payload.getLoginTime() +
                "\n\nChúng tôi rất vui được chào đón bạn đến với cộng đồng của chúng tôi." +
                "\nHãy khám phá các sự kiện hấp dẫn và tận hưởng những trải nghiệm tuyệt vời cùng VTicket." +
                "\nNếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi." +
                "\n\nChúc bạn có những trải nghiệm tuyệt vời!" +
                "\nTrân trọng," +
                "\nĐội ngũ VTicket";

        // send email
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject(subject);
        mail.setText(body);
        mailSender.send(mail);

        System.out.println("Email sent to: " + user.getEmail());
    }
}
