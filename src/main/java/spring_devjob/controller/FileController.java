package spring_devjob.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.FileResponse;
import spring_devjob.exception.FileException;
import spring_devjob.service.FileService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/file")
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileResponse> uploadFile(@RequestParam("fileName") @NotNull MultipartFile file) throws FileException, IOException {
        return ApiResponse.<FileResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Upload File")
                .result(fileService.uploadFile(file))
                .build();
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("fileName") @NotBlank @NotNull String fileName) throws FileException {
        Object response = fileService.downloadFile(fileName);
        if (response != null){
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(response);
        } else {
            ApiResponse apiResponse = ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Không thể tải file xuống")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam("fileName") @NotBlank @NotNull String fileName){
        boolean isDeleted = fileService.delete(fileName);
        if (isDeleted){
            ApiResponse apiResponse = ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("Xóa thành công file")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            ApiResponse apiResponse = ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("File không tồn tại")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }
}
