package com.host.SpringBootValidationServer.repositories;

import com.host.SpringBootValidationServer.model.NsNrule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NRULERepository extends JpaRepository<NsNrule, Integer> {
}
