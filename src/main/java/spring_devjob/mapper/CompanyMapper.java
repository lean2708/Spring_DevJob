package spring_devjob.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.User;
import spring_devjob.entity.history.CompanyHistory;
import spring_devjob.entity.history.UserHistory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CompanyRequest request);

    CompanyResponse toCompanyResponse(Company company);

    List<CompanyResponse> toCompanyResponseList(List<Company> companies);

    CompanyHistory toCompanyHistory(Company company);

    List<CompanyHistory> toCompanyHistoryList(List<Company> companyList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompany(@MappingTarget Company company, CompanyRequest request);

    EntityBasic toEntityBasic(Company company);
}
