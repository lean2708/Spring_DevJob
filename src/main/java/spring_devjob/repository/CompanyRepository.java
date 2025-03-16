package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Company;

import java.util.List;
import java.util.Set;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {
    boolean existsByName(String name);

    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.name = :name AND c.state = 'INACTIVE'")
    boolean existsInactiveCompanyByName(@Param("name") String name);

    Set<Company> findAllByIdIn(Set<Long> ids);
}
