package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Company;

import java.util.List;
import java.util.Set;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {
    boolean existsByName(String name);

    Set<Company> findAllByIdIn(Set<Long> ids);
}
