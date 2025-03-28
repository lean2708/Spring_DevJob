package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.entity.Company;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CompanyMapper {

    Company toCompany(CompanyRequest request);

    CompanyResponse toCompanyResponse(Company company);

    List<CompanyResponse> toCompanyResponseList(List<Company> companies);

    void updateCompany(@MappingTarget Company company, CompanyRequest request);

    EntityBasic toEntityBasic(Company company);
}
