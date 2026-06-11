package com.finsight.repository;

import com.finsight.entity.Category;
import com.finsight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findAllByUserOrSystem(@Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user IS NULL OR c.user = :user)")
    Optional<Category> findByIdAndUserOrSystem(@Param("id") Long id, @Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user = :user) AND c.name = :name AND c.type = :type")
    Optional<Category> findByNameAndType(@Param("name") String name, @Param("type") String type, @Param("user") User user);
}
