package br.com.atocf.pedido.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class Users {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 45)
    private String name;
}