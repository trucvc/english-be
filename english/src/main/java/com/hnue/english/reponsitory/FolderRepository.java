package com.hnue.english.reponsitory;

import com.hnue.english.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Integer> {
}
