package com.hnue.english.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    public String uploadFile(MultipartFile file) throws IOException {
        String bucketName = "quan-ly-sinh-vien-72421.appspot.com";
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Blob blob = StorageClient.getInstance().bucket(bucketName).create(fileName, file.getInputStream(), file.getContentType());
        return blob.getMediaLink();
    }

    public void deleteFile(String url) {
        try {
            String bucketName = "quan-ly-sinh-vien-72421.appspot.com";
            Bucket bucket = storage.get(bucketName);
            String[] parts = url.split("/o/");
            String filePath = parts[1].split("\\?")[0];
            filePath = URLDecoder.decode(filePath, "UTF-8");
            if (bucket != null) {
                Blob blob = bucket.get(filePath);
                if (blob != null){
                    blob.delete();
                }else{
                    throw new RuntimeException("File not found: " + filePath);
                }
            } else {
                throw new RuntimeException("Bucket not found: " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
