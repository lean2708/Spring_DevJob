package spring_devjob.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring_devjob.dto.response.FileResponse;
import spring_devjob.exception.FileException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {
    @Value("${aws.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public FileResponse uploadFile(MultipartFile multipartFile) throws IOException, FileException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException("File trống. Không thể lưu trữ file");
        }
        boolean isValidFile = isValidFile(multipartFile);

        List<String> allowedFileExtensions = new ArrayList<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp","svg", "webp"));

        if (!isValidFile || !allowedFileExtensions.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))){
            throw new FileException("File " +  multipartFile.getOriginalFilename() + " không hợp lệ. Định dạng file hoặc tên file không được hỗ trợ.");
        }

        // convert multipart file  to a file
        File file = new File(System.getProperty("java.io.tmpdir"), multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            fileOutputStream.write(multipartFile.getBytes());
        }

        // generate file name
        String fileName = generateFileName(multipartFile);
        // upload file
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("plain/"+ FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        metadata.addUserMetadata("Title", "File Upload - " + fileName);
        metadata.setContentLength(file.length());
        request.setMetadata(metadata);
        s3Client.putObject(request);

        String fileUrl = s3Client.getUrl(bucketName, fileName).toString();

        // delete file
        file.delete();

        return FileResponse.builder()
                .fileName(fileName)
                .url(fileUrl)
                .build();
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('PRO')")
    public Object downloadFile(String fileName) throws FileException {
        if (bucketIsEmpty()) {
            throw new FileException("Bucket yêu cầu không tồn tại hoặc trống.");
        }
        try {
            S3Object object = s3Client.getObject(bucketName, fileName);

            try (S3ObjectInputStream s3is = object.getObjectContent()) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                    byte[] read_buf = new byte[1024];
                    int read_len;
                    while ((read_len = s3is.read(read_buf)) > 0) {
                        fileOutputStream.write(read_buf, 0, read_len);
                    }
                }

                Path pathObject = Paths.get(fileName);
                Resource resource = new UrlResource(pathObject.toUri());

                if (resource.exists() && resource.isReadable()) {
                    return resource;
                } else {
                    throw new FileException("Không thể tìm thấy file!");
                }
            }
        } catch (AmazonS3Exception e) {
            if (e.getErrorCode().equals("NoSuchKey")) {
                throw new FileException("File yêu cầu không tồn tại trong kho lưu trữ.");
            } else {
                throw new FileException("Lỗi khi truy cập file trong S3: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new FileException("Lỗi khi xử lý file: " + e.getMessage());
        }
    }


    public boolean delete(String fileName) {
        try {
            s3Client.deleteObject(bucketName, fileName);
            return true;
        } catch (AmazonS3Exception e) {
            System.out.println("Lỗi khi xóa file khỏi S3: " + e.getMessage());
            return false;
        }
    }

    private boolean bucketIsEmpty() {
        ListObjectsV2Result result = s3Client.listObjectsV2(this.bucketName);
        if (result == null){
            return false;
        }
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        return objects.isEmpty();
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().trim().replaceAll(" ", "_");
    }
    public boolean isValidFile(MultipartFile multipartFile){
        log.info("Empty Status ==> {}", multipartFile.isEmpty());
        if (Objects.isNull(multipartFile.getOriginalFilename())){
            return false;
        }
        return !multipartFile.getOriginalFilename().trim().equals("");
    }

}

