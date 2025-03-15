package spring_devjob.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;

import java.util.List;
import java.util.Set;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long> {

    boolean existsByName(String name);

    Set<Resume> findAllByIdIn(Set<Long> ids);

    Page<Resume> findAllByUser(User user, Pageable pageable);

    List<Resume> findAllByUserId(long userId);

    Page<Resume> findAllByJob(Job job, Pageable pageable);
}
