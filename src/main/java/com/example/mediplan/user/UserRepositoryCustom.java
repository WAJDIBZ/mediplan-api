package com.example.mediplan.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface UserRepositoryCustom {

    Page<User> search(AdminUserFilter filter, Pageable pageable);

    List<User> findAll(AdminUserFilter filter, Sort sort);
}
