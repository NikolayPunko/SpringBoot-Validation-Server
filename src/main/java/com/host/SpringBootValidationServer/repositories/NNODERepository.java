package com.host.SpringBootValidationServer.repositories;

import com.host.SpringBootValidationServer.model.NsNnode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NNODERepository extends JpaRepository<NsNnode, Integer> {
}
