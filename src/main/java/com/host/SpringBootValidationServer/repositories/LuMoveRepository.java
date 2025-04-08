package com.host.SpringBootValidationServer.repositories;

import com.host.SpringBootValidationServer.model.LuMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LuMoveRepository extends JpaRepository<LuMove, Integer> {


}
