package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Company;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {

    Set<Company> findAllByIdIn(Set<Long> ids);

    @Query(value = "SELECT * FROM tbl_company WHERE id = :id", nativeQuery = true)
    Optional<Company> findCompanyById(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM tbl_company WHERE name = :name AND state = :state", nativeQuery = true)
    Integer countByNameAndState(@Param("name") String name, @Param("state") String state);
}
