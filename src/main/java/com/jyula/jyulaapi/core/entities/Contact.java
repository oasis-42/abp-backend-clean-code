package com.jyula.jyulaapi.core.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@ToString
@NoArgsConstructor
@Setter
@Getter
@Entity
@SuperBuilder
@Table(name = "contacts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Contact extends BaseEntity {
    @Column(name = "email")
    private String email;
    @Column(name = "name")
    private String name;
}
