package spring_devjob.service;

import org.springframework.web.multipart.MultipartFile;
import spring_devjob.constants.FileType;
import spring_devjob.entity.FileEntity;
import spring_devjob.exception.FileException;

import java.io.IOException;
import java.util.List;

public interface FileService {

    FileEntity uploadFile(MultipartFile file, FileType type) throws IOException, FileException;

    List<FileEntity> getAllFiles();

    Boolean deleteFile(String publicId) throws Exception;
}
