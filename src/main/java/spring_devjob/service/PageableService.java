package spring_devjob.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PageableService {

    public boolean isValidField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass(); // Chuyển lên class cha
        }
        return false;
    }

    public Pageable createPageable(int pageNo, int pageSize, String sortBy, Class<?> clazz) {
        // sap xep
        List<Sort.Order> sorts = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(-)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (!matcher.find()) {
                throw new AppException(ErrorCode.INVALID_SORT_FORMAT);
            }
            if (!isValidField(clazz, matcher.group(1))) {
                throw new AppException(ErrorCode.INVALID_SORT_FIELD);
            }
            Sort.Direction sortDirection = matcher.group(3).equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sorts.add(new Sort.Order(sortDirection, matcher.group(1)));
        }

        return PageRequest.of(pageNo, pageSize, Sort.by(sorts));
    }
}
