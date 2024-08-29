package com.host.SpringBootValidationServer.repositories;

import com.host.SpringBootValidationServer.model.NsGrNmsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GRNMSGRepository extends JpaRepository<NsGrNmsg, Integer> {
}
