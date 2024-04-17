package com.fawry.notificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final JavaMailSender emailSender;
    private final Environment environment;

    @KafkaListener(topics = "notificationTopic" ,groupId = "groupId")
    public void send(NotificationRequest notificationRequest){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("${spring.mail.username}");
        message.setTo(notificationRequest.getDestinationEmail());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getMsg());
        Notification notification = Notification
                                            .builder()
                                            .content(notificationRequest.getMsg())
                                            .destination(notificationRequest.getDestinationEmail())
                                            .sentAt(new Date())
                                            .subject(notificationRequest.getSubject())
                                            .build();
        try{
            emailSender.send(message);
            notification.setStatus(Status.SENT);
            notificationRepo.save(notification);
            log.info("notification was sent successfully!");
        }catch (MailException ex){
            notification.setStatus(Status.FAILED);
            String retryAmountString = environment.getProperty("retry.amount");
            int retryAmount = Integer.parseInt(retryAmountString);
            notification.setRetryAmount(retryAmount);
            notificationRepo.save(notification);
            log.info("notification was not sent successfully!");
            throw ex;
        }
    }
}
