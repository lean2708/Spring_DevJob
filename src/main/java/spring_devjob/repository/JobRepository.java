package spring_devjob.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.Skill;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    boolean existsByName(String name);
    Optional<Job> findByName(String name);
    List<Job> findAllByIdIn(List<Long> ids);
    List<Job> findBySkillsIn(List<Skill> skills);
    Page<Job> findAllByResumesIn(List<Resume> resumes, Pageable pageable);
    Page<Job> findAllByCompanyId(Long companyId, Pageable pageable);
    Void deleteAllByCompanyIn(List<Company> companies);
}
