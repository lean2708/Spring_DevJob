package spring_devjob.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.history.ResumeHistory;

@Repository
public interface ResumeHistoryRepository extends JpaRepository<ResumeHistory,Long> {
    boolean existsById(long id);
}
