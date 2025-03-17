package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;
import spring_devjob.entity.history.CompanyHistory;
import spring_devjob.entity.history.JobHistory;
import spring_devjob.entity.history.ResumeHistory;
import spring_devjob.entity.history.UserHistory;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.CompanyRepository;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.history.CompanyHistoryRepository;
import spring_devjob.repository.history.JobHistoryRepository;
import spring_devjob.repository.history.ResumeHistoryRepository;
import spring_devjob.repository.history.UserHistoryRepository;
import spring_devjob.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EntityHistoryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserHistoryRepository userHistoryRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CompanyHistoryRepository companyHistoryRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobHistoryRepository jobHistoryRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeHistoryRepository resumeHistoryRepository;
    private final ResumeMapper resumeMapper;

    @Value("${app.convert.days}")
    private long days;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void moveEntitiesToHistory() {
        moveUserToHistory();

        moveCompanyToHistory();

        moveJobToHistory();

        moveResumeToHistory();
    }

    private void moveUserToHistory(){
        List<User> userList = userRepository.findInactiveUsersBeforeDate(EntityStatus.INACTIVE,
                LocalDate.now().minusDays(days));

        List<UserHistory> userHistoryList = userMapper.toUserHistoryList(userList);

        userRepository.deleteAll(userList);

        userHistoryRepository.saveAll(userHistoryList);
    }

    private void moveCompanyToHistory(){
        List<Company> companyList = companyRepository.findInactiveCompaniesBeforeDate(EntityStatus.INACTIVE,
                LocalDate.now().minusDays(days));

        List<CompanyHistory> companyHistoryList = companyMapper.toCompanyHistoryList(companyList);

        companyRepository.deleteAll(companyList);

        companyHistoryRepository.saveAll(companyHistoryList);
    }

    private void moveJobToHistory(){
        List<Job> jobList = jobRepository.findInactiveJobsBeforeDate(EntityStatus.INACTIVE,
                LocalDate.now().minusDays(days));

        List<JobHistory> jobHistoryList = jobMapper.toJobHistoryList(jobList);

        jobRepository.deleteAll(jobList);

        jobHistoryRepository.saveAll(jobHistoryList);
    }

    private void moveResumeToHistory(){
        List<Resume> resumeList = resumeRepository.findInactiveResumesBeforeDate(EntityStatus.INACTIVE,
                LocalDate.now().minusDays(days));

        List<ResumeHistory> resumeHistoryList = resumeMapper.toResumeHistoryList(resumeList);

        resumeRepository.deleteAll(resumeList);

        resumeHistoryRepository.saveAll(resumeHistoryList);
    }
}
