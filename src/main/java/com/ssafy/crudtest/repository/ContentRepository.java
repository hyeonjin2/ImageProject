package com.ssafy.crudtest.repository;

import com.ssafy.crudtest.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Integer> {
    public List<Content> findTop1000ByOrderByUidDesc();
}
