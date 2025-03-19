package spring_devjob.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.history.CompanyHistory;

@Repository
public interface CompanyHistoryRepository extends JpaRepository<CompanyHistory,Long> {
    boolean existsById(long id);
}
