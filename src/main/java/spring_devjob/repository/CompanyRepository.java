package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Company;

@Repository
public interface CompanyRepository  extends JpaRepository<Company,Long> {
    boolean existsByName(String name);


    Company findByName(String name);
}
