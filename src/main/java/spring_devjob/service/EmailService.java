package spring_devjob.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import spring_devjob.dto.response.ResponseEmailJob;
import spring_devjob.entity.Job;
import spring_devjob.entity.Skill;
import spring_devjob.entity.Subscriber;
import spring_devjob.entity.User;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.SubscriberRepository;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;

    @Async
    public void sendEmail(String to, String subject, Map<String, Object> model, String templateName){
        MimeMessage message = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariables(model);

            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserEmailWithRegister(User user){
        if(user != null){
            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getName());
            this.sendEmail(user.getEmail(),
                    "Chúc mừng! Tài khoản DevJob của bạn đã được đăng kí thành công",
                    model,
                    "register"
            );
        }
    }

    public void sendVerificationCode(User user, String verificationCode) {
        if (user != null && verificationCode != null) {

            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getName());
            model.put("verificationCode", verificationCode);
            model.put("expirationTime", "10 phút");

            this.sendEmail(user.getEmail(),
                    "Mã xác nhận của bạn để khôi phục mật khẩu",
                    model,
                    "verification"
            );
        }
    }

    public void sendUserEmailWithPayment(Subscriber subscriber) {
        if (subscriber != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("name", subscriber.getName());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = subscriber.getExpiryDate().format(formatter);
            model.put("expiryDate", formattedDate);
            this.sendEmail(subscriber.getEmail(),
                    "Chúc mừng! Bạn đã đăng kí thành công gói Pro",
                    model,
                    "payment"
            );
        }
    }

    @Scheduled(cron = "0 0 */4 * * *")
    @Transactional
    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs != null && listSubs.size() > 0) {
            for (Subscriber sub : listSubs) {
                Set<Skill> skillSet = sub.getSkills();
                if (skillSet != null && skillSet.size() > 0) {
                    Set<Job> jobSet = this.jobRepository.findBySkillsIn(skillSet);
                    if (jobSet != null && !jobSet.isEmpty()) {
                        List<ResponseEmailJob> list = jobSet.stream().map(
                                job -> this.convertJobToSendEmail(job)).collect(Collectors.toList());
                        Map<String, Object> model = new HashMap<>();
                        model.put("name", sub.getName());
                        model.put("jobs", list);
                        this.sendEmail(
                                sub.getEmail(),
                                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                                model,
                                "job"
                        );
                    }
                }
            }
        }
    }
    public ResponseEmailJob convertJobToSendEmail(Job job) {
        Set<Skill> skills = job.getSkills();
        List<ResponseEmailJob.SkillEmail> s = skills.stream().
                map(skill -> new ResponseEmailJob.SkillEmail(skill.getName())).
                collect(Collectors.toList());

        return ResponseEmailJob.builder()
                .name(job.getName())
                .salary(job.getSalary())
                .company(new ResponseEmailJob.CompanyEmail(job.getCompany().getName(), job.getCompany().getLogoUrl()))
                .skills(s)
                .build();
    }
}
