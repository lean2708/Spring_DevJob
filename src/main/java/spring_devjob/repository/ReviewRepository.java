package spring_devjob.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Set<Review> findAllByIdIn(Set<Long> ids);

    Page<Review> findAllByCompanyId(Pageable pageable, long companyId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.company.id = :companyId")
    double getAverageRatingByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.company.id= :companyId")
    int getTotalReviewsByCompanyId(@Param("companyId") Long companyId);

    @Modifying
    @Query(value = "UPDATE tbl_review r " +
            "SET r.state = :state, r.deactivated_at = :deactivatedAt " +
            "WHERE r.user_id = :userId", nativeQuery = true)
    int updateAllReviewsByUserId(@Param("userId") Long userId,
                               @Param("state") String state,
                               @Param("deactivatedAt") LocalDate deactivatedAt);

    @Modifying
    @Query(value = "UPDATE tbl_review r " +
            "SET r.state = :state, r.deactivated_at = :deactivatedAt " +
            "WHERE r.company_id= :companyId", nativeQuery = true)
    int updateAllReviewsByCompanyId(@Param("companyId") Long companyId,
                                 @Param("state") String state,
                                 @Param("deactivatedAt") LocalDate deactivatedAt);
}
