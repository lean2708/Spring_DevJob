package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring_devjob.constants.FileType;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.entity.FileEntity;
import spring_devjob.exception.FileException;
import spring_devjob.service.FileService;

import java.io.IOException;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/v1/file")
public class FileController {

    private final FileService fileService;

    @PreAuthorize("hasAuthority('UPLOAD_IMAGE')")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileEntity> uploadImage(@RequestParam("fileImage") MultipartFile file) throws IOException, FileException {
        return new ResponseEntity<>(fileService.uploadFile(file, FileType.IMAGE), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('UPLOAD_VIDEO')")
    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileEntity> uploadVideo(@RequestParam("fileVideo") MultipartFile file) throws IOException, FileException {
        return new ResponseEntity<>(fileService.uploadFile(file, FileType.VIDEO), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('UPLOAD_CV')")
    @Operation(summary = "Upload CV", description = "API này để upload file CV (PDF, DOCX)")
    @PostMapping(value = "/upload/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileEntity> uploadCV(@RequestParam("fileCV") MultipartFile file) throws IOException, FileException {
        return new ResponseEntity<>(fileService.uploadFile(file, FileType.CV), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('GET_ALL_FILES')")
    @GetMapping("/all")
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }


    @PreAuthorize("hasAuthority('DELETE_FILE')")
    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<?> delete(@PathVariable String publicId) throws Exception {
        boolean isDeleted = fileService.deleteFile(publicId);
        if (isDeleted){
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("Xóa thành công file")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("File không tồn tại")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }
}
