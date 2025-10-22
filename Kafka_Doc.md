# Migration từ RabbitMQ sang Kafka - VTicket Project

## Phần 1: Kiến Thức Lý Thuyết về Apache Kafka

### 1.1 Apache Kafka là gì?

Apache Kafka là một **distributed event streaming platform** (nền tảng streaming sự kiện phân tán) được thiết kế để xử lý hàng triệu sự kiện/giây với độ trễ thấp, độ tin cậy cao và khả năng mở rộng tốt.

**So sánh Kafka vs RabbitMQ:**

| Đặc điểm | RabbitMQ | Kafka |

|----------|----------|-------|

| **Kiến trúc** | Message Broker truyền thống | Distributed Log/Event Stream |

| **Message Storage** | Xóa sau khi consume | Lưu trữ lâu dài (retention) |

| **Throughput** | ~20-50K msg/s | ~1M+ msg/s |

| **Độ trễ** | Thấp (~milliseconds) | Rất thấp (~milliseconds) |

| **Use case** | Task queues, RPC | Event sourcing, Log aggregation, Stream processing |

| **Ordering** | Per queue | Per partition |

| **Replay messages** | Không | Có (replay từ offset) |

### 1.2 Các Khái Niệm Cốt Lõi

#### **a) Topic**

- Topic là một **category/feed** để lưu trữ messages (events)
- Tương tự như **Queue** trong RabbitMQ nhưng mạnh mẽ hơn
- Ví dụ: `email-login-events`, `email-ticket-events`

#### **b) Partition**

- Mỗi topic được chia thành nhiều **partitions**
- Partition là một **ordered, immutable sequence** of records
- Messages trong cùng partition được đảm bảo thứ tự
- Cho phép **parallel processing** và **scalability**
```
Topic: email-login-events
├── Partition 0: [msg1, msg2, msg3, ...]
├── Partition 1: [msg4, msg5, msg6, ...]
└── Partition 2: [msg7, msg8, msg9, ...]
```


#### **c) Producer**

- Gửi messages vào Kafka topics
- Quyết định message đi vào partition nào (thông qua partition key)
- Tương tự **EmailQueueProducer** hiện tại của bạn

#### **d) Consumer & Consumer Group**

- **Consumer**: đọc messages từ topics
- **Consumer Group**: nhóm các consumers cùng đọc một topic
- Mỗi partition chỉ được một consumer trong group xử lý (đảm bảo không duplicate)
- Cho phép **horizontal scaling**
```
Consumer Group: email-service-group
├── Consumer 1 → Partition 0, 1
└── Consumer 2 → Partition 2, 3
```


#### **e) Offset**

- Vị trí của message trong partition
- Consumer tracking offset để biết đã đọc đến đâu
- Cho phép **replay messages** từ offset cũ

#### **f) Broker**

- Kafka server lưu trữ và phục vụ data
- Cluster gồm nhiều brokers cho high availability

#### **g) Replication**

- Mỗi partition có nhiều bản sao (replicas) trên các brokers khác nhau
- Đảm bảo **fault tolerance**: nếu một broker chết, data vẫn an toàn

### 1.3 Kafka Architecture Flow

```
┌─────────────┐                    ┌──────────────────────┐
│  Producer   │ ──── publish ────> │   Kafka Cluster      │
│ (Spring App)│                    │  ┌────────────────┐  │
└─────────────┘                    │  │ Broker 1       │  │
                                   │  │ Topic: email   │  │
                                   │  │ - Partition 0  │  │
┌─────────────┐                    │  │ - Partition 1  │  │
│  Consumer   │ <──── poll ─────── │  └────────────────┘  │
│ (Spring App)│                    │  ┌────────────────┐  │
└─────────────┘                    │  │ Broker 2       │  │
                                   │  │ (replicas)     │  │
                                   │  └────────────────┘  │
                                   └──────────────────────┘
```

### 1.4 Kafka vs RabbitMQ trong Context VTicket

**Hiện tại với RabbitMQ:**

```
LoginService → EmailQueueProducer → Exchange → Queue → EmailQueueConsumer → Send Email
```

**Với Kafka:**

```
LoginService → KafkaEmailProducer → Topic (email-login-events) → KafkaEmailConsumer → Send Email
```

**Ưu điểm khi dùng Kafka cho VTicket:**

1. **Event sourcing**: lưu lại toàn bộ lịch sử login/booking events
2. **Replay**: có thể replay emails nếu cần (debug, resend)
3. **Analytics**: dễ dàng tích hợp với analytics tools (Elasticsearch, Spark)
4. **Scalability**: dễ scale khi số lượng events tăng cao
5. **Multiple consumers**: nhiều services có thể đọc cùng events (analytics, audit, email)

---

## Phần 2: Hướng Dẫn Cài Đặt Kafka Standalone trên Windows

### 2.1 Prerequisites

- Java 8+ (bạn đang dùng Java 21 ✓)
- Windows 10/11

### 2.2 Download Kafka

1. Tải Kafka từ: https://kafka.apache.org/downloads

    - Chọn phiên bản: **Scala 2.13** - **kafka_2.13-3.9.0.tgz**

2. Giải nén vào thư mục: `C:\kafka`

### 2.3 Cấu Hình

**File: `C:\kafka\config\server.properties`**

```properties
# Thay đổi log directories cho Windows
log.dirs=C:/kafka/kafka-logs

# Tăng retention time (lưu messages 7 ngày)
log.retention.hours=168

# Số lượng partitions mặc định
num.partitions=3
```

**File: `C:\kafka\config\zookeeper.properties`**

```properties
dataDir=C:/kafka/zookeeper-data
```

### 2.4 Chạy Kafka

**Bước 1: Start Zookeeper** (mở PowerShell/CMD với quyền Administrator)

```powershell
cd C:\kafka
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

**Bước 2: Start Kafka Broker** (mở terminal mới)

```powershell
cd C:\kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

### 2.5 Tạo Topics (mở terminal mới)

```powershell
# Topic cho login emails
.\bin\windows\kafka-topics.bat --create ^
  --topic email-login-events ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1

# Topic cho ticket emails
.\bin\windows\kafka-topics.bat --create ^
  --topic email-ticket-events ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1

# Kiểm tra topics
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Xem chi tiết topic
.\bin\windows\kafka-topics.bat --describe ^
  --topic email-login-events ^
  --bootstrap-server localhost:9092
```

### 2.6 Test Kafka (Optional)

**Producer test:**

```powershell
.\bin\windows\kafka-console-producer.bat ^
  --topic email-login-events ^
  --bootstrap-server localhost:9092
```

**Consumer test (terminal mới):**

```powershell
.\bin\windows\kafka-console-consumer.bat ^
  --topic email-login-events ^
  --from-beginning ^
  --bootstrap-server localhost:9092
```

### 2.7 Chạy Kafka như Windows Service (Optional - Nâng cao)

Sử dụng **NSSM** (Non-Sucking Service Manager):

1. Download NSSM: https://nssm.cc/download
2. Cài đặt Zookeeper service
3. Cài đặt Kafka service

---

## Phần 3: Implementation - Tích Hợp Kafka vào VTicket

### 3.1 Thêm Dependencies vào `pom.xml`

```xml
<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 3.2 Cấu Hình Kafka trong `application.yaml`

Thêm configuration song song với RabbitMQ:

```yaml
spring:
  # ... existing configs ...
  
  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: vticket-email-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.vticket.vticket.dto.*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false
```

### 3.3 Tạo Kafka Configuration Class

**File mới: `src/main/java/com/vticket/vticket/config/kafka/KafkaConfig.java`**

```java
package com.vticket.vticket.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    public static final String TOPIC_EMAIL_LOGIN = "email-login-events";
    public static final String TOPIC_EMAIL_TICKET = "email-ticket-events";
    
    @Bean
    public NewTopic emailLoginTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_LOGIN)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic emailTicketTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_TICKET)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
```

### 3.4 Tạo Kafka Producer

**File mới: `src/main/java/com/vticket/vticket/config/kafka/EmailKafkaProducer.java`**

```java
package com.vticket.vticket.config.kafka;

import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class EmailKafkaProducer {
    private static final Logger logger = LogManager.getLogger(EmailKafkaProducer.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public EmailKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendEmailLoginEvent(LoginEventMessage payload) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaConfig.TOPIC_EMAIL_LOGIN, payload.getUserId(), payload);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent login email event for userId={} with offset={}", 
                    payload.getUserId(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send login email event for userId={}", 
                    payload.getUserId(), ex);
            }
        });
    }
    
    public void sendEmailTicketEvent(PaymentResponse payload) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(KafkaConfig.TOPIC_EMAIL_TICKET, payload.getUserId(), payload);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent ticket email event for bookingCode={} with offset={}", 
                    payload.getBookingCode(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send ticket email event for bookingCode={}", 
                    payload.getBookingCode(), ex);
            }
        });
    }
}
```

### 3.5 Tạo Kafka Consumer

**File mới: `src/main/java/com/vticket/vticket/config/kafka/EmailKafkaConsumer.java`**

```java
package com.vticket.vticket.config.kafka;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import com.vticket.vticket.service.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailKafkaConsumer {
    private static final Logger logger = LogManager.getLogger(EmailKafkaConsumer.class);
    
    private final JavaMailSender mailSender;
    private final UserCollection userCollection;
    private final MessageService messageService;
    
    public EmailKafkaConsumer(JavaMailSender mailSender, 
                             UserCollection userCollection,
                             MessageService messageService) {
        this.mailSender = mailSender;
        this.userCollection = userCollection;
        this.messageService = messageService;
    }
    
    @KafkaListener(
        topics = KafkaConfig.TOPIC_EMAIL_LOGIN,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoginEvent(
            @Payload LoginEventMessage payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("Received login event from partition={}, offset={}, userId={}", 
            partition, offset, payload.getUserId());
        
        try {
            User user = userCollection.getUserById(payload.getUserId());
            
            if (user == null) {
                logger.warn("User not found for ID: {}", payload.getUserId());
                return;
            }
            
            String subject = messageService.get("email.welcome.subject", user.getFull_name());
            String body = messageService.get("email.welcome.body", 
                user.getFull_name(), payload.getLoginTime());
            
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            
            logger.info("Successfully sent login email to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Error processing login email event for userId: {}", 
                payload.getUserId(), ex);
            // Kafka sẽ tự động retry nếu consumer throw exception
            throw ex;
        }
    }
    
    @KafkaListener(
        topics = KafkaConfig.TOPIC_EMAIL_TICKET,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTicketEvent(
            @Payload PaymentResponse payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("Received ticket event from partition={}, offset={}, bookingCode={}", 
            partition, offset, payload.getBookingCode());
        
        try {
            if (payload.getUserId() == null) {
                logger.warn("PaymentResponse userId is null");
                return;
            }
            
            User user = userCollection.getUserById(payload.getUserId());
            if (user == null) {
                logger.warn("User not found for ID: {}", payload.getUserId());
                return;
            }
            
            String subject = messageService.get("email.ticket.subject", user.getFull_name());
            String body = messageService.get("email.ticket.body",
                user.getFull_name(),
                payload.getEventId(),
                payload.getBookingCode(),
                payload.getTotalAmount()
            );
            
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            
            logger.info("Successfully sent ticket email to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Error processing ticket email event: {}", payload, ex);
            throw ex;
        }
    }
}
```

### 3.6 Update Config Class

**Update: `src/main/java/com/vticket/vticket/config/Config.java`**

Thêm constants cho Kafka:

```java
public static final class KAFKA {
    // Email Events
    public static final String TOPIC_EMAIL_LOGIN = "email-login-events";
    public static final String TOPIC_EMAIL_TICKET = "email-ticket-events";
    
    // Consumer Group
    public static final String GROUP_EMAIL_SERVICE = "vticket-email-service";
}
```

### 3.7 Update EmailService để dùng cả RabbitMQ và Kafka

**Update: `src/main/java/com/vticket/vticket/service/EmailService.java`**

Thêm feature flag để switch giữa RabbitMQ và Kafka:

```java
@Service
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Value("${app.mail.from}")
    private String mailFrom;
    
    @Value("${app.messaging.provider:rabbitmq}") // rabbitmq hoặc kafka
    private String messagingProvider;

    private final JavaMailSender mailSender;
    private final EmailQueueProducer emailQueueProducer; // RabbitMQ
    private final EmailKafkaProducer emailKafkaProducer; // Kafka
    private final MessageService messageService;

    public EmailService(JavaMailSender mailSender, 
                       EmailQueueProducer emailQueueProducer,
                       EmailKafkaProducer emailKafkaProducer,
                       MessageService messageService) {
        this.mailSender = mailSender;
        this.emailQueueProducer = emailQueueProducer;
        this.emailKafkaProducer = emailKafkaProducer;
        this.messageService = messageService;
    }

    public void sendWelcomeEmail(User newUser) {
        long start = System.currentTimeMillis();
        logger.info("Preparing to send welcome email to user ID: {} at {}", 
            newUser.getId(), newUser.getEmail());

        LoginEventMessage payload = LoginEventMessage.builder()
                .userId(newUser.getId())
                .loginTime(new Date())
                .build();

        if ("kafka".equalsIgnoreCase(messagingProvider)) {
            emailKafkaProducer.sendEmailLoginEvent(payload);
            logger.info("Enqueued welcome email via Kafka for user ID: {} in {} ms", 
                newUser.getId(), (System.currentTimeMillis() - start));
        } else {
            emailQueueProducer.sendEmailLoginToQueue(payload);
            logger.info("Enqueued welcome email via RabbitMQ for user ID: {} in {} ms", 
                newUser.getId(), (System.currentTimeMillis() - start));
        }
    }

    public void sendTicketMail(Booking successBooking) {
        long start = System.currentTimeMillis();

        PaymentResponse payload = PaymentResponse.builder()
                .bookingCode(successBooking.getBookingCode())
                .totalAmount(successBooking.getTotalAmount())
                .eventId(successBooking.getEventId())
                .userId(successBooking.getUserId())
                .build();
                
        if ("kafka".equalsIgnoreCase(messagingProvider)) {
            emailKafkaProducer.sendEmailTicketEvent(payload);
            logger.info("Enqueued ticket email via Kafka in {} ms", 
                (System.currentTimeMillis() - start));
        } else {
            emailQueueProducer.sendEmailTicketToQueue(payload);
            logger.info("Enqueued ticket email via RabbitMQ in {} ms", 
                (System.currentTimeMillis() - start));
        }
    }
}
```

### 3.8 Update application.yaml

Thêm config để switch giữa RabbitMQ và Kafka:

```yaml
app:
  mail:
    from: ${MAIL_FROM}
  messaging:
    provider: kafka  # hoặc 'rabbitmq' để dùng RabbitMQ
```

---

## Phần 4: Testing & Monitoring

### 4.1 Test Flow

1. **Start services**: Zookeeper → Kafka → Spring Boot App
2. **Test login**: User đăng nhập → Email được gửi via Kafka
3. **Test booking**: User đặt vé → Email xác nhận via Kafka

### 4.2 Monitor Kafka

**Xem consumer offset:**

```powershell
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 ^
  --group vticket-email-service --describe
```

**Xem messages trong topic:**

```powershell
.\bin\windows\kafka-console-consumer.bat ^
  --topic email-login-events ^
  --from-beginning ^
  --bootstrap-server localhost:9092 ^
  --property print.key=true ^
  --property print.offset=true
```

### 4.3 Kafka UI Tools (Optional)

- **Kafka Tool**: http://www.kafkatool.com/
- **Conduktor**: https://www.conduktor.io/
- **AKHQ**: https://akhq.io/

---

## Phần 5: Advanced Features

### 5.1 Error Handling & Retry

Thêm retry logic với `@RetryableTopic`:

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    autoCreateTopics = "false",
    include = {Exception.class}
)
@KafkaListener(topics = KafkaConfig.TOPIC_EMAIL_LOGIN)
public void consumeLoginEvent(@Payload LoginEventMessage payload) {
    // processing logic
}
```

### 5.2 Dead Letter Topic (DLT)

Kafka tự động tạo DLT khi message fail sau nhiều retries:

- Topic: `email-login-events-dlt`

### 5.3 Message Replay

Để replay messages từ offset cũ:

```java
@KafkaListener(topics = "email-login-events")
public void consume(ConsumerRecord<String, LoginEventMessage> record) {
    // Có thể lưu offset và seek lại khi cần replay
}
```

---

## Summary: RabbitMQ vs Kafka trong VTicket

**Tiếp tục dùng RabbitMQ khi:**

- Cần guaranteed delivery với routing phức tạp
- Message cần được xóa ngay sau khi consume
- Workload nhỏ (<50K msg/s)

**Chuyển sang Kafka khi:**

- Cần lưu trữ event history
- Cần replay messages
- Cần high throughput (>100K msg/s)
- Muốn tích hợp analytics/monitoring
- Cần multiple consumers cho cùng events

**Strategy cho VTicket:**

1. **Phase 1**: Chạy song song RabbitMQ + Kafka
2. **Phase 2**: Monitor và so sánh performance
3. **Phase 3**: Migrate hoàn toàn sang Kafka nếu phù hợp
4. **Phase 4**: Giữ RabbitMQ cho critical emails, Kafka cho analytics