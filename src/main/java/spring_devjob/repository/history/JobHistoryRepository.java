package spring_devjob.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.history.JobHistory;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory,Long> {
    boolean existsById(long id);
}
