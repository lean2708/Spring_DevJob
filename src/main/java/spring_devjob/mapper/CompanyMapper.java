package spring_devjob.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.entity.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CompanyRequest request);

    CompanyResponse toCompanyResponse(Company company);

    CompanyBasic toCompanyBasic(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompany(@MappingTarget Company company, CompanyRequest request);
}
