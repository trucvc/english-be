package com.hnue.english.service;

import com.hnue.english.model.Folder;
import com.hnue.english.reponsitory.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;

    public void createFolder(Folder theFolder){
        folderRepository.save(theFolder);
    }

    public Folder getFolder(int id){
        Folder folder = folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có folder nào với id: " + id));
        return folder;
    }

    public List<Folder> getAllFolder(){
        return folderRepository.findAll();
    }

    public Folder updateFolder(int id, Folder theFolder){
        Folder folder = folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có folder nào với id: " + id));
        folder.setFolderName(theFolder.getFolderName());
        folder.setDescription(theFolder.getDescription());
        return folderRepository.save(folder);
    }

    public void deleteFolder(int id){
        Folder folder = folderRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có folder nào với id: " + id));
        folderRepository.delete(folder);
    }
}
