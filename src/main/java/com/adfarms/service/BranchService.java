package com.adfarms.service;

import com.adfarms.entity.BranchEntity;
import com.adfarms.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    public List<BranchEntity> findAll() {
        return branchRepository.findAll();
    }

    public BranchEntity save(BranchEntity branch) {
        return branchRepository.save(branch);
    }

    public BranchEntity findById(Long id) {
        return branchRepository.findById(id).orElse(null);
    }
}
