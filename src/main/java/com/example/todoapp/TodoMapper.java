package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    List<Todo> findAll();

    Todo findById(@Param("id") Long id);

    void insert(Todo todo);

    void update(Todo todo);

    void deleteById(@Param("id") Long id);
}
