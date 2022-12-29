package com.ssafy.crudtest.controller;

import com.ssafy.crudtest.dto.ContentResDto;
import com.ssafy.crudtest.entity.Content;
import com.ssafy.crudtest.repository.ContentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/content")
public class Restcontroller {

    ContentRepository contentRepository;

    // 사진 목록 조회 API
    @GetMapping
    public ResponseEntity<?> list() {
        try {
            final List<Content> contents = contentRepository.findTop1000ByOrderByUidDesc();
            log.info("contents : {}", contents);

            List<ContentResDto> list = contents.stream().map(content -> {
                return ContentResDto.builder()
                        .uid(content.getUid())
                        .path(content.getPath())
                        .title(content.getTitle())
                        .build();
            }).toList();
            log.info("result list : {}", list);

            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            log.info(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 사진 목록 작성 API
    @PostMapping
    public ResponseEntity<Map<String, String>> post(
            @RequestPart("picture") MultipartFile pic, @RequestParam("title") String title,
            @RequestParam("password") String password) throws IOException {

        Map<String, String> resultMap = new HashMap<>();
        log.info("picture : {}, title : {}, password : {}", pic, title, password);

        String path = System.getProperty("user.dir");
        File file = new File(path + "/src/main/resources/static/" + pic.getOriginalFilename());
        log.info("path : {}, file : {}", path, file);

        // 폴더 생성 : mkdir()
        if (!file.exists()) { // 폴더가 존재하는지 체크, 없다면 생성
            if (file.mkdir()) {
                log.info("폴더 생성 성공");
            } else {
                log.info("폴더 생성 실패");
            }
        } else { // 폴더가 존재한다면
            log.info("폴더가 이미 존재합니다.");
        }

        // 저장할 파일 존재 확인
        File f = new File(file, title); // File(디렉터리 객체, 파일명)
        log.info("저장될 파일의 절대 경로 : {}", f.getAbsolutePath());

        if (!pic.isEmpty()) {
            if (!f.exists()) {
                // 이미지 저장하기
                pic.transferTo(file);
                log.info("파일 생성 성공");
            } else {
                log.info("파일이 이미 존재합니다.");
            }
        } else {
            log.info("입력으로 들어온 이미지가 없습니다.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 데이터 저장
        try {
            Content content = Content.builder()
                    .path(pic.getOriginalFilename())
                    .title(title)
                    .password(password)
                    .build();
            contentRepository.save(content);
            resultMap.put("path", content.getPath());
            return new ResponseEntity<>(resultMap, HttpStatus.CREATED);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // 사진 목록 수정 API
    @PutMapping("/{uid}")
    @Transactional
    public ResponseEntity<?> update(
            @PathVariable int uid, @RequestPart("picture") MultipartFile pic, @RequestParam("title") String title,
            @RequestParam("password") String password) throws IOException {

        Map<String, String> resultMap = new HashMap<>();
        try {
            Content content = contentRepository.findById(uid).orElseThrow();
            // 비밀번호가 같으면
            if (content.getPassword().equals(password)) {
                if (!pic.isEmpty()) {
                    String path = System.getProperty("user.dir");
                    File file = new File(path + "/src/main/resources/static/" + pic.getOriginalFilename());

                    File f = new File(file, title); // File(디렉터리 객체, 파일명)
                    log.info("저장될 파일의 절대 경로 : {}", f.getAbsolutePath());

                    if (!f.exists()) {
                        // 이미지 저장하기
                        pic.transferTo(file);
                        log.info("파일 생성 성공");
                        // 데이터 바꾸기
                        try {
                            content.setPath(pic.getOriginalFilename());
                            content.setTitle(title);

                            contentRepository.save(content);
                            resultMap.put("path", content.getPath());
                            log.info("content 업데이트 : {}", content);
                            return new ResponseEntity<>(resultMap, HttpStatus.OK);
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        log.info("파일이 이미 존재합니다.");
                        return new ResponseEntity<>(HttpStatus.CONFLICT);
                    }
                } else {
                    log.info("입력으로 들어온 이미지가 없습니다.");
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            // 비밀번호가 다르면
            else {
                log.info("비밀번호가 일치하지 않습니다.");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            log.info("데이터가 없습니다.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    // 글 삭제 API
    @DeleteMapping("/{uid}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable int uid) {
        try {
            Content content = contentRepository.findById(uid).orElseThrow();
            contentRepository.delete(content);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException s) {
            log.info("일치하는 데이터가 없습니다.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
