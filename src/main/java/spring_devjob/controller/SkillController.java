package spring_devjob.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.SkillService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class SkillController {
    private final SkillService skillService;

    @PostMapping("/skills")
    public ApiResponse<SkillResponse> create(@Valid @RequestBody SkillRequest request){
        return ApiResponse.<SkillResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Skill")
                .result(skillService.create(request))
                .build();
    }

    @GetMapping("/skills/{id}")
    public ApiResponse<SkillResponse> fetchSkillById(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                         @PathVariable long id){
        return ApiResponse.<SkillResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Skill By Id")
                .result(skillService.fetchSkillById(id))
                .build();
    }

    @GetMapping("/skills")
    public ApiResponse<PageResponse<SkillResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                             @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                 @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<SkillResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(skillService.fetchAllSkills(pageNo, pageSize, sortBy))
                .message("Fetch All Skills With Pagination")
                .build();
    }

    @PutMapping("/skills/{id}")
    public ApiResponse<SkillResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                 @PathVariable long id, @RequestBody SkillRequest request){
        return ApiResponse.<SkillResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Skill By Id")
                .result(skillService.update(id, request))
                .build();
    }

    @DeleteMapping("/skills/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        skillService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Skill By Id")
                .result(null)
                .build();
    }

    @DeleteMapping("/skills")
    public ApiResponse<Void> deleteSkills(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                          Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        skillService.deleteSkills(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Skills")
                .result(null)
                .build();
    }
}
