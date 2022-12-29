package com.ssafy.crudtest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;
    private String path;
    private String title;
    private String password;

    @Builder
    public Content(String path, String title, String password) {
        super();
        this.path = path;
        this.title = title;
        this.password = password;
    }

}
