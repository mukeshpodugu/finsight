package com.finsight.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for global default categories

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // 'INCOME' or 'EXPENSE'

    @Column(name = "color_code", nullable = false, length = 7)
    @Builder.Default
    private String colorCode = "#cccccc";

    @Column(name = "icon_name", nullable = false, length = 50)
    @Builder.Default
    private String iconName = "folder";
}
