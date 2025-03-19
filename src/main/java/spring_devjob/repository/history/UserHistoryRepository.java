package spring_devjob.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.history.UserHistory;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory,Long> {
    boolean existsById(long id);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
