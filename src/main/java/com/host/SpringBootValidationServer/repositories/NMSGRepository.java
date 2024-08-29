package com.host.SpringBootValidationServer.repositories;

import com.host.SpringBootValidationServer.model.NsNmsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NMSGRepository extends JpaRepository<NsNmsg, Integer> {
}
