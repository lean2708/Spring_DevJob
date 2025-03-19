package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Company;
import spring_devjob.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {
    boolean existsByName(String name);

    Set<Company> findAllByIdIn(Set<Long> ids);

    @Query(value = "SELECT * FROM tbl_company c WHERE c.id = :id", nativeQuery = true)
    Optional<Company> findCompanyById(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) > 0 FROM tbl_company c WHERE c.state = :state AND c.name = :name", nativeQuery = true)
    long existsInactiveCompanyByName(@Param("state") String state, @Param("name") String name);

    @Modifying
    @Query(value = "SELECT * FROM tbl_company c WHERE c.state = :state AND c.deactivated_at < :date", nativeQuery = true)
    List<Company> findInactiveCompaniesBeforeDate(@Param("state") String state, @Param("date") LocalDate date);
}
